package it.attendance100.mybicocca.ui.screen.lock

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveAppLockUseCase
import it.attendance100.mybicocca.domain.usecase.security.UnlockAppUseCase
import it.attendance100.mybicocca.domain.usecase.security.VerifyAppLockPasswordUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/**
 * Coverage for the app-lock gate ViewModel: locked and username mirror the domain streams,
 * a biometric success unlocks directly, password verification unlocks only on Success and
 * reports the outcome to the caller, and re-entrant password checks are dropped.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppLockViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeAppLock: ObserveAppLockUseCase = mockk()
    private val unlockApp: UnlockAppUseCase = mockk(relaxed = true)
    private val verifyAppLockPassword: VerifyAppLockPasswordUseCase = mockk()
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()

    private val account = Account(
        id = AccountId("acc-1"),
        username = "mario.rossi@campus.unimib.it",
        displayName = "Mario Rossi",
        academic = AcademicIdentity(
            recordUserId = "u1",
            personId = 7L,
            fiscalCode = null,
            careers = emptyList(),
            selectedCareerId = CareerId(101L),
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

    private fun viewModel(
        locked: Boolean = true,
        activeAccount: Account? = account,
    ): AppLockViewModel {
        every { observeAppLock() } returns flowOf(locked)
        every { observeActiveAccount() } returns flowOf(activeAccount)
        return AppLockViewModel(observeAppLock, unlockApp, verifyAppLockPassword, observeActiveAccount)
    }

    @Test
    fun `locked mirrors the domain lock stream`() = runTest {
        val vm = viewModel(locked = true)

        assertThat(vm.locked.value).isTrue()
    }

    @Test
    fun `username labels the lock screen with the active account`() = runTest {
        val vm = viewModel()

        assertThat(vm.username.value).isEqualTo("mario.rossi@campus.unimib.it")
    }

    @Test
    fun `username is null when no account is active`() = runTest {
        val vm = viewModel(activeAccount = null)

        assertThat(vm.username.value).isNull()
    }

    @Test
    fun `onBiometricSuccess unlocks directly`() = runTest {
        val vm = viewModel()

        vm.onBiometricSuccess()

        verify { unlockApp() }
    }

    @Test
    fun `verifyPassword unlocks and reports Success`() = runTest {
        coEvery { verifyAppLockPassword("secret") } returns UnlockResult.Success
        val vm = viewModel()
        var reported: UnlockResult? = null

        vm.verifyPassword("secret") { reported = it }

        verify { unlockApp() }
        assertThat(reported).isEqualTo(UnlockResult.Success)
        assertThat(vm.verifying.value).isFalse()
    }

    @Test
    fun `verifyPassword on wrong password does not unlock but reports WrongPassword`() = runTest {
        coEvery { verifyAppLockPassword(any()) } returns UnlockResult.WrongPassword
        val vm = viewModel()
        var reported: UnlockResult? = null

        vm.verifyPassword("nope") { reported = it }

        verify(exactly = 0) { unlockApp() }
        assertThat(reported).isEqualTo(UnlockResult.WrongPassword)
    }

    @Test
    fun `verifyPassword reports Error when there is nothing to verify against`() = runTest {
        coEvery { verifyAppLockPassword(any()) } returns UnlockResult.Error
        val vm = viewModel()
        var reported: UnlockResult? = null

        vm.verifyPassword("x") { reported = it }

        verify(exactly = 0) { unlockApp() }
        assertThat(reported).isEqualTo(UnlockResult.Error)
    }

    @Test
    fun `re-entrant verifyPassword while a check is in flight is dropped`() = runTest {
        val gate = CompletableDeferred<UnlockResult>()
        coEvery { verifyAppLockPassword(any()) } coAnswers { gate.await() }
        val vm = viewModel()
        var callbacks = 0

        vm.verifyPassword("a") { callbacks++ }
        assertThat(vm.verifying.value).isTrue()
        vm.verifyPassword("b") { callbacks++ }

        gate.complete(UnlockResult.WrongPassword)
        assertThat(callbacks).isEqualTo(1)
        assertThat(vm.verifying.value).isFalse()
    }
}
