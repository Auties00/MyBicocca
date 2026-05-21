package it.attendance100.mybicocca.ui.screen.segreterie.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.internship.InternshipApplication
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.InternshipRepository
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
class StageViewModel @Inject constructor(
    private val internshipRepository: InternshipRepository,
    private val careerRepository: CareerRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val applications: StateFlow<List<InternshipApplication>> = activeCareer
        .flatMapLatest { career ->
            career?.let { internshipRepository.observeByStudent(it.studentId) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = activeCareer
        .flatMapLatest { career ->
            career?.let { resourceSyncManager.observe(SyncKeys.internships(it.studentId)) } ?: flowOf(SyncUiState())
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
        resourceSyncManager.clearError(SyncKeys.internships(career.studentId))
    }

    private suspend fun refreshCareer(career: Career, force: Boolean) {
        val key = SyncKeys.internships(career.studentId)
        val refreshBlock: suspend () -> Result<Unit> = {
            internshipRepository.refresh(career.studentId)
        }

        if (force) {
            resourceSyncManager.refresh(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        } else {
            resourceSyncManager.refreshIfStale(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        }
    }
}
