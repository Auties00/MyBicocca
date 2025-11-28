package it.attendance100.mybicocca.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.domain.contracts.*
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.*

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val userRepository: UserRepository,
  private val notificationRepository: NotificationRepository,
) : ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
  val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

  init {
    loadData()
  }

  private fun loadData() {
    viewModelScope.launch {
      userRepository.getUser().collect {
        _user.value = it
      }
    }
    viewModelScope.launch {
      notificationRepository.getUnreadNotifications().collect {
        _notifications.value = it
      }
    }
  }
}
