package it.attendance100.mybicocca.data.remote.elearning.dto

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** The attendance pages and templates, and the video pages, the generated scrapers read. */
class MoodleWebScrapersTest {
    @Test
    fun `reads the attendance summary of view php by position`() {
        val page = """
            <body id="page-mod-attendance-view">
            <table class="attlist">
              <tr><td>Sessioni frequentate:</td><td>10</td></tr>
              <tr><td>Punteggio sulle sessione già registrate:</td><td>10 / 10</td></tr>
              <tr><td>Percentuale sulle sessioni già registrate:</td><td>100,0%</td></tr>
              <tr><td>Numero totale delle sessioni:</td><td>82</td></tr>
              <tr><td>Punteggio su tutte le sessioni:</td><td>10 / 82</td></tr>
              <tr><td>Percentuale su tutte le sessioni:</td><td>12,2%</td></tr>
              <tr><td>Punteggio massimo possibile:</td><td>82 / 82</td></tr>
              <tr><td>Percentuale massima possibile:</td><td>100,0%</td></tr>
            </table>
            <table class="generaltable">
              <tr><td colspan="3"><a href="https://elearning.unimib.it/mod/attendance/attendance.php?sessid=123&amp;sesskey=x">Invia presenza</a></td></tr>
              <tr><td colspan="3"><form><input type="hidden" name="sessid" value="124"></form></td></tr>
              <tr><td colspan="3"><a href="https://elearning.unimib.it/mod/attendance/attendance.php?sessid=123&amp;sesskey=x">Invia presenza</a></td></tr>
              <tr><td colspan="3">Nessuna azione</td></tr>
            </table>
            </body>
        """.trimIndent()
        val view = MoodleWebScrapers.attendanceView(Jsoup.parse(page))!!
        assertTrue(view.shown)
        val summary = view.summary!!
        assertEquals(10, summary.attendedSessions)
        assertEquals("10 / 10", summary.pointsTakenLabel)
        assertEquals(100.0, summary.takenSessionsPercentage)
        assertEquals(82, summary.totalSessions)
        assertEquals("10 / 82", summary.pointsAllSessionsLabel)
        assertEquals(12.2, summary.allSessionsPercentage)
        assertEquals("82 / 82", summary.maxPossiblePointsLabel)
        assertEquals(100.0, summary.maxPossiblePercentage)
        assertEquals(listOf("123", "124", "123"), view.submittableSessions.map { it.id })
    }

    @Test
    fun `a view php without statistics has no summary`() {
        val view = MoodleWebScrapers.attendanceView(Jsoup.parse("""<html><body id="page-mod-attendance-view"><p>Nessuna sessione</p></body></html>"""))!!
        assertTrue(view.shown)
        assertNull(view.summary)
        assertTrue(view.submittableSessions.isEmpty())
    }

    @Test
    fun `Moodle's login page is not the attendance page`() {
        val login = """<html><body id="page-login-index"><form id="login"><input name="username"></form></body></html>"""
        assertFalse(MoodleWebScrapers.attendanceView(Jsoup.parse(login))!!.shown)
    }

    @Test
    fun `reads the template rendered after marking`() {
        val template = """
            <span class="messages">
                <ion-item class="ion-text-wrap"><ion-label>{{ 'plugin.mod_attendance.incorrectpasswordshort' | translate }}</ion-label></ion-item>
            </span>
            <ion-item>
                <ion-label>
                    <h2>Lezione 12</h2>
                    <h3>Gruppo A</h3>
                    <h3>Presente</h3>
                </ion-label>
            </ion-item>
            <ion-item>
                <ion-label>
                    <h3>Gruppo B</h3>
                    <h3></h3>
                    <ion-button core-site-plugins-new-content component="mod_attendance" method="mobile_user_form" [args]="{cmid: 5, courseid: 7, sessid: 124, timestamp: 1}">Invia</ion-button>
                </ion-label>
            </ion-item>
        """.trimIndent()
        val page = MoodleWebScrapers.attendanceMarkPage(Jsoup.parse(template))!!
        assertEquals(listOf("incorrectpasswordshort"), page.messages.map { it.key })
        assertEquals(listOf("124"), page.markActions.map { it.sessionId })
        assertEquals(listOf("Gruppo A", "Presente", "Gruppo B"), page.headings)
    }

    @Test
    fun `reads a session's form`() {
        val template = """
            <form>
              <ion-input name="studentpass"></ion-input>
              <ion-radio-group>
                <ion-radio value="11">Presente</ion-radio>
                <ion-radio value="">Senza id</ion-radio>
                <ion-radio value="12">In ritardo</ion-radio>
              </ion-radio-group>
            </form>
        """.trimIndent()
        val form = MoodleWebScrapers.attendanceSessionForm(Jsoup.parse(template))!!
        assertFalse(form.refused)
        assertTrue(form.requiresPassword)
        assertEquals(listOf("11" to "Presente", "12" to "In ritardo"), form.statuses.map { it.id to it.description })
    }

    @Test
    fun `a session's form showing a message refuses marking`() {
        val template = """<ion-item><span class="messages">{{ 'plugin.mod_attendance.subnetwrong' | translate }}</span></ion-item>"""
        assertTrue(MoodleWebScrapers.attendanceSessionForm(Jsoup.parse(template))!!.refused)
    }

    @Test
    fun `reads the kaltura entry out of the player's address`() {
        val page = """<html><body><iframe src="https://kaf.unimib.it/browseandembed/index/media/entryid/1_ab-cd9/showDescription/true"></iframe></body></html>"""
        assertEquals("1_ab-cd9", MoodleWebScrapers.kalturaPlayer(Jsoup.parse(page))!!.entryId)
        assertNull(MoodleWebScrapers.kalturaPlayer(Jsoup.parse("<html><body><form id=login></form></body></html>"))!!.entryId)
    }
}
