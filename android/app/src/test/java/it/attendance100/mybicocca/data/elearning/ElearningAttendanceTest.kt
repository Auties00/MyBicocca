package it.attendance100.mybicocca.data.elearning

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleMobileApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleSiteApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebAttendanceApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleComponentString
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleMobileContent
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleMobileContentRestriction
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleMobileTemplate
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleNamedValue
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebScrapers
import kotlinx.coroutines.test.runTest
import org.jsoup.Jsoup
import org.junit.Test

/**
 * The attendance flows over the generated clients, on fixtures that follow the markup the plugin
 * (2024082402) renders: `view.php`'s summary table and session log, the
 * `mobile_user_form_latest` take form and the `mobile_view_page_latest` view.
 */
class ElearningAttendanceTest {
    private val token = "t".repeat(32)
    private val mobile = mockk<MoodleMobileApi>()
    private val site = mockk<MoodleSiteApi>()
    private val moodle = mockk<MoodleApi> {
        every { mobile } returns this@ElearningAttendanceTest.mobile
        every { site } returns this@ElearningAttendanceTest.site
        every { language } returns "it"
    }
    private val pages = mockk<MoodleWebAttendanceApi>()
    private val attendance = ElearningAttendance(moodle, pages)

    private val session = ElearningAttendanceSubmittableSession("901", "mer 18 nov 2026 2PM - 4PM")

    // region view.php

    private fun viewPage(userId: Int, sessionRows: String, summaryRows: String = FULL_SUMMARY) = """
        <html lang="it"><head><script>M.cfg = {"wwwroot":"https://elearning.unimib.it","userId":$userId};</script></head>
        <body id="page-mod-attendance-view">
        <table class="attlist">$summaryRows</table>
        <table class="generaltable attwidth boxaligncenter">
          <tr><th>Data</th><th>Descrizione</th><th>Stato</th><th>Punteggio</th><th>Note</th></tr>
          $sessionRows
        </table>
        </body></html>
    """.trimIndent()

    private fun row(date: String, time: String, statusCells: String) = """
        <tr><td class="datecol cell c0"><nobr>$date</nobr> <nobr>$time</nobr></td>
        <td class="desccol cell c1"><div class="text_to_html"><p>Lab</p></div></td>
        $statusCells</tr>
    """

    private fun link(sessId: Int, text: String) =
        """<td class="statuscol cell c2 lastcol" colspan="3"><a href="https://elearning.unimib.it/mod/attendance/attendance.php?sessid=$sessId&amp;sesskey=abc">$text</a></td>"""

    private fun read(page: String, futureLinkText: String? = "Report future absence") =
        ElearningAttendance.classify(MoodleWebScrapers.attendanceView(Jsoup.parse(page)), futureLinkText)

    private fun success(page: String, futureLinkText: String? = "Report future absence") =
        read(page, futureLinkText) as ElearningAttendanceViewResponse.Success

    @Test
    fun `summary rows are read by position`() {
        val summary = success(viewPage(42, "")).summary!!
        assertThat(summary.takenSessions).isEqualTo(10)
        assertThat(summary.pointsTakenLabel).isEqualTo("10 / 10")
        assertThat(summary.takenSessionsPercentage).isEqualTo(100.0)
        assertThat(summary.totalSessions).isEqualTo(82)
        assertThat(summary.pointsAllSessionsLabel).isEqualTo("10 / 82")
        assertThat(summary.allSessionsPercentage).isEqualTo(12.2)
        assertThat(summary.maxPossiblePointsLabel).isEqualTo("82 / 82")
        assertThat(summary.maxPossiblePercentage).isEqualTo(100.0)
    }

    @Test
    fun `a summary with an unexpected number of rows is not mislabeled`() {
        val sevenRows = FULL_SUMMARY.lines().filter { it.isNotBlank() }.drop(1).joinToString("\n")
        assertThat(success(viewPage(42, "", summaryRows = sevenRows)).summary).isNull()
        val nineRows = FULL_SUMMARY + "<tr><td>Altro:</td><td>1</td></tr>"
        assertThat(success(viewPage(42, "", summaryRows = nineRows)).summary).isNull()
    }

