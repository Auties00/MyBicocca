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
        """.trimIndent()
        val view = MoodleWebScrapers.attendanceView(Jsoup.parse(page))!!
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
        val view = MoodleWebScrapers.attendanceView(Jsoup.parse("<html><body><p>Nessuna sessione</p></body></html>"))!!
        assertNull(view.summary)
        assertTrue(view.submittableSessions.isEmpty())
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
