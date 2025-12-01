package it.attendance100.mybicocca.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.data.repository.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.*
import javax.inject.*

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val userRepository: UserRepository,
  networkMonitor: NetworkMonitor,
) : ViewModel() {

  // Expose data as Flow or State for UI
  val user = userRepository.getUser()

  init {
    // Observe network status to trigger refresh when connectivity returns
    viewModelScope.launch {
      networkMonitor.isOnline.collect { isOnline ->
        if (isOnline) {
          refreshData()
        }
      }
    }
  }


  fun refreshData() {
    viewModelScope.launch {
      // Runs in background.
      // UI updates automatically because it observes the 'user' Flow.
      try {
        userRepository.refreshUser()
      } catch (_: Exception) {
      }
    }
  }
}