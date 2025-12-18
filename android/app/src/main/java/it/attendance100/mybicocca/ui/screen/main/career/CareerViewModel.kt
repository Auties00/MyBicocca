package it.attendance100.mybicocca.ui.screen.main.career

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.*
import it.attendance100.mybicocca.domain.model.*
import it.attendance100.mybicocca.domain.repository.*
import it.attendance100.mybicocca.manager.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.*

@HiltViewModel
class CareerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    networkManager: NetworkManager,
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _stats = MutableStateFlow<CareerStats?>(null)
    val stats: StateFlow<CareerStats?> = _stats.asStateFlow()

    init {
        loadData()

        viewModelScope.launch {
            networkManager.isOnline.collect { isOnline ->
                if (isOnline) {
                    refreshData()
                }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            try {
	            userRepository.syncUser()
	            userRepository.syncCareerStats()
            } catch (_: Exception) {
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
	        userRepository.observeUser().asFlow().collect {
                _user.value = it
            }
        }
        viewModelScope.launch {
	        userRepository.observeCareerStats().asFlow().collect {
                _stats.value = it
            }
        }
    }
}