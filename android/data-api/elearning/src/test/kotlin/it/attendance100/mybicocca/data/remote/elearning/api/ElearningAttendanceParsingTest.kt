package it.attendance100.mybicocca.data.remote.elearning.api

import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSubmittableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceViewResponse
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Offline tests of the `mod_attendance` page and template parsing, on fixtures that follow the
 * markup the plugin (2024082402) renders: `view.php`'s summary table and session log, the
 * `mobile_user_form_latest` take form and the `mobile_view_page_latest` view.
 */
class ElearningAttendanceParsingTest {

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
        ElearningAttendanceApi.classifyViewPage("/mod/attendance/view.php", Jsoup.parse(page), futureLinkText)
            as ElearningAttendanceViewResponse.Success

    @Test
    fun `summary rows are read by position`() {
        val summary = read(viewPage(42, "")).summary!!
        assertEquals(10, summary.takenSessions)
        assertEquals("10 / 10", summary.pointsTakenLabel)
        assertEquals(100.0, summary.takenSessionsPercentage)
        assertEquals(82, summary.totalSessions)
        assertEquals("10 / 82", summary.pointsAllSessionsLabel)
        assertEquals(12.2, summary.allSessionsPercentage)
        assertEquals("82 / 82", summary.maxPossiblePointsLabel)
        assertEquals(100.0, summary.maxPossiblePercentage)
    }

    @Test
    fun `a summary with an unexpected number of rows is not mislabeled`() {
        val sevenRows = FULL_SUMMARY.lines().filter { it.isNotBlank() }.drop(1).joinToString("\n")
        assertNull(read(viewPage(42, "", summaryRows = sevenRows)).summary)
        val nineRows = FULL_SUMMARY + "<tr><td>Altro:</td><td>1</td></tr>"
        assertNull(read(viewPage(42, "", summaryRows = nineRows)).summary)
    }

    @Test
    fun `only open submit links become candidates, with their session time`() {
        val rows = listOf(
            row("lun 11 mar 2019", "10:15AM - 10:25AM", """<td class="statuscol" colspan="3">L'iscrizione dell'utente inizia il 26.02.2026</td>"""),
            row("ven 13 mar 2026", "5:20PM - 5:24PM", "<td>Presente</td><td>1 / 1</td><td>Autoregistrato</td>"),
            row("mer 18 nov 2026", "2PM - 4PM", link(901, "Registra presenza")),
            row("gio 19 nov 2026", "2PM - 4PM", link(902, "Report future absence")),
            row(
                "mer 18 nov 2026", "4PM - 6PM",
                """<td colspan="3"><form action="https://elearning.unimib.it/mod/attendance/attendance.php" method="get">
                   <input type="hidden" name="sessid" value="903"><input type="text" name="qrpass"><input type="submit"></form></td>"""
            ),
        ).joinToString("\n")

        val sessions = read(viewPage(42, rows)).submittableSessions

        assertEquals(
            listOf(ElearningAttendanceSubmittableSession(sessionId = "901", time = "mer 18 nov 2026 2PM - 4PM")),
            sessions
        )
    }

    @Test
    fun `without the future link text every submit link is kept`() {
        val rows = row("gio 19 nov 2026", "2PM - 4PM", link(902, "Report future absence"))
        val sessions = read(viewPage(42, rows), futureLinkText = null).submittableSessions
        assertEquals(listOf("902"), sessions.map { it.sessionId })
    }

    @Test
    fun `the page read as the logged-in user is a success`() {
        val response = ElearningAttendanceApi.classifyViewPage(
            path = "/mod/attendance/view.php",
            document = Jsoup.parse(viewPage(42, row("mer 18 nov 2026", "2PM - 4PM", link(901, "Registra presenza")))),
            futureLinkText = "Report future absence"
        )
        assertTrue(response is ElearningAttendanceViewResponse.Success)
        assertEquals(listOf("901"), (response as ElearningAttendanceViewResponse.Success).submittableSessions.map { it.sessionId })
    }

