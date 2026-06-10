package it.attendance100.mybicocca.ui.screen.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.account.LearningIdentity
import it.attendance100.mybicocca.domain.model.account.SignInFailure
import it.attendance100.mybicocca.domain.model.account.SignInResult
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.usecase.account.SignInUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State and behaviour coverage for the credential sign-in form. A real [AuthViewModel] over a
 * faked [SignInUseCase] drives the full-screen [AuthScreen] (its shared-element wordmark degrades
 * to plain text when no root [SharedTransitionScope] is provided, so the screen renders directly).
 * Verifies the username/password fields and the SPID/CIE placeholder buttons render, that the
 * submit button stays disabled until both fields are filled so a blank submit never reaches the
 * use case, that a filled submit runs sign-in, and that rejected credentials raise the persistent
 * field-error highlight. Anchored on [AuthScreenTestTags] and rendered through the shared
 * [setBicoccaContent] environment, which installs the app-wide haptic/snackbar/device locals the
 * screen tree assumes. The submit and SPID/CIE controls sit below the fold of the scrolling form,
 * so each interaction scrolls its target into view first.
 */
@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val signIn = mockk<SignInUseCase>()

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
        createdAt = java.time.Instant.EPOCH,
        lastUsedAt = java.time.Instant.EPOCH,
        lastSyncedAt = java.time.Instant.EPOCH,
    )

    private fun setScreen(): AuthViewModel {
        val viewModel = AuthViewModel(signIn)
        compose.setBicoccaContent {
            AuthScreen(onSignedIn = { _, _ -> }, viewModel = viewModel)
        }
        return viewModel
    }

    @Test
    fun the_form_renders_both_credential_fields_and_the_SPID_and_CIE_placeholders() {
        setScreen()

        compose.onNodeWithTag(AuthScreenTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(AuthScreenTestTags.USERNAME_FIELD).assertIsDisplayed()
        compose.onNodeWithTag(AuthScreenTestTags.PASSWORD_FIELD).assertIsDisplayed()
        compose.onNodeWithTag(AuthScreenTestTags.SPID_BUTTON).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag(AuthScreenTestTags.CIE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun submit_stays_disabled_with_blank_fields_so_a_blank_submit_never_reaches_sign_in() {
        setScreen()

        compose.onNodeWithTag(AuthScreenTestTags.SUBMIT_BUTTON).assertIsNotEnabled()
        compose.onNodeWithTag(AuthScreenTestTags.SUBMIT_BUTTON).performClick()
        compose.waitForIdle()

        coVerify(exactly = 0) { signIn(any(), any()) }
    }

    @Test
    fun filling_both_fields_enables_submit_and_tapping_it_runs_sign_in() {
        coEvery { signIn(any(), any()) } returns SignInResult.Success(account, requiresCareerPick = false)
        setScreen()

        compose.onNodeWithTag(AuthScreenTestTags.USERNAME_FIELD).performTextInput("mario")
        compose.onNodeWithTag(AuthScreenTestTags.PASSWORD_FIELD).performTextInput("secret")
        compose.onNodeWithTag(AuthScreenTestTags.SUBMIT_BUTTON).performScrollTo().assertIsEnabled()
        compose.onNodeWithTag(AuthScreenTestTags.SUBMIT_BUTTON).performClick()
        compose.waitForIdle()

        coVerify { signIn("mario", "secret") }
    }

    @Test
    fun rejected_credentials_raise_the_persistent_field_error_highlight() {
        coEvery { signIn(any(), any()) } returns SignInResult.Failure(SignInFailure.BadCredentials)
        val viewModel = setScreen()

        compose.onNodeWithTag(AuthScreenTestTags.USERNAME_FIELD).performTextInput("mario")
        compose.onNodeWithTag(AuthScreenTestTags.PASSWORD_FIELD).performTextInput("wrong")
        compose.onNodeWithTag(AuthScreenTestTags.SUBMIT_BUTTON).performScrollTo().performClick()
        compose.waitForIdle()

        coVerify { signIn("mario", "wrong") }
        assertThat(viewModel.credentialsRejected.value).isTrue()
    }
}
