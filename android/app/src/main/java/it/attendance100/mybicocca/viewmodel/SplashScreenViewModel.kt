package it.attendance100.mybicocca.viewmodel

import android.util.*
import androidx.compose.runtime.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.domain.contracts.*
import kotlinx.coroutines.*
import retrofit2.*
import javax.inject.*

@HiltViewModel
class SplashViewModel @Inject constructor(
  private val authRepository: AuthRepository,
  private val userRepository: UserRepository,
) : ViewModel() {

  sealed class SplashState {
    object Loading : SplashState()
    object ShowLoginButton : SplashState()
    object NavigateHome : SplashState()
    object NavigateLogin : SplashState()
  }

  private val _splashState = mutableStateOf<SplashState>(SplashState.Loading)
  val splashState: State<SplashState> = _splashState

  init {
    checkLoginStatus()
  }

  private fun checkLoginStatus() {
    viewModelScope.launch {
      Log.d("SplashViewModel", "Checking login status...")
      if (authRepository.isUserLoggedIn()) {
        Log.d("SplashViewModel", "User is logged in. Fetching data...")
        try {
          userRepository.refreshUser()
          Log.d("SplashViewModel", "Data fetch successful. Navigating to Home.")
          _splashState.value = SplashState.NavigateHome
        } catch (e: Exception) {
          Log.e("SplashViewModel", "Data fetch failed", e)
          // Check for ExpiredJWTApiException or 401
          if (e is it.attendance100.mybicocca.data.api.ExpiredJWTApiException || (e is HttpException && e.code() == 401)) {
            Log.w("SplashViewModel", "Auth error (JWT expired). Redirecting to Login.")
            authRepository.logout()
            _splashState.value = SplashState.NavigateLogin
          } else {
            // Offline mode or other error -> Proceed to Home
            Log.w("SplashViewModel", "Non-auth error. Proceeding to Home (Offline Mode).")
            _splashState.value = SplashState.NavigateHome
          }
        }
      } else {
        Log.d("SplashViewModel", "User not logged in. Showing Login button.")
        _splashState.value = SplashState.ShowLoginButton
      }
    }
  }
}