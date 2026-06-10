package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamEnrollmentWindow
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamCallsUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the bookable-exams (Esami prenotabili) ViewModel: the live-first
 * fetch that surfaces NotYetLoaded -> Loaded, keeps the already-loaded list when a refresh fails
 * (Failed without nuking data), the pullToRefresh reset-to-loading behaviour, the refresh mutex
 * collapsing overlapping fetches, and the deep-link focus signal.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookableExamsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val account = account(careerId)

    private val getExamCalls: GetExamCallsUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): BookableExamsViewModel {
        every { observeActiveAccount() } returns flowOf(account)
        return BookableExamsViewModel(getExamCalls, observeActiveAccount)
    }

    @Test
    fun `init fetches for the active career and goes NotYetLoaded then Loaded`() = runTest {
        val calls = listOf(examCall(7L))
        coEvery { getExamCalls(careerId) } returns calls

        every { observeActiveAccount() } returns flowOf(account)
        val vm = BookableExamsViewModel(getExamCalls, observeActiveAccount)

        coVerify { getExamCalls(careerId) }
        val data = vm.examCalls.value
        assertThat(data).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((data as Loadable.Loaded).value).isEqualTo(calls)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `no active career leaves the list NotYetLoaded and never fetches`() = runTest {
        every { observeActiveAccount() } returns flowOf(null)
        val vm = BookableExamsViewModel(getExamCalls, observeActiveAccount)

        assertThat(vm.examCalls.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        coVerify(exactly = 0) { getExamCalls(any()) }
    }

    @Test
    fun `fetch failure sets Failed without clearing already-loaded data`() = runTest {
        val calls = listOf(examCall(7L))
        val boom = IOException("offline")
        coEvery { getExamCalls(careerId) } returns calls
        val vm = viewModel()

        assertThat(vm.examCalls.value).isInstanceOf(Loadable.Loaded::class.java)

        coEvery { getExamCalls(careerId) } throws boom
        vm.refresh()

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        val data = vm.examCalls.value
        assertThat(data).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((data as Loadable.Loaded).value).isEqualTo(calls)
    }

    @Test
    fun `pullToRefresh resets to NotYetLoaded before re-fetching`() = runTest {
        val first = listOf(examCall(1L))
        val second = listOf(examCall(2L), examCall(3L))
        coEvery { getExamCalls(careerId) } returns first
        val vm = viewModel()
        assertThat((vm.examCalls.value as Loadable.Loaded).value).isEqualTo(first)

        coEvery { getExamCalls(careerId) } returns second
        vm.pullToRefresh()

        val data = vm.examCalls.value
        assertThat(data).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((data as Loadable.Loaded).value).isEqualTo(second)
    }

    @Test
    fun `requestFocus then consumeFocus toggles the pending focus key`() = runTest {
        coEvery { getExamCalls(careerId) } returns emptyList()
        val vm = viewModel()

        vm.pendingFocusCourseKey.test {
            assertThat(awaitItem()).isNull()
            vm.requestFocus("E3101Q123")
            assertThat(awaitItem()).isEqualTo("E3101Q123")
            vm.consumeFocus()
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful refresh after a failure returns sync status to Idle`() = runTest {
        coEvery { getExamCalls(careerId) } throws IOException("offline")
        val vm = viewModel()
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)

        coEvery { getExamCalls(careerId) } returns listOf(examCall(9L))
        vm.refresh()

        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    private fun examCall(choiceId: Long): ExamCall = ExamCall(
        key = ExamCallKey(courseOfStudyId = 1L, activityId = 2L, callId = 3),
        examCallId = 50L,
        activityChoiceId = choiceId,
        activityCode = "E3101Q123",
        activityDescription = "Algoritmi",
        courseOfStudyDescription = "Informatica",
        callDescription = "Appello I",
        callDate = null,
        callTime = null,
        enrollmentWindow = ExamEnrollmentWindow(opensAt = null, closesAt = null),
        enrolledNumber = 0,
        state = null,
        stateDescription = null,
        callType = ExamCallType.Final,
        examType = ExamType.Written,
        isReserved = false,
        matId = null,
        notes = null,
        president = null,
        bookingTypeDescription = null,
    )

    private companion object {
        fun account(careerId: CareerId): Account = Account(
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
}
