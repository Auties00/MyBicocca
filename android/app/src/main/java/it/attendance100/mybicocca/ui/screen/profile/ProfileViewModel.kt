package it.attendance100.mybicocca.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.data.model.transcript.RecordBookStats
import it.attendance100.mybicocca.data.model.user.User
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.TranscriptRepository
import it.attendance100.mybicocca.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
	private val userRepository: UserRepository,
	private val careerRepository: CareerRepository,
	private val transcriptRepository: TranscriptRepository,
) : ViewModel() {

	val user: StateFlow<User?> = userRepository.observeUser()
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

	val activeCareer: StateFlow<Career?> = careerRepository.observeAll()
		.map { it.firstOrNull() }
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

	val stats: StateFlow<RecordBookStats?> = activeCareer
		.flatMapLatest { career ->
			career?.let { transcriptRepository.observeStats(it.studentId) } ?: flowOf(null)
		}
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

	val rows: StateFlow<List<RecordBookRow>> = activeCareer
		.flatMapLatest { career ->
			career?.let { transcriptRepository.observeRows(it.studentId) } ?: flowOf(emptyList())
		}
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

	init {
		viewModelScope.launch {
			userRepository.refresh()
			careerRepository.refresh()
		}
		viewModelScope.launch {
			activeCareer.filterNotNull().collect { career ->
				transcriptRepository.refresh(career.studentId)
			}
		}
	}
}
