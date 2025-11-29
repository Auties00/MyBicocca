package it.attendance100.mybicocca.viewmodel.login

import android.net.*
import android.util.*
import androidx.compose.runtime.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.data.api.*
import it.attendance100.mybicocca.data.repository.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import javax.inject.*
import it.attendance100.mybicocca.domain.contracts.AuthRepository as IAuthRepository

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val authRepository: IAuthRepository,
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