package it.attendance100.mybicocca.ui.screen.main.career

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.domain.model.CareerStats
import it.attendance100.mybicocca.domain.model.User
import it.attendance100.mybicocca.domain.repository.UserRepository
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _stats = MutableStateFlow<CareerStats?>(null)
    val stats: StateFlow<CareerStats?> = _stats.asStateFlow()

    init {
        loadData()

        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    refreshData()
                }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            try {
                userRepository.refreshUser()
            } catch (_: Exception) {
            }
        }
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