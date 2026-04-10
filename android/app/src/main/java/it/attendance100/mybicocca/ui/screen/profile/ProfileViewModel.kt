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
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
	private val resourceSyncManager: ResourceSyncManager,
	networkMonitor: NetworkMonitor,
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

	val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

	private val syncState: StateFlow<SyncUiState> = combine(
		resourceSyncManager.observe(SyncKeys.USER),
		resourceSyncManager.observe(SyncKeys.CAREERS),
		activeCareer.flatMapLatest { career ->
			career?.let { resourceSyncManager.observe(SyncKeys.transcript(it.studentId)) }
				?: flowOf(SyncUiState())
		},
	) { userSync, careerSync, transcriptSync ->
		SyncUiState(
			isRefreshing = userSync.isRefreshing || careerSync.isRefreshing || transcriptSync.isRefreshing,
			errorMessage = userSync.errorMessage ?: careerSync.errorMessage ?: transcriptSync.errorMessage,
		)
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncUiState())

	val isRefreshing: StateFlow<Boolean> = syncState
		.map { it.isRefreshing }
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

	val error: StateFlow<String?> = syncState
		.map { it.errorMessage }
		.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

	init {
		viewModelScope.launch {
			resourceSyncManager.refreshIfStale(SyncKeys.USER, SyncPolicies.ShortLived) {
				userRepository.refresh()
			}
		}
		viewModelScope.launch {
			resourceSyncManager.refreshIfStale(SyncKeys.CAREERS, SyncPolicies.ShortLived) {
				careerRepository.refresh()
			}
		}
		viewModelScope.launch {
			activeCareer.filterNotNull().collectLatest { career ->
				resourceSyncManager.refreshIfStale(
					SyncKeys.transcript(career.studentId),
					SyncPolicies.Default,
				) {
					transcriptRepository.refresh(career.studentId)
				}
			}
		}
	}

	fun refresh() {
		viewModelScope.launch {
			resourceSyncManager.refresh(SyncKeys.USER, SyncPolicies.ShortLived) {
				userRepository.refresh()
			}
		}
		viewModelScope.launch {
			resourceSyncManager.refresh(SyncKeys.CAREERS, SyncPolicies.ShortLived) {
				careerRepository.refresh()
			}
		}
		viewModelScope.launch {
			val career = activeCareer.value ?: return@launch
			resourceSyncManager.refresh(SyncKeys.transcript(career.studentId), SyncPolicies.Default) {
				transcriptRepository.refresh(career.studentId)
			}
		}
	}

	fun clearError() {
		resourceSyncManager.clearError(SyncKeys.USER)
		resourceSyncManager.clearError(SyncKeys.CAREERS)
		val career = activeCareer.value ?: return
		resourceSyncManager.clearError(SyncKeys.transcript(career.studentId))
	}
}