    @Test
    fun `only open submit links become candidates, with their session time`() {
        val rows = listOf(
            row("lun 11 mar 2019", "10:15AM - 10:25AM", """<td class="statuscol" colspan="3">L'iscrizione dell'utente inizia il 26.02.2026</td>"""),
            row("ven 13 mar 2026", "5:20PM - 5:24PM", "<td>Presente</td><td>1 / 1</td><td>Autoregistrato</td>"),
            row("mer 18 nov 2026", "2PM - 4PM", link(901, "Registra presenza")),
            row("mer 18 nov 2026", "2PM - 4PM", link(901, "Registra presenza")),
            row("gio 19 nov 2026", "2PM - 4PM", link(902, "Report future absence")),
            row(
                "mer 18 nov 2026", "4PM - 6PM",
                """<td colspan="3"><form action="https://elearning.unimib.it/mod/attendance/attendance.php" method="get">
                   <input type="hidden" name="sessid" value="903"><input type="text" name="qrpass"><input type="submit"></form></td>"""
            ),
        ).joinToString("\n")

        assertThat(success(viewPage(42, rows)).submittableSessions).containsExactly(session)
    }

    @Test
    fun `without the future link text every submit link is kept`() {
        val rows = row("gio 19 nov 2026", "2PM - 4PM", link(902, "Report future absence"))
        assertThat(success(viewPage(42, rows), futureLinkText = null).submittableSessions.map { it.sessionId }).containsExactly("902")
    }

    @Test
    fun `an expired session lands on the enrol page as the guest user`() {
        val enrolPage = """<html><head><script>M.cfg = {"userId":1};</script></head><body id="page-enrol-index">Opzioni di iscrizione</body></html>"""
        assertThat(read(enrolPage)).isEqualTo(ElearningAttendanceViewResponse.RequiresReauth)
    }

    @Test
    fun `a guest page served at view php also needs a new session`() {
        val guestPage = """<html><head><script>M.cfg = {"userId":1};</script></head><body id="page-mod-attendance-view">Accesso negato</body></html>"""
        assertThat(read(guestPage)).isEqualTo(ElearningAttendanceViewResponse.RequiresReauth)
    }

    @Test
    fun `a logged-in redirect elsewhere is unavailable, not an expired session`() {
        val managePage = """<html><head><script>M.cfg = {"userId":42};</script></head><body id="page-mod-attendance-manage"></body></html>"""
        assertThat(read(managePage)).isEqualTo(ElearningAttendanceViewResponse.Unavailable)
    }

    @Test
    fun `the page is read in the web services' language, skipping its future-session link`() = runTest {
        val rows = row("mer 18 nov 2026", "2PM - 4PM", link(901, "Registra presenza")) +
            row("gio 19 nov 2026", "2PM - 4PM", link(902, "Segnala assenza futura"))
        coEvery { pages.getAttendanceView(5, "it") } returns MoodleWebScrapers.attendanceView(Jsoup.parse(viewPage(42, rows)))!!
        coEvery { site.componentStrings("mod_attendance", "it", any()) } returns
            listOf(MoodleComponentString("submitattendancefuture", "Segnala assenza futura"))

        val view = attendance.getAttendanceSummary(token, 5) as ElearningAttendanceViewResponse.Success

        assertThat(view.submittableSessions).containsExactly(session)
    }

    // endregion

    // region mobile_user_form

    private fun userForm(messages: List<String> = emptyList(), password: Boolean = false, statuses: Map<String, String> = emptyMap()) =
        buildString {
            append("""<div class="attendance_mobile_user_form">""")
            messages.forEach {
                append("""<span class="messages"><ion-item class="ion-text-wrap"><ion-label>{{ 'plugin.mod_attendance.$it' | translate }}</ion-label></ion-item></span>""")
            }
            if (password) {
                append("""<ion-item><ion-label>{{ 'plugin.mod_attendance.enterpassword' | translate }}:</ion-label><ion-input type="text" name="studentpass"></ion-input></ion-item>""")
            }
            if (statuses.isNotEmpty() || password) {
                append("""<ion-radio-group name="status">""")
                statuses.forEach { (id, label) -> append("""<ion-item class="ion-text-wrap"><ion-radio value="$id">$label</ion-radio></ion-item>""") }
                append("""</ion-radio-group><ion-button method="mobile_view_activity">submit</ion-button>""")
            }
            append("</div>")
        }

    private fun content(html: String) =
        MoodleMobileContent(templates = listOf(MoodleMobileTemplate(html = html)), restrict = MoodleMobileContentRestriction())

    @Test
    fun `the take form gives the password requirement and the status labels`() = runTest {
        coEvery { mobile.getContent(any(), any(), any(), any()) } returns
            content(userForm(password = true, statuses = mapOf("11" to "Presente", "12" to "Ritardo")))

        val markable = attendance.getMarkableSessions(token, courseId = 7, moduleId = 5, sessions = listOf(session)).single()

        assertThat(markable.refusal).isNull()
        assertThat(markable.requiresPassword).isTrue()
        assertThat(markable.statuses.map { it.description }).containsExactly("Presente", "Ritardo").inOrder()
        assertThat(markable.time).isEqualTo(session.time)
    }

