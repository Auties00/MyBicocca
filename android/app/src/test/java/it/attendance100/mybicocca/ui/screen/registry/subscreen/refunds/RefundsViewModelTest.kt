package it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds

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
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.tax.GetRefundsUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * State-machine coverage for the Rimborsi ViewModel: the live-first fetch with the newest
 * academic-year-first sort (null years sink to the bottom), the Failed-without-clearing-data
 * contract, and recovery to Idle.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RefundsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val careerId = CareerId(101L)

    private val getRefunds: GetRefundsUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private fun viewModel(): RefundsViewModel {
        every { observeActiveAccount() } returns flowOf(account(careerId))
        return RefundsViewModel(getRefunds, observeActiveAccount)
    }

    @Test
    fun `init fetches and sorts refunds newest academic year first with nulls last`() = runTest {
        val older = refund(year = 2021)
        val newer = refund(year = 2024)
        val undated = refund(year = null)
        coEvery { getRefunds(careerId) } returns listOf(older, undated, newer)
        val vm = viewModel()

        val data = vm.refunds.value
        assertThat(data).isInstanceOf(Loadable.Loaded::class.java)
        assertThat((data as Loadable.Loaded).value).containsExactly(newer, older, undated).inOrder()
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        coVerify { getRefunds(careerId) }
    }

    @Test
    fun `no active career leaves the list NotYetLoaded and never fetches`() = runTest {
        every { observeActiveAccount() } returns flowOf(null)
        val vm = RefundsViewModel(getRefunds, observeActiveAccount)

        assertThat(vm.refunds.value).isEqualTo(Loadable.NotYetLoaded)
        coVerify(exactly = 0) { getRefunds(any()) }
    }

    @Test
    fun `fetch failure sets Failed without clearing already-loaded refunds`() = runTest {
        coEvery { getRefunds(careerId) } returns listOf(refund(year = 2024))
        val vm = viewModel()

        val boom = IOException("offline")
        coEvery { getRefunds(careerId) } throws boom
        vm.refresh()

        val status = vm.syncStatus.value
        assertThat(status).isInstanceOf(SyncStatus.Failed::class.java)
        assertThat((status as SyncStatus.Failed).cause).isEqualTo(boom)
        assertThat((vm.refunds.value as Loadable.Loaded).value).hasSize(1)
    }

    @Test
    fun `successful refresh after a failure returns sync status to Idle`() = runTest {
        coEvery { getRefunds(careerId) } throws IOException("offline")
        val vm = viewModel()
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)

        coEvery { getRefunds(careerId) } returns emptyList()
        vm.refresh()

        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    private fun refund(year: Int?): Refund = Refund(
        invoiceId = null,
        academicYear = year,
        amount = 100.0,
        description = "Rimborso",
        reasonCode = null,
        mandateNumber = null,
        refunded = false,
        note = null,
        collectedBy = null,
        issueDate = null,
        processingDate = null,
        paymentDate = null,
        creditDate = null,
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
