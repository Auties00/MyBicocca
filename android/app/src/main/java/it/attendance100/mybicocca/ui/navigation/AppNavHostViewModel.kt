package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.UserRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val careerRepository: CareerRepository,
) : ViewModel() {
    val profilePic = userRepository.observeUser().map { user ->
        user?.profilePic
    }

    init {
        viewModelScope.launch {
            userRepository.refresh()
            careerRepository.refresh()
        }
    }
}
