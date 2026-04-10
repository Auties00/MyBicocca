package it.attendance100.mybicocca.ui.screen.segreterie.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.exam.ExamCall
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.ExamRepository
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent
import it.attendance100.mybicocca.util.NetworkMonitor
import it.attendance100.mybicocca.util.awaitFirstNonNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BookingViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val careerRepository: CareerRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val examCalls: StateFlow<List<ExamCall>> = activeCareer
        .flatMapLatest { career ->
            career?.let { examRepository.observeExamCallsByCareer(it.studentId) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = activeCareer
        .flatMapLatest { career ->
            career?.let { resourceSyncManager.observe(SyncKeys.examCalls(it.studentId)) } ?: flowOf(SyncUiState())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val error: StateFlow<String?> = syncState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _selectedExamCall = MutableStateFlow<ExamCall?>(null)
    val selectedExamCall: StateFlow<ExamCall?> = _selectedExamCall.asStateFlow()

    private val _selectedExamError = MutableStateFlow<String?>(null)
    val selectedExamError: StateFlow<String?> = _selectedExamError.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _isActionInProgress = MutableStateFlow(false)
    val isActionInProgress: StateFlow<Boolean> = _isActionInProgress.asStateFlow()

    private val _events = MutableSharedFlow<SegreterieActionEvent>()
    val events: SharedFlow<SegreterieActionEvent> =
        _events.asSharedFlow()

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
        resourceSyncManager.clearError(SyncKeys.examCalls(career.studentId))
    }

    fun loadExamCallById(id: Long) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _selectedExamError.value = null
            try {
                val call = examRepository.getExamCallDetailById(id) ?: examRepository.getExamCallById(id)
                if (call != null) {
                    _selectedExamCall.value = call
                } else {
                    _selectedExamError.value = "Impossibile caricare i dettagli dell'appello"
                }
            } catch (e: Exception) {
                _selectedExamError.value = e.message ?: "Errore sconosciuto"
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }

    fun bookSelectedExam() {
        viewModelScope.launch {
            val career = activeCareer.awaitFirstNonNull()
            val examCall = selectedExamCall.value
            if (examCall == null) {
                _events.emit(
                    SegreterieActionEvent.ShowMessage(
                        "Appello non disponibile"
                    )
                )
                return@launch
            }

            _isActionInProgress.value = true
            try {
                examRepository.bookExam(examCall).getOrThrow()
                refreshCareer(career, force = true)
                examRepository.refreshBookings(career.matricolaId).getOrThrow()
                _selectedExamCall.value = examRepository.getExamCallDetailById(examCall.id) ?: examCall
                _events.emit(
                    SegreterieActionEvent.ShowMessage(
                        "Prenotazione confermata"
                    )
                )
            } catch (e: Exception) {
                _events.emit(
                    SegreterieActionEvent.ShowMessage(
                        e.message ?: "Impossibile completare la prenotazione"
                    )
                )
            } finally {
                _isActionInProgress.value = false
            }
        }
    }

    private suspend fun refreshCareer(career: Career, force: Boolean) {
        val key = SyncKeys.examCalls(career.studentId)
        val refreshBlock: suspend () -> Result<Unit> = {
            examRepository.refreshExamCalls(
                careerId = career.studentId,
                matricolaId = career.matricolaId,
            )
        }

        if (force) {
            resourceSyncManager.refresh(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        } else {
            resourceSyncManager.refreshIfStale(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        }
    }
}
