package it.attendance100.mybicocca.data.elearning

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleMobileApi
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleWebAttendanceApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleMobileContent
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleMobileContentRestriction
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleMobileTemplate
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleNamedValue
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebAttendanceView
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebSubmittableSession
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The attendance flows over the generated clients: reading `view.php`, telling the sessions
 * open for marking apart, and reading the outcome of marking from the plugin's template.
 */
class ElearningAttendanceTest {
    private val token = "t".repeat(32)
    private val mobile = mockk<MoodleMobileApi>()
    private val moodle = mockk<MoodleApi> { every { mobile } returns this@ElearningAttendanceTest.mobile }
    private val pages = mockk<MoodleWebAttendanceApi>()
    private val attendance = ElearningAttendance(moodle, pages)

    private fun template(html: String) {
        coEvery { mobile.getContent(any(), any(), any(), any()) } returns content(html)
    }

    private fun content(html: String) =
        MoodleMobileContent(templates = listOf(MoodleMobileTemplate(html = html)), restrict = MoodleMobileContentRestriction())

    private fun button(sessionId: String) =
        """<ion-button core-site-plugins-new-content [args]="{cmid: 5, courseid: 7, sessid: $sessionId, timestamp: 1}">Invia</ion-button>"""

    private suspend fun mark(sessionId: String = "124") = attendance.markSession(token, courseId = 7, moduleId = 5, sessionId = sessionId, statusId = "11")

    @Test
    fun `a page Moodle redirected away from requires a new session`() = runTest {
        coEvery { pages.getAttendanceView(5) } returns MoodleWebAttendanceView(shown = false)
        assertThat(attendance.getAttendanceSummary(5)).isEqualTo(ElearningAttendanceViewResponse.RequiresReauth)
    }

    @Test
    fun `the page's sessions are listed once`() = runTest {
        val sessions = listOf("123", "124", "123").map { MoodleWebSubmittableSession(it) }
        coEvery { pages.getAttendanceView(5) } returns MoodleWebAttendanceView(shown = true, submittableSessions = sessions)
        val view = attendance.getAttendanceSummary(5) as ElearningAttendanceViewResponse.Success
        assertThat(view.submittableSessionIds).containsExactly("123", "124").inOrder()
    }

    @Test
    fun `a template without messages or the session's button means it was marked`() = runTest {
        template("""<ion-item><ion-label><h3>Gruppo A</h3><h3>Presente</h3></ion-label></ion-item>${button("200")}""")
        assertThat(mark()).isEqualTo(ElearningAttendanceMarkResult.Marked("Presente"))
    }

    @Test
    fun `a template still offering the session's button means it wasn't open`() = runTest {
        template(button("124"))
        assertThat(mark()).isEqualTo(ElearningAttendanceMarkResult.NotOpen(reason = null))
    }

    @Test
    fun `the plugin's messages tell why marking failed`() = runTest {
        fun message(key: String) = """<span class="messages"><ion-item>{{ 'plugin.mod_attendance.$key' | translate }}</ion-item></span>"""
        template(message("incorrectpasswordshort"))
        assertThat(mark()).isEqualTo(ElearningAttendanceMarkResult.WrongPassword)
        template(message("subnetwrong"))
        assertThat(mark()).isEqualTo(ElearningAttendanceMarkResult.NotOpen("subnetwrong"))
        template(message("attendance_no_status"))
        assertThat(mark()).isEqualTo(ElearningAttendanceMarkResult.Failed("attendance_no_status"))
    }

    @Test
    fun `sessions whose form refuses marking are left out`() = runTest {
        coEvery { mobile.getContent(any(), any(), any(), any()) } answers {
            val session = thirdArg<List<MoodleNamedValue>>().first { it.name == "sessid" }.value
            val html = if (session == "1") """<form><ion-radio value="11">Presente</ion-radio></form>""" else """<span class="messages">closed</span>"""
            content(html)
        }
        val sessions = attendance.getMarkableSessions(token, courseId = 7, moduleId = 5, sessionIds = listOf("1", "2"))
        assertThat(sessions.map { it.sessionId }).containsExactly("1")
        assertThat(sessions.single().statuses.map { it.id }).containsExactly("11")
    }
}
