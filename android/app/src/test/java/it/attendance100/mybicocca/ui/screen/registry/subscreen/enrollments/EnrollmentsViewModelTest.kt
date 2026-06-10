package it.attendance100.mybicocca.ui.screen.registry.subscreen.enrollments

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
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.model.enrollment.EnrollmentHistory
import it.attendance100.mybicocca.domain.model.enrollment.RenewalState
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.enrollment.GetEnrollmentHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.enrollment.GetRenewalWebUrlUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the Iscrizioni ViewModel: the live-first history load on the active
 * career, the Failed-without-clearing-data refresh contract, and the renew one-shot event
 * carrying the resolved Esse3 web URL.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EnrollmentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val getEnrollmentHistory: GetEnrollmentHistoryUseCase = mockk()
    private val getRenewalWebUrl: GetRenewalWebUrlUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): EnrollmentsViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return EnrollmentsViewModel(observeActiveAccount, getEnrollmentHistory, getRenewalWebUrl)
    }

    @Test
    fun `init loads the history and settles sync status Idle`() = runTest {
        val history = EnrollmentHistory(years = emptyList(), renewal = RenewalState.Renewable(2025))
        coEvery { getEnrollmentHistory(careerId) } returns history
        val vm = viewModel()

        val data = vm.history.value
        assertThat(data).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((data as Loadable.Loaded).value).isEqualTo(history)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        coVerify { getEnrollmentHistory(careerId) }
    }

    @Test
    fun `load failure sets Failed without clearing already-loaded history`() = runTest {
        val history = EnrollmentHistory(years = emptyList(), renewal = RenewalState.Enrolled(2025))
        coEvery { getEnrollmentHistory(careerId) } returns history
        val vm = viewModel()

        val boom = IOException("offline")
        coEvery { getEnrollmentHistory(careerId) } throws boom
        vm.refresh()

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat((vm.history.value as Loadable.Loaded).value).isEqualTo(history)
    }

    @Test
    fun `renew resolves the web url and emits OpenRenewalWeb exactly once`() = runTest {
        coEvery { getEnrollmentHistory(careerId) } returns
            EnrollmentHistory(years = emptyList(), renewal = RenewalState.Renewable(2025))
        every { getRenewalWebUrl(careerId) } returns "https://s3w.si.unimib.it/renew"
        val vm = viewModel()

        vm.events.test {
            vm.renew()
            val event = awaitItem()
            assertThat(event).isEqualTo(EnrollmentEvent.OpenRenewalWeb("https://s3w.si.unimib.it/renew"))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        verify { getRenewalWebUrl(careerId) }
    }

    @Test
    fun `refresh after a failure recovers sync status to Idle`() = runTest {
        coEvery { getEnrollmentHistory(careerId) } throws IOException("offline")
        val vm = viewModel()
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)

        coEvery { getEnrollmentHistory(careerId) } returns
            EnrollmentHistory(years = emptyList(), renewal = RenewalState.NotApplicable)
        vm.refresh()

        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

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
