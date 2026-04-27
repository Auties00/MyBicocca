package it.attendance100.mybicocca.ui.screen.segreterie.studyplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.studyplan.PlannedCourse
import it.attendance100.mybicocca.data.model.studyplan.StudyPlanHeader
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.StudyPlanRepository
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PianoCarrieraViewModel @Inject constructor(
    private val studyPlanRepository: StudyPlanRepository,
    private val careerRepository: CareerRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val headers: StateFlow<List<StudyPlanHeader>> = activeCareer
        .flatMapLatest { career ->
            career?.let { studyPlanRepository.observeHeaders(it.studentId) } ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val courses: StateFlow<List<PlannedCourse>> = headers
        .flatMapLatest { hdrs ->
            val planId = hdrs.firstOrNull()?.id ?: return@flatMapLatest flowOf(emptyList())
            studyPlanRepository.observeCourses(planId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = activeCareer
        .flatMapLatest { career ->
            career?.let { resourceSyncManager.observe(SyncKeys.studyPlan(it.studentId)) } ?: flowOf(SyncUiState())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val error: StateFlow<String?> = syncState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _isActionInProgress = MutableStateFlow(false)
    val isActionInProgress: StateFlow<Boolean> = _isActionInProgress.asStateFlow()

    private val _events = MutableSharedFlow<it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent>()
    val events: SharedFlow<it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent> =
        _events.asSharedFlow()

    private val _isEditEnabled = MutableStateFlow(false)
    val isEditEnabled: StateFlow<Boolean> = _isEditEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            activeCareer.collectLatest { career ->
                if (career != null) {
                    refreshCareer(career, force = false)
                    checkEditingWindow()
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshCareer(activeCareer.awaitFirstNonNull(), force = true)
            checkEditingWindow()
        }
    }

    fun clearError() {
        val career = activeCareer.value ?: return
        resourceSyncManager.clearError(SyncKeys.studyPlan(career.studentId))
    }

    fun editStudyPlan() {
        viewModelScope.launch {
            _events.emit(
                it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                    "La modifica del piano di studi non e' disponibile via Esse3 per il profilo studente"
                )
            )
        }
    }

    fun printStudyPlan() {
        viewModelScope.launch {
            val career = activeCareer.awaitFirstNonNull()
            val planId = headers.value.firstOrNull()?.id
            if (planId == null) {
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                        "Piano di studi non disponibile"
                    )
                )
                return@launch
            }

            _isActionInProgress.value = true
            try {
                val document = studyPlanRepository.getPlanPrint(career.studentId, planId).getOrThrow()
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.OpenDocument(
                        document
                    )
                )
            } catch (e: Exception) {
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                        e.message ?: "Impossibile aprire la stampa del piano"
                    )
                )
            } finally {
                _isActionInProgress.value = false
            }
        }
    }

    private suspend fun refreshCareer(career: Career, force: Boolean) {
        val key = SyncKeys.studyPlan(career.studentId)
        val refreshBlock: suspend () -> Result<Unit> = {
            runCatching {
                studyPlanRepository.refreshHeaders(career.studentId).getOrThrow()
                val planId = studyPlanRepository.observeHeaders(career.studentId)
                    .first()
                    .firstOrNull()
                    ?.id
                if (planId != null) {
                    studyPlanRepository.refreshCourses(career.studentId, planId).getOrThrow()
                }
            }
        }

        if (force) {
            resourceSyncManager.refresh(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        } else {
            resourceSyncManager.refreshIfStale(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        }
    }

    private fun checkEditingWindow(preFetchedHeader: StudyPlanHeader? = null) {
        viewModelScope.launch {
            val header = preFetchedHeader ?: headers.value.firstOrNull() ?: return@launch
            val regId = header.choiceRegulationId ?: return@launch
            _isEditEnabled.value = runCatching {
                studyPlanRepository.isEditingWindowOpen(regId)
            }.getOrDefault(false)
        }
    }
}