    @Test
    fun `an expired session lands on the enrol page as the guest user`() {
        val enrolPage = Jsoup.parse(
            """<html><head><script>M.cfg = {"userId":1};</script></head><body id="page-enrol-index">Opzioni di iscrizione</body></html>"""
        )
        assertEquals(
            ElearningAttendanceViewResponse.RequiresReauth,
            ElearningAttendanceApi.classifyViewPage("/enrol/index.php", enrolPage, futureLinkText = null)
        )
    }

    @Test
    fun `a guest page served at view php also needs a new session`() {
        val guestPage = Jsoup.parse("""<html><head><script>M.cfg = {"userId":1};</script></head><body>Accesso negato</body></html>""")
        assertEquals(
            ElearningAttendanceViewResponse.RequiresReauth,
            ElearningAttendanceApi.classifyViewPage("/mod/attendance/view.php", guestPage, futureLinkText = null)
        )
    }

    @Test
    fun `a logged-in redirect elsewhere is unavailable, not an expired session`() {
        val managePage = Jsoup.parse("""<html><head><script>M.cfg = {"userId":42};</script></head><body id="page-mod-attendance-manage"></body></html>""")
        assertEquals(
            ElearningAttendanceViewResponse.Unavailable,
            ElearningAttendanceApi.classifyViewPage("/mod/attendance/manage.php", managePage, futureLinkText = null)
        )
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

    private val session = ElearningAttendanceSubmittableSession("901", "mer 18 nov 2026 2PM - 4PM")

    @Test
    fun `the take form gives the password requirement and the status labels`() {
        val markable = ElearningAttendanceApi.parseUserForm(session, userForm(password = true, statuses = mapOf("11" to "Presente", "12" to "Ritardo")))
        assertNull(markable.refusal)
        assertTrue(markable.requiresPassword)
        assertEquals(listOf("Presente", "Ritardo"), markable.statuses.map { it.description })
        assertEquals("mer 18 nov 2026 2PM - 4PM", markable.time)
    }

    @Test
    fun `a refused form keeps the refusal instead of dropping the session`() {
        val markable = ElearningAttendanceApi.parseUserForm(session, userForm(messages = listOf("subnetwrong")))
        assertEquals("subnetwrong", markable.refusal)
        assertTrue(markable.statuses.isEmpty())
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

    @Test
    fun `the session's own item carries the recorded status`() {
        val html = mobileView(
            item("mer 18 nov 2026 10AM - 12PM", status = "Assente"),
            item("mer 18 nov 2026 2PM - 4PM", group = "Turno A", status = "Presente"),
        )
        assertEquals(ElearningAttendanceMarkResult.Marked("Presente"), ElearningAttendanceApi.classifyMarkTemplate(Jsoup.parse(html), session))
    }

    @Test
    fun `a session missing from the mobile window is not reported as marked`() {
        val html = mobileView(item("mer 18 nov 2026 10AM - 12PM", status = "Presente"))
        assertEquals(ElearningAttendanceMarkResult.Unconfirmed, ElearningAttendanceApi.classifyMarkTemplate(Jsoup.parse(html), session))
    }

    @Test
    fun `a session still offering its submit action was not marked`() {
        val html = mobileView(item("mer 18 nov 2026 2PM - 4PM", sessId = 901))
        assertEquals(ElearningAttendanceMarkResult.NotOpen(null), ElearningAttendanceApi.classifyMarkTemplate(Jsoup.parse(html), session))
    }

    @Test
    fun `a wrong password message wins`() {
        val html = mobileView(item("mer 18 nov 2026 2PM - 4PM", sessId = 901), messages = listOf("incorrectpasswordshort"))
        assertEquals(ElearningAttendanceMarkResult.WrongPassword, ElearningAttendanceApi.classifyMarkTemplate(Jsoup.parse(html), session))
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
