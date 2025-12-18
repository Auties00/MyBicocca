package it.attendance100.mybicocca.ui.screen.login

import android.net.*
import androidx.compose.runtime.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.domain.repository.*
import kotlinx.coroutines.*
import javax.inject.*

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
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
	            val success = userRepository.finishLogin(code, state, cookie)
	            if (success) {
		            // Fetch user profile immediately after login
		            userRepository.syncUser()
		            loginState = LoginState.Success
	            } else {
		            loginState = LoginState.Error
	            }
            }
        } else {
            loginState = LoginState.Error
        }
    }


	/**
	 * Starts the login flow by requesting the login URL from the repository
	 * The UI observes the emitted URL and loads it in a WebView
	 */
	fun startLoginFlow() {
        viewModelScope.launch {
	        userRepository.startLogin().collect { uri ->
		        // Handled by UI/WebView observing
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