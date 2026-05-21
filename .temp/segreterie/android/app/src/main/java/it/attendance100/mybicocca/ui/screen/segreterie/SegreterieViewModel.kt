package it.attendance100.mybicocca.ui.screen.segreterie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.EvaluationRepository
import it.attendance100.mybicocca.data.repository.ExamRepository
import it.attendance100.mybicocca.data.repository.StudyPlanRepository
import it.attendance100.mybicocca.data.repository.TaxRepository
import it.attendance100.mybicocca.data.repository.TranscriptRepository
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SegreterieViewModel @Inject constructor(
    private val taxRepository: TaxRepository,
    private val examRepository: ExamRepository,
    private val transcriptRepository: TranscriptRepository,
    studyPlanRepository: StudyPlanRepository,
    private val evaluationRepository: EvaluationRepository,
    careerRepository: CareerRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val syncState: StateFlow<SyncUiState> = activeCareer
        .flatMapLatest { career ->
            if (career == null) return@flatMapLatest flowOf(SyncUiState())
            combine(
                resourceSyncManager.observe(SyncKeys.taxes(career.studentId)),
                resourceSyncManager.observe(SyncKeys.examCalls(career.studentId)),
                resourceSyncManager.observe(SyncKeys.transcript(career.studentId)),
                resourceSyncManager.observe(SyncKeys.questionnaires(career.studentId)),
            ) { taxSync, examSync, transcriptSync, questionnaireSync ->
                SyncUiState(
                    isRefreshing = taxSync.isRefreshing || examSync.isRefreshing || transcriptSync.isRefreshing || questionnaireSync.isRefreshing,
                    errorMessage = taxSync.errorMessage ?: examSync.errorMessage ?: transcriptSync.errorMessage ?: questionnaireSync.errorMessage,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val error: StateFlow<String?> = syncState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val unpaidTaxCount: StateFlow<Int> = activeCareer
        .flatMapLatest { career ->
            career?.let { taxRepository.observeCharges(it.studentId) } ?: flowOf(emptyList())
        }
        .map { charges -> charges.count { it.status != "PAID" && it.status != "CANCELED" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val bookedExamCount: StateFlow<Int> = examRepository.observeBookings()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val availableExamCount: StateFlow<Int> = examRepository.observeExamCalls()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val examResultsCount: StateFlow<Int> = activeCareer
        .flatMapLatest { career ->
            career?.let { transcriptRepository.observeRows(it.studentId) } ?: flowOf(emptyList())
        }
        .map { rows ->
            rows.count { row ->
                row.status == RecordBookRow.Status.PASSED || row.grade != null || row.date != null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val studyPlanStatus: StateFlow<String?> = activeCareer
        .flatMapLatest { career ->
            career?.let { studyPlanRepository.observeHeaders(it.studentId) } ?: flowOf(emptyList())
        }
        .map { headers -> headers.firstOrNull()?.statusDescription }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val studyPlanLastUpdated: StateFlow<String?> = activeCareer
        .flatMapLatest { career ->
            career?.let { studyPlanRepository.observeHeaders(it.studentId) } ?: flowOf(emptyList())
        }
        .map { headers -> headers.firstOrNull()?.lastUpdated }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val questionnairePendingCount: StateFlow<Int> = evaluationRepository.observePending()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun refresh() {
        viewModelScope.launch {
            val career = activeCareer.value ?: return@launch
            launch {
                resourceSyncManager.refresh(SyncKeys.taxes(career.studentId), SyncPolicies.Default) {
                    taxRepository.refreshCharges(career.studentId, career.studentId)
                }
            }
            launch {
                resourceSyncManager.refresh(SyncKeys.examCalls(career.studentId), SyncPolicies.Default) {
                    examRepository.refreshExamCalls(career.studentId, career.matricolaId)
                }
            }
            launch {
                resourceSyncManager.refresh(SyncKeys.transcript(career.studentId), SyncPolicies.Default) {
                    transcriptRepository.refresh(career.studentId)
                }
            }
            launch {
                resourceSyncManager.refresh(SyncKeys.questionnaires(career.studentId), SyncPolicies.Default) {
                    evaluationRepository.refresh(career.studentId)
                }
            }
        }
    }

    fun clearError() {
        val career = activeCareer.value ?: return
        resourceSyncManager.clearError(SyncKeys.taxes(career.studentId))
        resourceSyncManager.clearError(SyncKeys.examCalls(career.studentId))
        resourceSyncManager.clearError(SyncKeys.transcript(career.studentId))
        resourceSyncManager.clearError(SyncKeys.questionnaires(career.studentId))
    }
}
