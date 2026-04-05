package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.user.User
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.UserRepository
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val careerRepository: CareerRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    val profilePic: Flow<ByteArray?> = userRepository.observeUser().map { it?.profilePic }
    val user: Flow<User?> = userRepository.observeUser()
    val activeCareer: Flow<Career?> = careerRepository.observeAll().map { it.firstOrNull() }
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    init {
        viewModelScope.launch {
            userRepository.refresh()
            careerRepository.refresh()
        }
    }
}
