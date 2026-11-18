package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.settings.DeviceIdentityStore
import it.attendance100.mybicocca.data.location.DeviceLocationProvider
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningAttendanceApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSubmittableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceViewResponse
import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
import it.attendance100.mybicocca.domain.model.attendance.OpenAttendanceSession
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import it.attendance100.mybicocca.domain.repository.TranscriptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Network-only attendance marking branches that don't depend on the full pending-course
 * fan-out: the scan-kind dispatch (unrecognized payloads and lesson codes without a matricola
 * fail gracefully), the empty-modules short-circuit, open sessions read from the module page
 * (renewing an expired browser session once), and the catch-all that turns an elearning
 * failure into a [PresenceMarkOutcome.Failed].
 */
class AttendanceRepositoryImplTest {

    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val easyStaffApi: EasyStaffApi = mockk(relaxed = true)
    private val studyPlanRepository: StudyPlanRepository = mockk(relaxed = true)
    private val transcriptRepository: TranscriptRepository = mockk(relaxed = true)
    private val elearningCourseRepository: ElearningCourseRepository = mockk(relaxed = true)
    private val deviceIdentityStore: DeviceIdentityStore = mockk(relaxed = true)
    private val locationProvider: DeviceLocationProvider = mockk(relaxed = true)
    private val attendanceApi: ElearningAttendanceApi = mockk(relaxed = true)
    private val elearningApi: ElearningApi = mockk(relaxed = true) {
        every { attendance } returns attendanceApi
    }

    private val careerId = RepositoryTestFixtures.careerId

    private lateinit var repository: AttendanceRepositoryImpl

    @Before
    fun setUp() {
        repository = AttendanceRepositoryImpl(
            sessionManager = sessionManager,
            easyStaffApi = easyStaffApi,
            studyPlanRepository = studyPlanRepository,
            transcriptRepository = transcriptRepository,
            elearningCourseRepository = elearningCourseRepository,
            deviceIdentityStore = deviceIdentityStore,
            locationProvider = locationProvider,
        )
    }

    private fun module() = AttendanceModuleRef(courseId = 10, courseModuleId = 20, name = "Registro")

    @Test
    fun `registerPresence rejects an unrecognized scan`() = runTest {
        every { sessionManager.activeAccount } returns MutableStateFlow(RepositoryTestFixtures.account())

        val outcome = repository.registerPresence(PresenceScan.Unrecognized("garbage"), careerId)

        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Failed::class.java)
        assertThat((outcome.message as UiText.StringResource).resId)
            .isEqualTo(R.string.attendance_msg_code_unrecognized)
    }

    @Test
    fun `registerPresence with a lesson code fails when no matricola is available`() = runTest {
        every { sessionManager.activeAccount } returns MutableStateFlow(null)

        val outcome = repository.registerPresence(PresenceScan.LessonCode("L123"), careerId)

        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Failed::class.java)
        assertThat((outcome.message as UiText.StringResource).resId)
            .isEqualTo(R.string.attendance_msg_student_number_unavailable)
    }

    @Test
    fun `getOpenSessions short-circuits on an empty module list`() = runTest {
        val sessions = repository.getOpenSessions(emptyList())

        assertThat(sessions).isEmpty()
        coVerify(exactly = 0) { sessionManager.elearning() }
    }

    private val open = ElearningAttendanceSubmittableSession("77", "mer 18 nov 2026 2PM - 4PM")
    private val wrongNetwork = ElearningAttendanceSubmittableSession("78", "mer 18 nov 2026 4PM - 6PM")

    private fun described(session: ElearningAttendanceSubmittableSession, refusal: String? = null) =
        ElearningAttendanceMarkableSession(
            sessionId = session.sessionId,
            time = session.time,
            requiresPassword = false,
            statuses = emptyList(),
            refusal = refusal,
        )

    @Test
    fun `getOpenSessions describes the page's sessions and leaves out the refused ones`() = runTest {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery { attendanceApi.getAttendanceSummary("ws-token", 20) } returns
            ElearningAttendanceViewResponse.Success(summary = null, submittableSessions = listOf(open, wrongNetwork))
        coEvery { attendanceApi.getMarkableSessions("ws-token", 10, 20, any()) } returns
            listOf(described(open), described(wrongNetwork, refusal = "subnetwrong"))

        val sessions = repository.getOpenSessions(listOf(module()))

        assertThat(sessions.map { it.sessionId }).containsExactly("77")
        assertThat(sessions.single().time).isEqualTo(open.time)
        coVerify(exactly = 1) { attendanceApi.getMarkableSessions("ws-token", 10, 20, listOf(open, wrongNetwork)) }
        coVerify(exactly = 0) { sessionManager.reauthElearning(any()) }
    }

    @Test
    fun `getOpenSessions reuses a page it just read instead of loading it again`() = runTest {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery { attendanceApi.getAttendanceSummary("ws-token", 20) } returns
            ElearningAttendanceViewResponse.Success(summary = null, submittableSessions = listOf(open))
        coEvery { attendanceApi.getMarkableSessions(any(), any(), any(), any()) } returns listOf(described(open))

        repository.getOpenSessions(listOf(module()))
        repository.getOpenSessions(listOf(module()))

        coVerify(exactly = 1) { attendanceApi.getAttendanceSummary(any(), any()) }
    }

    @Test
    fun `an expired browser session is renewed once, through the client that saw it expire`() = runTest {
        val renewedAttendanceApi: ElearningAttendanceApi = mockk(relaxed = true)
        val renewedApi: ElearningApi = mockk(relaxed = true) { every { attendance } returns renewedAttendanceApi }
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery { sessionManager.reauthElearning(stale = elearningApi) } returns ElearningSession(renewedApi, "new-token")
        coEvery { attendanceApi.getAttendanceSummary("ws-token", 20) } returns ElearningAttendanceViewResponse.RequiresReauth
        coEvery { renewedAttendanceApi.getAttendanceSummary("new-token", 20) } returns
            ElearningAttendanceViewResponse.Success(summary = null, submittableSessions = emptyList())

        val sessions = repository.getOpenSessions(listOf(module()))

        assertThat(sessions).isEmpty()
        coVerify(exactly = 1) { sessionManager.reauthElearning(stale = elearningApi) }
        coVerify(exactly = 1) { attendanceApi.getAttendanceSummary(any(), any()) }
        coVerify(exactly = 1) { renewedAttendanceApi.getAttendanceSummary("new-token", 20) }
    }

    @Test
    fun `a page Moodle won't show is not treated as an expired session`() = runTest {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery { attendanceApi.getAttendanceSummary("ws-token", 20) } returns ElearningAttendanceViewResponse.Unavailable

        val sessions = repository.getOpenSessions(listOf(module()))

        assertThat(sessions).isEmpty()
        coVerify(exactly = 0) { sessionManager.reauthElearning(any()) }
    }

    @Test
    fun `markSession passes the session's time and maps an elearning failure to a Failed outcome`() = runTest {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery {
            attendanceApi.markSession(any(), any(), any(), any(), any(), any())
        } throws RuntimeException("boom")
        val session = OpenAttendanceSession(
            sessionId = "77",
            module = module(),
            time = open.time,
            requiresPassword = false,
            statuses = emptyList(),
        )

        val outcome = repository.markSession(session, statusId = null, password = null)

        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Failed::class.java)
        assertThat((outcome.message as UiText.StringResource).resId)
            .isEqualTo(R.string.attendance_msg_failed)
        coVerify { attendanceApi.markSession("ws-token", 10, 20, open, any(), null) }
    }
}
