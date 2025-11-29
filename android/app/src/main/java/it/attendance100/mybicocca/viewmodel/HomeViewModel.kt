package it.attendance100.mybicocca.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.data.repository.*
import kotlinx.coroutines.*
import javax.inject.*

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val userRepository: UserRepository,
) : ViewModel() {

  // Expose data as Flow or State for UI
  val user = userRepository.getUser()

  init {
    // Trigger background refresh immediately on init
    refreshData()
  }


  fun refreshData() {
    viewModelScope.launch {
      // Runs in background.
      // UI updates automatically because it observes the 'user' Flow.
      userRepository.refreshUser()
    }
  }
}