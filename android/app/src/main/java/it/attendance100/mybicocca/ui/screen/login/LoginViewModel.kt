package it.attendance100.mybicocca.ui.screen.login

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.remote.api.MyBicoccaApiService
import it.attendance100.mybicocca.data.repository.UserRepository
import it.attendance100.mybicocca.domain.repository.AuthRepository
import it.attendance100.mybicocca.util.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val apiService: MyBicoccaApiService,
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    var loginState by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    /**
     * Called when the WebView intercepts the callback URL
     * Parses the code and state, then triggers the repository to fetch headers
     */
    fun handleCallbackUrl(url: String, cookie: String) {
        if (loginState is LoginState.Authenticating) return

        val uri = Uri.parse(url)
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")

        if (code != null && state != null) {
            loginState = LoginState.Authenticating
            viewModelScope.launch {
                val success = authRepository.performLoginCallback(code, state, cookie)
                loginState = if (success) LoginState.Success else LoginState.Error
            }
        } else {
            loginState = LoginState.Error
        }
    }

    fun fetchAndLogProfile() {
        viewModelScope.launch {
            try {
                // Trigger the repository refresh
                // Fetches from API and saves to Room Database
                userRepository.refreshUser()
            } catch (e: Exception) {
                Log.e("MyBicoccaAuth", "Failed to refresh user data", e)
            }
        }
    }

    fun resetState() {
        loginState = LoginState.Idle
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Authenticating : LoginState()
        object Success : LoginState()
        object Error : LoginState()
    }
}