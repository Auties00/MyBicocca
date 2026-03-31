package it.attendance100.mybicocca.ui.screen.login

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.repository.AuthRepository
import it.attendance100.mybicocca.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val EMAIL_STATE_KEY = "login_identifier"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    var email by mutableStateOf(savedStateHandle[EMAIL_STATE_KEY] ?: "")
        private set

    fun updateEmail(input: String) {
        email = input
        savedStateHandle[EMAIL_STATE_KEY] = input
        clearError()
    }

    fun clearError() {
        _uiState.update { state ->
            if (state.error == null) state else state.copy(error = null)
        }
    }

    private fun isValidIdentifier(identifier: String): Boolean {
        if (identifier.isBlank()) return false
        if (identifier.contains('@'))
            return Patterns.EMAIL_ADDRESS.matcher(identifier).matches()

        return true
    }

    val emailHasErrors by derivedStateOf {
        val trimmedEmail = email.trim()
        return@derivedStateOf trimmedEmail.isNotEmpty() && !isValidIdentifier(trimmedEmail)
    }

    fun login(password: String) {
        val trimmedIdentifier = email.trim()

        if (!isValidIdentifier(trimmedIdentifier)) {
            _uiState.update { it.copy(error = "Enter a valid username.") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(error = "The password cannot be empty.") }
            return
        }

        val finalEmail = if (trimmedIdentifier.contains('@')) {
            trimmedIdentifier
        } else {
            // Assume it's a username
            "$trimmedIdentifier@campus.unimib.it"
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.login(finalEmail, password)
                .onSuccess {
                    // Fetch and cache user profile
                    userRepository.refresh()
                    _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
                }
                .onFailure { e ->
                    Log.e("LoginViewModel", "Login failed!", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = mapLoginError(e),
                        )
                    }
                }
        }
    }

    private fun mapLoginError(error: Throwable): String {
        if (isInvalidCredentialsError(error)) return "Invalid username or password."

        return error.localizedMessage ?: "Login failed. Please try again."
    }

    private fun isInvalidCredentialsError(error: Throwable): Boolean {
        return generateSequence(error) { it.cause }
            .any { current ->
                val statusCode = extractStatusCode(current)
                val message = current.localizedMessage.orEmpty().lowercase()
                val className = current.javaClass.name.lowercase()

                (statusCode == 401 && isCredentialMessage(message)) ||
                        message.contains("status code 401") ||
                        isCredentialMessage(message) ||
                        (statusCode == 401 && className.contains("validationexception"))
            }
    }

    private fun extractStatusCode(error: Throwable): Int? {
        val getterCode = runCatching {
            error.javaClass.methods
                .firstOrNull { it.name == "getStatusCode" && it.parameterCount == 0 }
                ?.invoke(error)
                ?.let { value -> (value as? Number)?.toInt() }
        }.getOrNull()

        if (getterCode != null) return getterCode

        return runCatching {
            val field = error.javaClass.getDeclaredField("statusCode")
            field.isAccessible = true
            (field.get(error) as? Number)?.toInt()
        }.getOrNull()
    }

    private fun isCredentialMessage(message: String): Boolean {
        return (
                (message.contains("credenzial") && message.contains("non valid")) ||
                        (message.contains("invalid credential")) ||
                        (message.contains("login") && message.contains("credenzial"))
                )
    }
}
