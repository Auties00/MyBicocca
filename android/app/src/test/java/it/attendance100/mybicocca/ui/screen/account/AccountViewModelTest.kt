package it.attendance100.mybicocca.ui.screen.account

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.career.CareerStatus
import it.attendance100.mybicocca.domain.usecase.account.GetUserPhotoUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveAccountEventsUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveAccountsUseCase
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.account.SignOutUseCase
import it.attendance100.mybicocca.domain.usecase.account.SwitchAccountUseCase
import it.attendance100.mybicocca.domain.usecase.career.PickCareerUseCase
import it.attendance100.mybicocca.domain.usecase.career.SwitchCareerUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.account.state.AccountEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import it.attendance100.mybicocca.domain.model.account.AccountEvent as DomainAccountEvent

/**
 * Coverage for the account switcher ViewModel: derived streams for accounts, careers and the
 * selected career; one-shot Switched and SignedOut events; the combined select-account-career
 * action that activates the owning account first; and the forwarding of domain reconciliation
 * events.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")
    private val otherId = AccountId("acc-2")
    private val careerA = CareerId(101L)
    private val careerB = CareerId(102L)

    private fun career(id: CareerId, description: String) = Career(
        id = id,
        enrollmentTraitId = 1L,
        programId = 2L,
        easyStaffProgramCode = "E32",
        academicYearEnrollmentId = 3L,
        studentNumber = "900001",
        description = description,
        academicYear = 2024,
        status = CareerStatus.ACTIVE,
    )

    private fun account(
        id: AccountId,
        careers: List<Career>,
        selected: CareerId,
    ) = Account(
        id = id,
        username = "user-${id.value}",
        displayName = "User ${id.value}",
        academic = AcademicIdentity(
            recordUserId = "rec-${id.value}",
            personId = 7L,
            fiscalCode = null,
            careers = careers,
            selectedCareerId = selected,
        ),
        learning = LearningIdentity(
            lmsUserId = 11,
            lmsUsername = "user-${id.value}",
            locale = "it",
            isSiteAdmin = false,
            maxUploadFileSizeBytes = 0L,
            storageQuotaBytes = 0L,
        ),
        createdAt = Instant.EPOCH,
        lastUsedAt = Instant.EPOCH,
        lastSyncedAt = Instant.EPOCH,
    )

    private val active = account(accountId, listOf(career(careerA, "Informatica"), career(careerB, "Matematica")), careerA)
    private val other = account(otherId, listOf(career(careerA, "Fisica")), careerA)

    private val observeAccounts: ObserveAccountsUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val observeEvents: ObserveAccountEventsUseCase = mockk()
    private val switchAccountUseCase: SwitchAccountUseCase = mockk(relaxed = true)
    private val switchCareerUseCase: SwitchCareerUseCase = mockk(relaxed = true)
    private val pickCareerUseCase: PickCareerUseCase = mockk(relaxed = true)
    private val signOutUseCase: SignOutUseCase = mockk(relaxed = true)
    private val getUserPhotoUseCase: GetUserPhotoUseCase = mockk()

    private fun viewModel(
        accounts: List<Account> = listOf(active, other),
        activeAccount: Account? = active,
        domainEvents: kotlinx.coroutines.flow.Flow<DomainAccountEvent> = emptyFlow(),
    ): AccountViewModel {
        every { observeAccounts() } returns flowOf(accounts)
        every { observeActiveAccount() } returns flowOf(activeAccount)
        every { observeEvents() } returns domainEvents
        coEvery { getUserPhotoUseCase(any()) } returns "/tmp/photo.jpg"
        return AccountViewModel(
            observeAccounts,
            observeActiveAccount,
            observeEvents,
            switchAccountUseCase,
            switchCareerUseCase,
            pickCareerUseCase,
            signOutUseCase,
            getUserPhotoUseCase,
        )
    }

    @Test
    fun `accounts and careers and selectedCareerId derive from the streams`() = runTest {
        val vm = viewModel()

        assertThat(vm.activeAccount.value).isEqualTo(active)
        assertThat(vm.selectedCareerId.value).isEqualTo(careerA)
        vm.careers.test {
            assertThat(awaitItem()).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
        vm.accounts.test {
            assertThat(awaitItem()).containsExactly(active, other)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `switchAccount emits Switched when the active account becomes the target`() = runTest {
        val vm = viewModel()

        vm.events.test {
            vm.switchAccount(accountId)
            val event = awaitItem()
            assertThat(event).isInstanceOf(AccountEvent.Switched::class.java)
            assertThat((event as AccountEvent.Switched).account).isEqualTo(active)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { switchAccountUseCase(accountId) }
    }

    @Test
    fun `switchCareer delegates to the use case`() = runTest {
        val vm = viewModel()

        vm.switchCareer(accountId, careerB)

        coVerify { switchCareerUseCase(accountId, careerB) }
    }

    @Test
    fun `pickCareer delegates to the use case`() = runTest {
        val vm = viewModel()

        vm.pickCareer(accountId, careerA)

        coVerify { pickCareerUseCase(accountId, careerA) }
    }

    @Test
    fun `signOut emits SignedOut on success`() = runTest {
        val vm = viewModel()

        vm.events.test {
            vm.signOut(otherId)
            val event = awaitItem()
            assertThat(event).isInstanceOf(AccountEvent.SignedOut::class.java)
            assertThat((event as AccountEvent.SignedOut).accountId).isEqualTo(otherId)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { signOutUseCase(otherId) }
    }

    @Test
    fun `signOut emits SignOutFailed when the use case throws`() = runTest {
        coEvery { signOutUseCase(any()) } throws IllegalStateException("boom")
        val vm = viewModel()

        vm.events.test {
            vm.signOut(otherId)
            val event = awaitItem()
            assertThat(event).isInstanceOf(AccountEvent.SignOutFailed::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectAccountCareer activates a non-active account before switching career`() = runTest {
        val vm = viewModel()

        vm.selectAccountCareer(otherId, careerA)

        coVerify { switchAccountUseCase(otherId) }
    }

    @Test
    fun `selectAccountCareer skips the switch when the career already matches the active account`() = runTest {
        val vm = viewModel()

        vm.selectAccountCareer(accountId, careerA)

        coVerify(exactly = 0) { switchAccountUseCase(any()) }
        coVerify(exactly = 0) { switchCareerUseCase(any(), any()) }
    }

    @Test
    fun `selectAccountCareer switches career when it differs on the active account`() = runTest {
        val vm = viewModel()

        vm.selectAccountCareer(accountId, careerB)

        coVerify(exactly = 0) { switchAccountUseCase(any()) }
        coVerify { switchCareerUseCase(accountId, careerB) }
    }

    @Test
    fun `selectAccountCareer is a no-op for an unknown account`() = runTest {
        val vm = viewModel()

        vm.selectAccountCareer(AccountId("ghost"), careerA)

        coVerify(exactly = 0) { switchAccountUseCase(any()) }
        coVerify(exactly = 0) { switchCareerUseCase(any(), any()) }
    }

    @Test
    fun `domain reconciliation events are mapped and forwarded`() = runTest {
        val domainEvent = DomainAccountEvent.SelectedCareerMissing(accountId)
        val vm = viewModel(domainEvents = flowOf(domainEvent))

        vm.events.test {
            val event = awaitItem()
            assertThat(event).isInstanceOf(AccountEvent.SelectedCareerMissing::class.java)
            assertThat((event as AccountEvent.SelectedCareerMissing).accountId).isEqualTo(accountId)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
