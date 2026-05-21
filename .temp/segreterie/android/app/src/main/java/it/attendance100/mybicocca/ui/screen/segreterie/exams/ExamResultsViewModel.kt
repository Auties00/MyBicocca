package it.attendance100.mybicocca.ui.screen.segreterie.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.data.model.transcript.RecordBookStats
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.TranscriptRepository
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.util.NetworkMonitor
import it.attendance100.mybicocca.util.awaitFirstNonNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExamResultsViewModel @Inject constructor(
    private val transcriptRepository: TranscriptRepository,
    private val careerRepository: CareerRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val rows: StateFlow<List<RecordBookRow>> = activeCareer
        .flatMapLatest { career ->
            career?.let { transcriptRepository.observeRows(it.studentId) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<RecordBookStats?> = activeCareer
        .flatMapLatest { career ->
            career?.let { transcriptRepository.observeStats(it.studentId) } ?: flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val syncState: StateFlow<SyncUiState> = activeCareer
        .flatMapLatest { career ->
            career?.let { resourceSyncManager.observe(SyncKeys.transcript(it.studentId)) } ?: flowOf(SyncUiState())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val error: StateFlow<String?> = syncState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            activeCareer.collectLatest { career ->
                if (career != null) {
                    refreshCareer(career, force = false)
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshCareer(activeCareer.awaitFirstNonNull(), force = true)
        }
    }

    fun clearError() {
        val career = activeCareer.value ?: return
        resourceSyncManager.clearError(SyncKeys.transcript(career.studentId))
    }

    private suspend fun refreshCareer(career: Career, force: Boolean) {
        val key = SyncKeys.transcript(career.studentId)
        val refreshBlock: suspend () -> Result<Unit> = {
            transcriptRepository.refresh(career.studentId)
        }

        if (force) {
            resourceSyncManager.refresh(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        } else {
            resourceSyncManager.refreshIfStale(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        }
    }
}
