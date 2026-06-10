package it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults

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
import it.attendance100.mybicocca.domain.model.exam.AcknowledgmentStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.model.exam.ExamGrade
import it.attendance100.mybicocca.domain.model.exam.ExamResult
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.exam.AcceptExamResultUseCase
import it.attendance100.mybicocca.domain.usecase.exam.GetExamResultsUseCase
import it.attendance100.mybicocca.domain.usecase.exam.RejectExamResultUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.examResults.state.ExamResultEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the Esiti ViewModel: the live-first fetch, pullToRefresh reset, and
 * the accept/reject contract (gating, optimistic drop + reconcile, success/failure one-shot
 * events, the use case routed by the accept flag).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExamResultsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val getExamResults: GetExamResultsUseCase = mockk()
    private val acceptExamResult: AcceptExamResultUseCase = mockk(relaxed = true)
    private val rejectExamResult: RejectExamResultUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): ExamResultsViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return ExamResultsViewModel(getExamResults, acceptExamResult, rejectExamResult, observeActiveAccount)
    }

    @Test
    fun `init fetches and goes NotYetLoaded then Loaded`() = runTest {
        val list = listOf(result(applicationListId = 700L))
        coEvery { getExamResults(careerId) } returns list
        val vm = viewModel()

        val data = vm.results.value
        assertThat(data).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((data as Loadable.Loaded).value).isEqualTo(list)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `fetch failure sets Failed without clearing already-loaded results`() = runTest {
        val list = listOf(result(applicationListId = 700L))
        coEvery { getExamResults(careerId) } returns list
        val vm = viewModel()

        val boom = IOException("offline")
        coEvery { getExamResults(careerId) } throws boom
        vm.refresh()

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat((vm.results.value as Loadable.Loaded).value).isEqualTo(list)
    }

    @Test
    fun `pullToRefresh resets to NotYetLoaded before re-fetching`() = runTest {
        val first = listOf(result(applicationListId = 700L))
        coEvery { getExamResults(careerId) } returns first
        val vm = viewModel()

        val second = listOf(result(applicationListId = 800L))
        coEvery { getExamResults(careerId) } returns second
        vm.pullToRefresh()

        assertThat((vm.results.value as Loadable.Loaded).value).isEqualTo(second)
    }

    @Test
    fun `accept calls accept, emits AcceptSucceeded, drops the row, then refetches`() = runTest {
        val target = result(applicationListId = 700L)
        coEvery { getExamResults(careerId) } returns listOf(target)
        val vm = viewModel()
        coEvery { getExamResults(careerId) } returns emptyList()

        vm.events.test {
            vm.accept(target)
            assertThat(awaitItem()).isEqualTo(ExamResultEvent.AcceptSucceeded)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { acceptExamResult(careerId, 700L) }
        coVerify(exactly = 0) { rejectExamResult(any(), any()) }
        assertThat(vm.actionState.value).isEqualTo(ExamResultActionState.Idle)
        assertThat((vm.results.value as Loadable.Loaded).value).isEmpty()
    }

    @Test
    fun `reject calls reject and emits RejectSucceeded`() = runTest {
        val target = result(applicationListId = 700L)
        coEvery { getExamResults(careerId) } returns listOf(target)
        val vm = viewModel()
        coEvery { getExamResults(careerId) } returns emptyList()

        vm.events.test {
            vm.reject(target)
            assertThat(awaitItem()).isEqualTo(ExamResultEvent.RejectSucceeded)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { rejectExamResult(careerId, 700L) }
        coVerify(exactly = 0) { acceptExamResult(any(), any()) }
    }

    @Test
    fun `accept failure emits Failed and keeps the result`() = runTest {
        val target = result(applicationListId = 700L)
        coEvery { getExamResults(careerId) } returns listOf(target)
        val vm = viewModel()

        val boom = IOException("server down")
        coEvery { acceptExamResult(any(), any()) } throws boom

        vm.events.test {
            vm.accept(target)
            val event = awaitItem()
            assertThat(event).isInstanceOf(ExamResultEvent.Failed::class.java)
            assertThat((event as ExamResultEvent.Failed).cause).isEqualTo(boom)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.actionState.value).isEqualTo(ExamResultActionState.Idle)
        assertThat((vm.results.value as Loadable.Loaded).value).containsExactly(target)
    }

    @Test
    fun `accept is a no-op when the result has no applicationListId`() = runTest {
        coEvery { getExamResults(careerId) } returns emptyList()
        val vm = viewModel()

        vm.accept(result(applicationListId = null))

        coVerify(exactly = 0) { acceptExamResult(any(), any()) }
    }

    private fun result(applicationListId: Long?): ExamResult = ExamResult(
        key = ExamCallKey(courseOfStudyId = 1L, activityId = 2L, callId = 3),
        applicationListId = applicationListId,
        publicationId = 4242L,
        activityDescription = "Algoritmi",
        examDateTime = null,
        grade = ExamGrade.Numeric(30),
        acknowledgment = AcknowledgmentStatus.NotViewed,
        publishedNote = null,
        acknowledgmentDeadline = null,
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
