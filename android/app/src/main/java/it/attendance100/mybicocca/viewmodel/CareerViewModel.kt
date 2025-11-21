package it.attendance100.mybicocca.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.domain.contracts.*
import it.attendance100.mybicocca.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.*

@HiltViewModel
class CareerViewModel @Inject constructor(
  private val userRepository: UserRepository,
) : ViewModel() {

  private val _user = MutableStateFlow<User?>(null)
  val user: StateFlow<User?> = _user.asStateFlow()

  private val _stats = MutableStateFlow<CareerStats?>(null)
  val stats: StateFlow<CareerStats?> = _stats.asStateFlow()

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
      userRepository.getCareerStats().collect {
        _stats.value = it
      }
    }
  }
}
