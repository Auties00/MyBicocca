package it.attendance100.mybicocca.ui.screen.settings.subscreen.settingsSecurity

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.domain.usecase.privacy.ObserveCrashReportingEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.privacy.SetCrashReportingEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveAppLockEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveLockTimeoutUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveSecureScreenUseCase
import it.attendance100.mybicocca.domain.usecase.security.SetAppLockEnabledUseCase
import it.attendance100.mybicocca.domain.usecase.security.SetLockTimeoutUseCase
import it.attendance100.mybicocca.domain.usecase.security.SetSecureScreenUseCase
import it.attendance100.mybicocca.domain.usecase.security.UnlockAppUseCase
import it.attendance100.mybicocca.domain.usecase.security.VerifyAppLockPasswordUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Coverage for the security settings ViewModel: the three persisted preference streams (master
 * toggle, timeout, secure screen) are mirrored, enabling the lock also unlocks so the user is not
 * re-challenged, setters forward to persistence, and password verification toggles [verifying],
 * reports the outcome and drops re-entrant attempts.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsSecurityViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeAppLockEnabled: ObserveAppLockEnabledUseCase = mockk()
    private val observeLockTimeout: ObserveLockTimeoutUseCase = mockk()
    private val observeSecureScreen: ObserveSecureScreenUseCase = mockk()
    private val observeCrashReporting: ObserveCrashReportingEnabledUseCase = mockk()
    private val setAppLockEnabled: SetAppLockEnabledUseCase = mockk(relaxed = true)
    private val setLockTimeoutMinutes: SetLockTimeoutUseCase = mockk(relaxed = true)
    private val setSecureScreenEnabled: SetSecureScreenUseCase = mockk(relaxed = true)
    private val setCrashReportingEnabled: SetCrashReportingEnabledUseCase = mockk(relaxed = true)
    private val verifyAppLockPassword: VerifyAppLockPasswordUseCase = mockk()
    private val unlockApp: UnlockAppUseCase = mockk(relaxed = true)

    private fun viewModel(
        enabled: Boolean = false,
        timeoutMinutes: Int = 0,
        secureScreen: Boolean = false,
    ): SettingsSecurityViewModel {
        every { observeAppLockEnabled() } returns flowOf(enabled)
        every { observeLockTimeout() } returns flowOf(timeoutMinutes)
        every { observeSecureScreen() } returns flowOf(secureScreen)
        every { observeCrashReporting() } returns flowOf(true)
        return SettingsSecurityViewModel(
            observeAppLockEnabled,
            observeLockTimeout,
            observeSecureScreen,
            observeCrashReporting,
            setAppLockEnabled,
            setLockTimeoutMinutes,
            setSecureScreenEnabled,
            setCrashReportingEnabled,
            verifyAppLockPassword,
            unlockApp,
        )
    }

    @Test
    fun `enabled mirrors the persisted master toggle`() = runTest {
        val vm = viewModel(enabled = true)

        assertThat(vm.enabled.value).isTrue()
    }

    @Test
    fun `timeoutMinutes mirrors the persisted inactivity threshold`() = runTest {
        val vm = viewModel(timeoutMinutes = 5)

        assertThat(vm.timeoutMinutes.value).isEqualTo(5)
    }

    @Test
    fun `secureScreen mirrors the persisted secure window opt-in`() = runTest {
        val vm = viewModel(secureScreen = true)

        assertThat(vm.secureScreen.value).isTrue()
    }

    @Test
    fun `verifying starts false`() = runTest {
        val vm = viewModel()

        assertThat(vm.verifying.value).isFalse()
    }

    @Test
    fun `setEnabled persists the toggle then unlocks so the user is not re-challenged`() = runTest {
        val vm = viewModel()

        vm.setEnabled(true)

        coVerifyOrder {
            setAppLockEnabled(true)
            unlockApp()
        }
    }

    @Test
    fun `setTimeout persists the picked timeout`() = runTest {
        val vm = viewModel()

        vm.setTimeout(15)

        coVerify { setLockTimeoutMinutes(15) }
    }

    @Test
    fun `setSecureScreen persists the picked secure window opt-in`() = runTest {
        val vm = viewModel()

        vm.setSecureScreen(true)

        coVerify { setSecureScreenEnabled(true) }
    }

    @Test
    fun `verifyPassword reports the result and clears verifying`() = runTest {
        coEvery { verifyAppLockPassword("secret") } returns UnlockResult.Success
        val vm = viewModel()
        var reported: UnlockResult? = null

        vm.verifyPassword("secret") { reported = it }

        assertThat(reported).isEqualTo(UnlockResult.Success)
        assertThat(vm.verifying.value).isFalse()
    }

    @Test
    fun `verifyPassword reports WrongPassword`() = runTest {
        coEvery { verifyAppLockPassword(any()) } returns UnlockResult.WrongPassword
        val vm = viewModel()
        var reported: UnlockResult? = null

        vm.verifyPassword("nope") { reported = it }

        assertThat(reported).isEqualTo(UnlockResult.WrongPassword)
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
