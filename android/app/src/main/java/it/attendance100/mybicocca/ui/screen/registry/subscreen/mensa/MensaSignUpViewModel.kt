package it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.mensa.MensaActivationResult
import it.attendance100.mybicocca.domain.model.mensa.MensaEmailCheck
import it.attendance100.mybicocca.domain.model.mensa.MensaPasswordRule
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpInput
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpResult
import it.attendance100.mybicocca.domain.model.mensa.MensaSignUpUnavailableException
import it.attendance100.mybicocca.domain.repository.MensaSignUpRepository
import it.attendance100.mybicocca.ui.screen.registry.subscreen.mensa.component.MensaNewAccount
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MensaSignUpStep { STARTING, FORM, CODE, OFFLINE, WEB_FORM }

data class MensaSignUpState(
    val step: MensaSignUpStep = MensaSignUpStep.STARTING,
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val captcha: String = "",
    val privacyAccepted: Boolean = false,
    val captchaImage: ByteArray? = null,
    val captchaLoading: Boolean = false,
    val emailCheck: MensaEmailCheck? = null,
    val emailChecking: Boolean = false,
    /** Compass's reasons for refusing the last submission. */
    val refusal: List<String>? = null,
    val submitting: Boolean = false,
    /** The address the code was sent to, on the code step. */
    val codeEmail: String = "",
    val code: String = "",
    val codeError: String? = null,
    /** A request failed for lack of connection; the form stays as it is. */
    val connectionError: Boolean = false,
) {
    val brokenPasswordRules: Set<MensaPasswordRule> get() = MensaPasswordRule.broken(password)
    val emailLooksValid: Boolean get() = EmailShape.matches(email.trim())
    val passwordsMatch: Boolean get() = passwordConfirm.isEmpty() || passwordConfirm == password

    val canSubmit: Boolean
        get() = !submitting &&
            firstName.trim().length >= 2 && lastName.trim().length >= 2 &&
            emailLooksValid && emailCheck !is MensaEmailCheck.Refused &&
            brokenPasswordRules.isEmpty() && passwordConfirm == password &&
            captcha.isNotBlank() && privacyAccepted

    val canActivate: Boolean get() = !submitting && code.isNotBlank()

    private companion object {
        val EmailShape = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]{2,}$""")
    }
}

/**
 * The native sign-up: our own form over Compass's sign-up pages. Anything those pages answer that
 * the client does not recognise moves to [MensaSignUpStep.WEB_FORM], which shows the pages themselves.
 */
@HiltViewModel
class MensaSignUpViewModel @Inject constructor(
    private val repository: MensaSignUpRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MensaSignUpState())
    val state: StateFlow<MensaSignUpState> = _state.asStateFlow()

    private val _activated = Channel<MensaNewAccount>(Channel.BUFFERED)

    /** The account just activated, with the password typed for it, to sign in with. */
    val activated = _activated.receiveAsFlow()

    private var emailCheckJob: Job? = null
    private var started = false

    /** Opens the sign-up the first time the page shows; a sign-up in progress is kept as it is. */
    fun ensureStarted() {
        if (!started) start()
    }

    /** Opens a new sign-up (or retries one that could not start). */
    fun start() {
        started = true
        _state.update { it.copy(step = MensaSignUpStep.STARTING, connectionError = false) }
        launchGuarded {
            repository.start()
            _state.update { it.copy(step = MensaSignUpStep.FORM) }
            loadCaptcha()
        }
    }

    fun setFirstName(value: String) = _state.update { it.copy(firstName = value) }
    fun setLastName(value: String) = _state.update { it.copy(lastName = value) }
    fun setPassword(value: String) = _state.update { it.copy(password = value) }
    fun setPasswordConfirm(value: String) = _state.update { it.copy(passwordConfirm = value) }
    fun setCaptcha(value: String) = _state.update { it.copy(captcha = value.uppercase()) }
    fun setPrivacyAccepted(value: Boolean) = _state.update { it.copy(privacyAccepted = value) }
    fun setCode(value: String) = _state.update { it.copy(code = value.filter(Char::isLetterOrDigit), codeError = null) }

    /** Asks Compass about the address once typing pauses, so a taken address shows before submitting. */
    fun setEmail(value: String) {
        _state.update { it.copy(email = value, emailCheck = null, emailChecking = false) }
        emailCheckJob?.cancel()
        if (!_state.value.emailLooksValid) return
        emailCheckJob = viewModelScope.launch {
            delay(700)
            _state.update { it.copy(emailChecking = true) }
            val check = runCatching { repository.checkEmail(value.trim()) }.getOrElse { e ->
                if (e is CancellationException) throw e
                if (e is MensaSignUpUnavailableException) MensaEmailCheck.Unknown else null
            }
            _state.update { if (it.email == value) it.copy(emailCheck = check, emailChecking = false) else it }
        }
    }

    fun refreshCaptcha() = launchGuarded { loadCaptcha() }

    fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(submitting = true, refusal = null, connectionError = false) }
        launchGuarded(onDone = { _state.update { it.copy(submitting = false) } }) {
            val result = repository.submit(
                MensaSignUpInput(
                    firstName = current.firstName,
                    lastName = current.lastName,
                    email = current.email,
                    password = current.password,
                    passwordConfirm = current.passwordConfirm,
                    captcha = current.captcha,
                ),
            )
            when (result) {
                is MensaSignUpResult.NeedsCode -> _state.update {
                    // The page names the address the code went to; should it ever leave it out, it is the one typed.
                    val sentTo = result.email.ifBlank { current.email.trim() }
                    it.copy(step = MensaSignUpStep.CODE, codeEmail = sentTo, code = "", codeError = null)
                }
                is MensaSignUpResult.Refused -> {
                    // A captcha is good for one try: a refused form needs a new one.
                    _state.update { it.copy(refusal = result.reasons, captcha = "") }
                    loadCaptcha()
                }
            }
        }
    }

    fun activate() {
        val current = _state.value
        if (!current.canActivate) return
        _state.update { it.copy(submitting = true, codeError = null, connectionError = false) }
        launchGuarded(onDone = { _state.update { it.copy(submitting = false) } }) {
            when (val result = repository.activate(current.codeEmail, current.code)) {
                MensaActivationResult.Activated -> {
                    _activated.send(MensaNewAccount(current.codeEmail, current.password))
                    // Done: opening the page again starts a new sign-up rather than this finished one.
                    started = false
                    _state.value = MensaSignUpState()
                }
                is MensaActivationResult.Failed -> _state.update { it.copy(codeError = result.message) }
            }
        }
    }

    /** Back from the code step to the form, e.g. to fix a mistyped address. The captcha was spent, so a new one loads. */
    fun editForm() {
        _state.update { it.copy(step = MensaSignUpStep.FORM, code = "", codeError = null, captcha = "") }
        refreshCaptcha()
    }

    private suspend fun loadCaptcha() {
        _state.update { it.copy(captchaLoading = true) }
        try {
            val image = repository.captcha()
            _state.update { it.copy(captchaImage = image) }
        } finally {
            _state.update { it.copy(captchaLoading = false) }
        }
    }

    /** Runs [block]; a changed page moves to the web form, a lost connection is shown on the form. */
    private fun launchGuarded(onDone: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (_: MensaSignUpUnavailableException) {
                _state.update { it.copy(step = MensaSignUpStep.WEB_FORM) }
            } catch (_: Exception) {
                _state.update {
                    if (it.step == MensaSignUpStep.STARTING) it.copy(step = MensaSignUpStep.OFFLINE) else it.copy(connectionError = true)
                }
            } finally {
                onDone()
            }
        }
    }

    override fun onCleared() {
        repository.close()
    }
}
