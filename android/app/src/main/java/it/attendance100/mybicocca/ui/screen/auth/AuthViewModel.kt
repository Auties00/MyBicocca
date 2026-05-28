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

    private val _error = MutableStateFlow<SignInFailure?>(null)
    val error: StateFlow<SignInFailure?> = _error.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun setUsername(value: String) {
        _username.value = value
        clearError()
    }

    fun setPassword(value: String) {
        _password.value = value
        clearError()
    }

    fun submit() {
        if (_inflight.value) return
        if (_username.value.isBlank() || _password.value.isBlank()) return
        viewModelScope.launch {
            _inflight.value = true
            clearError()
            val result = signIn(_username.value, _password.value)
            _inflight.value = false
            when (result) {
                is SignInResult.Success -> {
                    _password.value = ""
                    _events.send(AuthEvent.SignedIn(result.account, result.requiresCareerPick))
                }
                is SignInResult.Failure -> {
                    _error.value = result.reason
                }
            }
        }
    }

    fun reset() {
        _username.value = ""
        _password.value = ""
        clearError()
    }

    private fun clearError() {
        _error.value = null
    }
}
