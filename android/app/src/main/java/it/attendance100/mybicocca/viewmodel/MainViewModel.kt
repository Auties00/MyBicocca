package it.attendance100.mybicocca.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.*

@HiltViewModel
class MainViewModel @Inject constructor(
  networkMonitor: NetworkMonitor,
  private val preferencesManager: PreferencesManager,
) : ViewModel() {

  val isOffline: StateFlow<Boolean> = networkMonitor.isOnline
      .map { !it }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
      )

  private val _isSessionExpired = MutableStateFlow(false)
  val isSessionExpired: StateFlow<Boolean> = _isSessionExpired.asStateFlow()

  init {
    startSessionCheck()
    networkMonitor.refresh()
  }

  /**
   * Periodically checks if the user session has expired based on preferences
   */
  private fun startSessionCheck() {
    viewModelScope.launch {
      while (isActive) {
        checkSession()
        delay(60000) // Check every minute
      }
    }
  }

  fun checkSession() {
    if (!preferencesManager.isLoggedIn()) {
      _isSessionExpired.value = false
      return
    }

    if (preferencesManager.keepLoggedIn) {
      _isSessionExpired.value = false
      return
    }

    val currentTime = System.currentTimeMillis()
    val authExpiry = preferencesManager.authExpiry

    if (authExpiry > 0) {
      // Use expiry time provided by server
      _isSessionExpired.value = currentTime > authExpiry
    } else {
      // Fallback to local timer if server expiry not yet available
      val startTime = preferencesManager.sessionStartTime
      val duration = preferencesManager.sessionDuration

      if (duration == PreferencesManager.DURATION_FOREVER) {
        _isSessionExpired.value = false
        return
      }

      if (currentTime - startTime > duration) {
        _isSessionExpired.value = true
      } else {
        _isSessionExpired.value = false
      }
    }
  }
}