    @Test
    fun `a refused form keeps the refusal instead of dropping the session`() = runTest {
        coEvery { mobile.getContent(any(), any(), any(), any()) } answers {
            val sessionId = thirdArg<List<MoodleNamedValue>>().first { it.name == "sessid" }.value
            content(if (sessionId == "1") userForm(statuses = mapOf("11" to "Presente")) else userForm(messages = listOf("subnetwrong")))
        }

        val sessions = attendance.getMarkableSessions(
            token, courseId = 7, moduleId = 5,
            sessions = listOf(ElearningAttendanceSubmittableSession("1", "a"), ElearningAttendanceSubmittableSession("2", "b")),
        )

        assertThat(sessions.map { it.sessionId to it.refusal }).containsExactly("1" to null, "2" to "subnetwrong").inOrder()
        assertThat(sessions[1].statuses).isEmpty()
    }

    // endregion

    // region mobile_view_activity

    private fun mobileView(vararg items: String, messages: List<String> = emptyList()) = buildString {
        append("""<div class="attendance_mobile_view_page">""")
        messages.forEach {
            append("""<span class="messages"><ion-item class="ion-text-wrap"><ion-label>{{ 'plugin.mod_attendance.$it' | translate }}</ion-label></ion-item></span>""")
        }
        items.forEach { append(it) }
        append("""<ion-item><ion-label><ion-grid><ion-row><ion-col>Sessioni frequentate</ion-col><ion-col>10</ion-col></ion-row></ion-grid></ion-label></ion-item></div>""")
    }

    private fun item(time: String, group: String = "", status: String = "", sessId: Int? = null) = """
        <ion-item><ion-label><h2>$time</h2><h3>$group</h3><h3>$status</h3>
        ${sessId?.let { """<ion-button core-site-plugins-new-content [args]="{cmid: 5, courseid: 6, sessid: $it, timestamp: 1}">submit</ion-button>""" } ?: ""}
        </ion-label></ion-item>
    """

    private suspend fun mark(html: String): ElearningAttendanceMarkResult {
        coEvery { mobile.getContent(any(), any(), any(), any()) } returns content(html)
        return attendance.markSession(token, courseId = 7, moduleId = 5, session = session, statusId = "11")
    }

    @Test
    fun `the session's own item carries the recorded status`() = runTest {
        val html = mobileView(
            item("mer 18 nov 2026 10AM - 12PM", status = "Assente"),
            item("mer 18 nov 2026 2PM - 4PM", group = "Turno A", status = "Presente"),
        )
        assertThat(mark(html)).isEqualTo(ElearningAttendanceMarkResult.Marked("Presente"))
    }

    @Test
    fun `a session missing from the mobile window is not reported as marked`() = runTest {
        val html = mobileView(item("mer 18 nov 2026 10AM - 12PM", status = "Presente"))
        assertThat(mark(html)).isEqualTo(ElearningAttendanceMarkResult.Unconfirmed)
    }

    @Test
    fun `a session still offering its submit action was not marked`() = runTest {
        val html = mobileView(item("mer 18 nov 2026 2PM - 4PM", sessId = 901))
        assertThat(mark(html)).isEqualTo(ElearningAttendanceMarkResult.NotOpen(null))
    }

    @Test
    fun `the plugin's messages tell why marking failed`() = runTest {
        val open = item("mer 18 nov 2026 2PM - 4PM", sessId = 901)
        assertThat(mark(mobileView(open, messages = listOf("incorrectpasswordshort")))).isEqualTo(ElearningAttendanceMarkResult.WrongPassword)
        assertThat(mark(mobileView(open, messages = listOf("subnetwrong")))).isEqualTo(ElearningAttendanceMarkResult.NotOpen("subnetwrong"))
        assertThat(mark(mobileView(open, messages = listOf("attendance_no_status")))).isEqualTo(ElearningAttendanceMarkResult.Failed("attendance_no_status"))
    }

    // endregion

    private companion object {
        val FULL_SUMMARY = """
            <tr><td>Sessioni frequentate:</td><td>10</td></tr>
            <tr><td>Punteggio sulle sessione già registrate:</td><td>10 / 10</td></tr>
            <tr><td>Percentuale sulle sessioni già registrate:</td><td>100,0%</td></tr>
            <tr><td>Numero totale delle sessioni:</td><td>82</td></tr>
            <tr><td>Punteggio su tutte le sessioni:</td><td>10 / 82</td></tr>
            <tr><td>Percentuale su tutte le sessioni:</td><td>12,2%</td></tr>
            <tr><td>Punteggio massimo possibile:</td><td>82 / 82</td></tr>
            <tr><td>Percentuale massima possibile:</td><td>100,0%</td></tr>
        """.trimIndent()
    }
}
