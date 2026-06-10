package it.attendance100.mybicocca.ui.screen.auth

import app.cash.turbine.test
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
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.auth.state.AuthEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Instant

/**
 * Coverage for the credential sign-in form ViewModel: blank and re-entrant submissions are
 * dropped, success clears the password and emits SignedIn, BadCredentials sets the persistent
 * field-highlight while every failure surfaces a one-shot Failed event, and editing a field
 * clears the rejection highlight.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val signIn: SignInUseCase = mockk()

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

    private fun viewModel() = AuthViewModel(signIn)

    @Test
    fun `setUsername and setPassword update fields and clear the rejection highlight`() = runTest {
        coEvery { signIn(any(), any()) } returns SignInResult.Failure(SignInFailure.BadCredentials)
        val vm = viewModel()
        vm.setUsername("u")
        vm.setPassword("p")
        vm.submit()
        assertThat(vm.credentialsRejected.value).isTrue()

        vm.setUsername("u2")

        assertThat(vm.username.value).isEqualTo("u2")
        assertThat(vm.credentialsRejected.value).isFalse()
    }

    @Test
    fun `submit with blank username does not call sign-in`() = runTest {
        val vm = viewModel()
        vm.setPassword("p")

        vm.submit()

        coVerify(exactly = 0) { signIn(any(), any()) }
    }

    @Test
    fun `submit with blank password does not call sign-in`() = runTest {
        val vm = viewModel()
        vm.setUsername("u")

        vm.submit()

        coVerify(exactly = 0) { signIn(any(), any()) }
    }

    @Test
    fun `successful submit clears the password and emits SignedIn`() = runTest {
        coEvery { signIn("mario", "secret") } returns SignInResult.Success(account, requiresCareerPick = true)
        val vm = viewModel()
        vm.setUsername("mario")
        vm.setPassword("secret")

        vm.events.test {
            vm.submit()
            val event = awaitItem()
            assertThat(event).isInstanceOf(AuthEvent.SignedIn::class.java)
            val signedIn = event as AuthEvent.SignedIn
            assertThat(signedIn.account).isEqualTo(account)
            assertThat(signedIn.requiresCareerPick).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.password.value).isEmpty()
        assertThat(vm.inflight.value).isFalse()
    }

    @Test
    fun `bad credentials sets the rejection highlight and emits Failed`() = runTest {
        coEvery { signIn(any(), any()) } returns SignInResult.Failure(SignInFailure.BadCredentials)
        val vm = viewModel()
        vm.setUsername("u")
        vm.setPassword("p")

        vm.events.test {
            vm.submit()
            val event = awaitItem()
            assertThat(event).isInstanceOf(AuthEvent.Failed::class.java)
            assertThat((event as AuthEvent.Failed).reason).isEqualTo(SignInFailure.BadCredentials)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.credentialsRejected.value).isTrue()
    }

    @Test
    fun `non-credential failure emits Failed without setting the rejection highlight`() = runTest {
        coEvery { signIn(any(), any()) } returns SignInResult.Failure(SignInFailure.NoConnection)
        val vm = viewModel()
        vm.setUsername("u")
        vm.setPassword("p")

        vm.events.test {
            vm.submit()
            val event = awaitItem()
            assertThat((event as AuthEvent.Failed).reason).isEqualTo(SignInFailure.NoConnection)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.credentialsRejected.value).isFalse()
    }

    @Test
    fun `re-entrant submit while a sign-in is in flight is dropped`() = runTest {
        val gate = CompletableDeferred<SignInResult>()
        coEvery { signIn(any(), any()) } coAnswers { gate.await() }
        val vm = viewModel()
        vm.setUsername("u")
        vm.setPassword("p")

        vm.submit()
        assertThat(vm.inflight.value).isTrue()
        vm.submit()

        gate.complete(SignInResult.Success(account, requiresCareerPick = false))
        coVerify(exactly = 1) { signIn(any(), any()) }
    }

    @Test
    fun `reset blanks the whole form`() = runTest {
        coEvery { signIn(any(), any()) } returns SignInResult.Failure(SignInFailure.BadCredentials)
        val vm = viewModel()
        vm.setUsername("u")
        vm.setPassword("p")
        vm.submit()

        vm.reset()

        assertThat(vm.username.value).isEmpty()
        assertThat(vm.password.value).isEmpty()
        assertThat(vm.credentialsRejected.value).isFalse()
    }
}
