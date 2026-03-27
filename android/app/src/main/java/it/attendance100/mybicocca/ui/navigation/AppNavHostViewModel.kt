package it.attendance100.mybicocca.ui.navigation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.repository.UserRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    userRepository: UserRepository,
) : ViewModel() {
    val profilePic = userRepository.observeUser().map { user ->
        user?.profilePic
    }
}
