package it.attendance100.mybicocca.ui.navigation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.career.PickCareerUseCase
import it.attendance100.mybicocca.domain.usecase.connectivity.ObserveConnectivityUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.navigation.route.RootPhase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/**
 * Coverage for the root session-phase resolver: a null active account routes to
 * Authenticating, a signed-in account to SignedIn, a pending career pick to NeedsCareerPick,
 * the connectivity flag streams through, and onCareerPicked persists the choice then releases
 * the gate.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")
    private val careerId = CareerId(101L)

    private val account = Account(
        id = accountId,
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "u1",
            personId = 7L,
            fiscalCode = null,
            careers = emptyList(),
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

    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val pickCareerUseCase: PickCareerUseCase = mockk(relaxed = true)
    private val observeConnectivity: ObserveConnectivityUseCase = mockk()

    private fun viewModel(
        activeAccount: Account? = account,
        online: Boolean = true,
    ): RootViewModel {
        every { observeActiveAccount() } returns flowOf(activeAccount)
        every { observeConnectivity() } returns flowOf(online)
        return RootViewModel(observeActiveAccount, pickCareerUseCase, observeConnectivity)
    }

    @Test
    fun `null active account resolves to Authenticating`() = runTest {
        val vm = viewModel(activeAccount = null)

        vm.phase.test {
            assertThat(awaitItem()).isEqualTo(RootPhase.Authenticating)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a signed-in account resolves to SignedIn`() = runTest {
        val vm = viewModel()

        vm.phase.test {
            assertThat(awaitItem()).isEqualTo(RootPhase.SignedIn(account))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSignedIn with requiresPick holds the phase at NeedsCareerPick`() = runTest {
        val vm = viewModel()

        vm.onSignedIn(accountId, requiresPick = true)

        vm.phase.test {
            assertThat(awaitItem()).isEqualTo(RootPhase.NeedsCareerPick(account))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSignedIn without a pick stays SignedIn`() = runTest {
        val vm = viewModel()

        vm.onSignedIn(accountId, requiresPick = false)

        vm.phase.test {
            assertThat(awaitItem()).isEqualTo(RootPhase.SignedIn(account))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCareerPicked persists the career and releases the pending pick gate`() = runTest {
        val vm = viewModel()
        vm.onSignedIn(accountId, requiresPick = true)

        vm.onCareerPicked(accountId, careerId)

        coVerify { pickCareerUseCase(accountId, careerId) }
        vm.phase.test {
            assertThat(awaitItem()).isEqualTo(RootPhase.SignedIn(account))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isOnline streams the connectivity flag`() = runTest {
        val vm = viewModel(online = false)

        vm.isOnline.test {
            assertThat(awaitItem()).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
