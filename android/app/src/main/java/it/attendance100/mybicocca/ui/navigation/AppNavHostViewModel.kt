package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.user.User
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val careerRepository: CareerRepository,
) : ViewModel() {
    val profilePic: Flow<ByteArray?> = userRepository.observeUser().map { it?.profilePic }
    val user: Flow<User?> = userRepository.observeUser()
    val activeCareer: Flow<Career?> = careerRepository.observeAll().map { it.firstOrNull() }

    init {
        viewModelScope.launch {
            userRepository.refresh()
            careerRepository.refresh()
        }
    }
}
