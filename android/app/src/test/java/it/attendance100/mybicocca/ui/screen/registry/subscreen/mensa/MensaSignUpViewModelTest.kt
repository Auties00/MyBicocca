package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.mensa.MensaActivationResult
import it.attendance100.mybicocca.domain.model.mensa.MensaPasswordRule
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpResult
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpUnavailableException
import it.attendance100.mybicocca.domain.repository.MensaSignUpRepository
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaNewAccount
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MensaSignUpViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val repository = mockk<MensaSignUpRepository>(relaxed = true) {
        coEvery { captcha() } returns byteArrayOf(1, 2, 3)
    }

    private fun filled(vm: MensaSignUpViewModel) {
        vm.setFirstName("Giulia")
        vm.setLastName("Demo")
        vm.setPassword("Mensa2026!")
        vm.setPasswordConfirm("Mensa2026!")
        vm.setCaptcha("abcde")
        vm.setPrivacyAccepted(true)
        // Set last: a valid address starts a delayed check that the test does not wait for.
        vm.setEmail("giulia.demo@example.org")
    }

    @Test
    fun `starting opens the form with a captcha`() = runTest {
        val vm = MensaSignUpViewModel(repository)
        vm.ensureStarted()
        assertThat(vm.state.value.step).isEqualTo(MensaSignUpStep.FORM)
        assertThat(vm.state.value.captchaImage).isNotNull()
        coVerify(exactly = 1) { repository.start() }
    }

    @Test
    fun `a refused submission shows the reasons and asks for a new captcha`() = runTest {
        coEvery { repository.submit(any()) } returns MensaSignUpResult.Refused(listOf("Captcha non valido."))
        val vm = MensaSignUpViewModel(repository)
        vm.ensureStarted()
        filled(vm)
        assertThat(vm.state.value.captcha).isEqualTo("ABCDE")
        vm.submit()
        assertThat(vm.state.value.refusal).containsExactly("Captcha non valido.")
        assertThat(vm.state.value.captcha).isEmpty()
        coVerify(exactly = 2) { repository.captcha() }
    }

    @Test
    fun `a page Compass changed switches to the web form`() = runTest {
        coEvery { repository.start() } throws MensaSignUpUnavailableException("changed")
        val vm = MensaSignUpViewModel(repository)
        vm.ensureStarted()
        assertThat(vm.state.value.step).isEqualTo(MensaSignUpStep.WEB_FORM)
    }

    @Test
    fun `the right code activates and hands over the credentials`() = runTest {
        coEvery { repository.submit(any()) } returns MensaSignUpResult.NeedsCode("giulia.demo@example.org")
        coEvery { repository.activate(any(), "123456") } returns MensaActivationResult.Activated
        val vm = MensaSignUpViewModel(repository)
        vm.ensureStarted()
        filled(vm)
        vm.submit()
        assertThat(vm.state.value.step).isEqualTo(MensaSignUpStep.CODE)
        vm.activated.test {
            vm.setCode("123456")
            vm.activate()
            assertThat(awaitItem()).isEqualTo(MensaNewAccount("giulia.demo@example.org", "Mensa2026!"))
        }
        // Finished: the next visit starts a new sign-up.
        assertThat(vm.state.value.step).isEqualTo(MensaSignUpStep.STARTING)
    }

    @Test
    fun `a wrong code stays on the code step with Compass's explanation`() = runTest {
        coEvery { repository.submit(any()) } returns MensaSignUpResult.NeedsCode("giulia.demo@example.org")
        coEvery { repository.activate(any(), any()) } returns MensaActivationResult.Failed("Codice errato")
        val vm = MensaSignUpViewModel(repository)
        vm.ensureStarted()
        filled(vm)
        vm.submit()
        vm.setCode("000000")
        vm.activate()
        assertThat(vm.state.value.step).isEqualTo(MensaSignUpStep.CODE)
        assertThat(vm.state.value.codeError).isEqualTo("Codice errato")
    }

    @Test
    fun `password rules match the sign-up page`() {
        assertThat(MensaPasswordRule.broken("Mensa2026!")).isEmpty()
        assertThat(MensaPasswordRule.broken("mensa")).containsExactly(
            MensaPasswordRule.LENGTH, MensaPasswordRule.UPPER_AND_LOWER, MensaPasswordRule.DIGIT, MensaPasswordRule.SPECIAL,
        )
        assertThat(MensaPasswordRule.broken("Mensà2026!")).containsExactly(MensaPasswordRule.ALLOWED_CHARACTERS)
        assertThat(MensaPasswordRule.broken("Mensa2026#")).containsExactly(MensaPasswordRule.SPECIAL, MensaPasswordRule.ALLOWED_CHARACTERS)
    }
}
