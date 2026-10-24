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
import it.attendance100.mybicocca.data.elearning.ElearningApi
import it.attendance100.mybicocca.data.elearning.ElearningAttendance
import it.attendance100.mybicocca.data.elearning.ElearningAttendanceMarkableSession
import it.attendance100.mybicocca.data.elearning.ElearningAttendanceViewResponse
import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
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
    private val attendanceApi: ElearningAttendance = mockk(relaxed = true)
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

    @Test
    fun `getOpenSessions describes only the sessions the module page offers`() = runTest {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery { attendanceApi.getAttendanceSummary(20) } returns
            ElearningAttendanceViewResponse.Success(summary = null, submittableSessionIds = listOf("77"))
        coEvery { attendanceApi.getMarkableSessions("ws-token", 10, 20, listOf("77")) } returns
            listOf(ElearningAttendanceMarkableSession(sessionId = "77", requiresPassword = false, statuses = emptyList()))

        val sessions = repository.getOpenSessions(listOf(module()))

        assertThat(sessions.map { it.sessionId }).containsExactly("77")
        coVerify(exactly = 0) { sessionManager.reauthElearning() }
    }

    @Test
    fun `getOpenSessions renews an expired browser session once`() = runTest {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery { sessionManager.reauthElearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery { attendanceApi.getAttendanceSummary(20) } returnsMany listOf(
            ElearningAttendanceViewResponse.RequiresReauth,
            ElearningAttendanceViewResponse.Success(summary = null, submittableSessionIds = emptyList()),
        )

        val sessions = repository.getOpenSessions(listOf(module()))

        assertThat(sessions).isEmpty()
        coVerify(exactly = 1) { sessionManager.reauthElearning() }
        coVerify(exactly = 0) { attendanceApi.getMarkableSessions(any(), any(), any(), any()) }
    }

    @Test
    fun `markSession maps an elearning failure to a Failed outcome`() = runTest {
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, "ws-token")
        coEvery {
            attendanceApi.markSession(any(), any(), any(), any(), any(), any())
        } throws RuntimeException("boom")

        val outcome = repository.markSession(module(), sessionId = "s1", statusId = null, password = null)

        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Failed::class.java)
        assertThat((outcome.message as UiText.StringResource).resId)
            .isEqualTo(R.string.attendance_msg_failed)
    }
}
