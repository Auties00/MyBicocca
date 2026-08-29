package it.attendance100.mybicocca.ui.screen.lock

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.security.UnlockResult
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.security.ObserveAppLockUseCase
import it.attendance100.mybicocca.domain.usecase.security.UnlockAppUseCase
import it.attendance100.mybicocca.domain.usecase.security.VerifyAppLockPasswordUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Behaviour coverage for the app-lock gate's password path. Robolectric's headless device reports
 * biometric hardware as available, so the gate first renders the biometric path; [revealPasswordPath]
 * taps "Usa password" to fall back to the password field (a no-op when the password path already
 * shows). Verifies the gate renders the active username and a disabled unlock button, that entering
 * a password enables unlock, and that tapping it runs the password check and unlocks on success.
 * The screen is driven by a real [AppLockViewModel] over faked use cases, anchored on
 * [AppLockTestTags], and wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class AppLockScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val observeAppLock = mockk<ObserveAppLockUseCase> {
        every { this@mockk.invoke() } returns flowOf(true)
    }
    private val unlockApp = mockk<UnlockAppUseCase>(relaxed = true)
    private val verifyAppLockPassword = mockk<VerifyAppLockPasswordUseCase>()
    private val account = mockk<Account>(relaxed = true) {
        every { username } returns "mario.rossi"
    }
    private val observeActiveAccount = mockk<ObserveActiveAccountUseCase> {
        every { this@mockk.invoke() } returns flowOf(account)
    }

    private fun setLockScreen() {
        val viewModel = AppLockViewModel(observeAppLock, unlockApp, verifyAppLockPassword, observeActiveAccount)
        compose.setBicoccaContent {
                AppLockScreen(viewModel = viewModel)

        }
        revealPasswordPath()
    }

    private fun revealPasswordPath() {
        val onBiometricPath = compose.onAllNodesWithTag(AppLockTestTags.USE_PASSWORD_BUTTON)
            .fetchSemanticsNodes().isNotEmpty()
        if (onBiometricPath) {
            compose.onNodeWithTag(AppLockTestTags.USE_PASSWORD_BUTTON).performClick()
        }
    }

    @Test
    fun password_gate_shows_the_username_and_a_disabled_unlock_button() {
        setLockScreen()

        compose.onNodeWithTag(AppLockTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(AppLockTestTags.USERNAME).assertIsDisplayed()
        compose.onNodeWithTag(AppLockTestTags.PASSWORD_FIELD).assertIsDisplayed()
        compose.onNodeWithTag(AppLockTestTags.UNLOCK_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun entering_a_password_enables_unlock_and_tapping_it_runs_the_password_check() {
        coEvery { verifyAppLockPassword(any()) } returns UnlockResult.Success
        setLockScreen()

        compose.onNodeWithTag(AppLockTestTags.PASSWORD_FIELD).performTextInput("hunter2")
        compose.onNodeWithTag(AppLockTestTags.UNLOCK_BUTTON).assertIsEnabled()
        compose.onNodeWithTag(AppLockTestTags.UNLOCK_BUTTON).performClick()
        compose.waitForIdle()

        coVerify { verifyAppLockPassword("hunter2") }
    }

    @Test
    fun a_correct_password_unlocks_the_app() {
        coEvery { verifyAppLockPassword(any()) } returns UnlockResult.Success
        setLockScreen()

        compose.onNodeWithTag(AppLockTestTags.PASSWORD_FIELD).performTextInput("hunter2")
        compose.onNodeWithTag(AppLockTestTags.UNLOCK_BUTTON).performClick()
        compose.waitForIdle()

        coVerify { unlockApp() }
    }
}
