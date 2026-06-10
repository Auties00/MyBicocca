package it.attendance100.mybicocca.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.account.SignInFailure
import it.attendance100.mybicocca.domain.model.account.SignInResult
import it.attendance100.mybicocca.domain.usecase.account.SignInUseCase
import it.attendance100.mybicocca.ui.screen.auth.state.AuthEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the credential sign-in form shared by the full-screen login and the in-sheet
 * add-account flow.
 *
 * Field values, [inflight] and [credentialsRejected] are separate [StateFlow]s; sign-in
 * outcomes are one-shot [AuthEvent]s on [events]. [submit] drops re-entrant or blank
 * submissions and clears the password on success; [reset] blanks the whole form so a
 * retained instance can be reused by the next host.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signIn: SignInUseCase,
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _inflight = MutableStateFlow(false)
    val inflight: StateFlow<Boolean> = _inflight.asStateFlow()

    private val _credentialsRejected = MutableStateFlow(false)

    /**
     * Persistent field-highlight: the username/password outlines stay red after a rejected
     * login until the user edits a field. The failure message itself is a one-shot snackbar
     * ([AuthEvent.Failed]), not state.
     */
    val credentialsRejected: StateFlow<Boolean> = _credentialsRejected.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun setUsername(value: String) {
        _username.value = value
        _credentialsRejected.value = false
    }

    fun setPassword(value: String) {
        _password.value = value
        _credentialsRejected.value = false
    }

    fun submit() {
        if (_inflight.value) return
        if (_username.value.isBlank() || _password.value.isBlank()) return
        viewModelScope.launch {
            _inflight.value = true
            _credentialsRejected.value = false
            val result = signIn(_username.value, _password.value)
            _inflight.value = false
            when (result) {
                is SignInResult.Success -> {
                    _password.value = ""
                    _events.send(AuthEvent.SignedIn(result.account, result.requiresCareerPick))
                }
                is SignInResult.Failure -> {
                    _credentialsRejected.value = result.reason is SignInFailure.BadCredentials
                    _events.send(AuthEvent.Failed(result.reason))
                }
            }
        }
    }

    fun reset() {
        _username.value = ""
        _password.value = ""
        _credentialsRejected.value = false
    }
}
