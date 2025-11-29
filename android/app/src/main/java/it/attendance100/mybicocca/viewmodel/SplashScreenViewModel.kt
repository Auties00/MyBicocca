package it.attendance100.mybicocca.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import javax.inject.*

@HiltViewModel
class SplashViewModel @Inject constructor(
  private val preferencesManager: PreferencesManager,
) : ViewModel() {

  private val _startDestination = mutableStateOf<Screen?>(null)
  val startDestination: State<Screen?> = _startDestination

  init {
    viewModelScope.launch {
      // Optional: minimal delay to prevent screen flicker if check is too fast
      // delay(500)

      if (preferencesManager.isLoggedIn()) {
        _startDestination.value = Screen.Home
      } else {
        _startDestination.value = Screen.Login
      }
    }
  }
}