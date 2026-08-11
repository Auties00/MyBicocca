package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ConsumePresenceScanUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.GetPendingAttendanceCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.MarkPresenceUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ObservePendingPresenceScanUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ParsePresenceScanUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state.AttendanceEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state.MarkUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the Presenze sheet ViewModel: the career-triggered course
 * fetch, the mark-submission lifecycle (Idle -> Submitting -> Done), the recorded-outcome
 * re-refresh, the no-career guard, and the externally-scanned QR pipeline that opens the
 * rileva sheet and submits.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val getPendingCourses: GetPendingAttendanceCoursesUseCase = mockk()
    private val markPresence: MarkPresenceUseCase = mockk()
    private val parseScan: ParsePresenceScanUseCase = mockk()
    private val observePendingPresenceScan: ObservePendingPresenceScanUseCase = mockk()
    private val consumePresenceScan: ConsumePresenceScanUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun course(name: String): CourseAttendance = CourseAttendance(
        name = name,
        code = "E3101Q123",
        year = StudyYear(1),
        semester = Semester.First,
        credits = 6f,
        teacherName = null,
        classroomAttendance = null,
        sessionAttendance = emptyList(),
    )

    private fun viewModel(
        scanFlow: MutableStateFlow<String?> = MutableStateFlow(null),
        active: Account? = account(careerId),
    ): AttendanceViewModel {
        every { observeActiveAccount() } returns flowOf(active)
        every { observePendingPresenceScan() } returns scanFlow
        coEvery { getPendingCourses(any()) } returns emptyList()
        return AttendanceViewModel(
            getPendingCourses = getPendingCourses,
            markPresence = markPresence,
            parseScan = parseScan,
            observePendingPresenceScan = observePendingPresenceScan,
            consumePresenceScan = consumePresenceScan,
            observeActiveAccount = observeActiveAccount,
        )
    }

    @Test
    fun `courses goes NotYetLoaded then Loaded on the career-triggered fetch`() = runTest {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        every { observePendingPresenceScan() } returns MutableStateFlow(null)
        coEvery { getPendingCourses(careerId) } returns listOf(course("Algoritmi"))

        val vm = AttendanceViewModel(
            getPendingCourses, markPresence, parseScan,
            observePendingPresenceScan, consumePresenceScan, observeActiveAccount,
        )

        assertThat(vm.courses.value).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((vm.courses.value as Loadable.Loaded).value).hasSize(1)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `fetch failure sets Failed without populating courses`() = runTest {
        val boom = IOException("offline")
        every { observeActiveAccount() } returns flowOf(account(careerId))
        every { observePendingPresenceScan() } returns MutableStateFlow(null)
        coEvery { getPendingCourses(careerId) } throws boom

        val vm = AttendanceViewModel(
            getPendingCourses, markPresence, parseScan,
            observePendingPresenceScan, consumePresenceScan, observeActiveAccount,
        )

        assertThat(vm.courses.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat((vm.syncStatus.value as SyncStatus.Failed).cause).isEqualTo(boom)
    }

    @Test
    fun `submitScan moves markState Idle to Done with the outcome`() = runTest {
        val recorded = PresenceMarkOutcome.Recorded(statusDescription = null)
        val scan = PresenceScan.LessonCode("ABC")
        every { parseScan("ABC") } returns scan
        coEvery { markPresence(scan, careerId) } returns recorded
        coEvery { getPendingCourses(careerId) } returns emptyList()
        val vm = viewModel()

        vm.submitScan("ABC")

        val state = vm.markState.value
        assertThat(state).isInstanceOf(MarkUiState.Done::class.java)
        assertThat((state as MarkUiState.Done).outcome).isEqualTo(recorded)
    }

    @Test
    fun `recorded outcome triggers an extra course refresh`() = runTest {
        val scan = PresenceScan.LessonCode("ABC")
        every { parseScan("ABC") } returns scan
        coEvery { markPresence(scan, careerId) } returns PresenceMarkOutcome.Recorded(
            statusDescription = "ok"
        )
        coEvery { getPendingCourses(careerId) } returns emptyList()
        val vm = viewModel()

        vm.submitScan("ABC")

        coVerify(atLeast = 2) { getPendingCourses(careerId) }
    }

    @Test
    fun `failed outcome does not trigger a course refresh`() = runTest {
        val scan = PresenceScan.LessonCode("ABC")
        every { parseScan("ABC") } returns scan
        coEvery { markPresence(scan, careerId) } returns PresenceMarkOutcome.WrongCredential()
        coEvery { getPendingCourses(careerId) } returns emptyList()
        val vm = viewModel()

        vm.submitScan("ABC")

        coVerify(exactly = 1) { getPendingCourses(careerId) }
    }

    @Test
    fun `submitScan with no active career reports a failed outcome and never marks`() = runTest {
        val vm = viewModel(active = null)

        vm.submitScan("ABC")

        val state = vm.markState.value
        assertThat(state).isInstanceOf(MarkUiState.Done::class.java)
        assertThat((state as MarkUiState.Done).outcome).isInstanceOf(PresenceMarkOutcome.Failed::class.java)
        coVerify(exactly = 0) { markPresence(any(), any()) }
    }

    @Test
    fun `resetMarkState returns the mark flow to Idle`() = runTest {
        val scan = PresenceScan.LessonCode("ABC")
        every { parseScan("ABC") } returns scan
        coEvery { markPresence(scan, careerId) } returns PresenceMarkOutcome.NotOpen()
        coEvery { getPendingCourses(careerId) } returns emptyList()
        val vm = viewModel()
        vm.submitScan("ABC")

        vm.resetMarkState()

        assertThat(vm.markState.value).isEqualTo(MarkUiState.Idle)
    }

    @Test
    fun `an external QR scan opens the rileva sheet, consumes the scan, and submits it`() = runTest {
        val scanFlow = MutableStateFlow<String?>(null)
        val parsed = PresenceScan.SessionLink("99", "qr")
        every { parseScan("raw-link") } returns parsed
        coEvery { markPresence(parsed, careerId) } returns PresenceMarkOutcome.Recorded(
            statusDescription = "ok"
        )
        coEvery { getPendingCourses(careerId) } returns emptyList()
        val vm = viewModel(scanFlow = scanFlow)

        vm.events.test {
            scanFlow.value = "raw-link"
            assertThat(awaitItem()).isEqualTo(AttendanceEvent.OpenRilevaSheet)
            cancelAndIgnoreRemainingEvents()
        }

        verify { consumePresenceScan() }
        coVerify { markPresence(parsed, careerId) }
    }

    private fun account(careerId: CareerId): Account = Account(
        id = AccountId("acc-1"),
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "u1",
            personId = 7L,
            fiscalCode = null,
            careers = listOf(
                Career(
                    id = careerId,
                    enrollmentTraitId = 1L,
                    programId = 2L,
                    easyStaffProgramCode = "E32",
                    academicYearEnrollmentId = 3L,
                    studentNumber = "900001",
                    description = "Informatica",
                    academicYear = 2024,
                    status = CareerStatus.ACTIVE,
                ),
            ),
            selectedCareerId = careerId,
        ),
        learning = LearningIdentity(
            lmsUserId = 11,
            lmsUsername = "mario.rossi@campus.unimib.it",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )
}
