package it.attendance100.mybicocca.ui.screen.segreterie.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.career.Career
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.repository.CareerRepository
import it.attendance100.mybicocca.data.repository.ExamRepository
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BookedViewModel @Inject constructor(
    private val examRepository: ExamRepository,
    private val careerRepository: CareerRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    private val activeCareer = careerRepository.observeAll()
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val bookings: StateFlow<List<ExamBooking>> = examRepository.observeBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = activeCareer
        .flatMapLatest { career ->
            career?.let {
                resourceSyncManager.observe(SyncKeys.examBookings(it.matricolaId ?: it.studentId))
            } ?: flowOf(SyncUiState())
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
        resourceSyncManager.clearError(SyncKeys.examBookings(career.matricolaId ?: career.studentId))
    }

    fun cancelBooking(booking: ExamBooking) {
        viewModelScope.launch {
            val career = activeCareer.awaitFirstNonNull()
            _isActionInProgress.value = true
            try {
//                examRepository.cancelBooking(booking).getOrThrow() TODO: implement
                refreshCareer(career, force = true)
                examRepository.refreshExamCalls(career.studentId, career.matricolaId).getOrThrow()
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                        "Prenotazione cancellata"
                    )
                )
            } catch (e: Exception) {
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                        e.message ?: "Impossibile cancellare la prenotazione"
                    )
                )
            } finally {
                _isActionInProgress.value = false
            }
        }
    }

    fun printBooking(booking: ExamBooking) {
        viewModelScope.launch {
            _isActionInProgress.value = true
            try {
//                val document = examRepository.getBookingStatino(booking).getOrThrow() TODO: implement
//                _events.emit(
//                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.OpenDocument(
//                        document
//                    )
//                )
            } catch (e: Exception) {
                _events.emit(
                    it.attendance100.mybicocca.ui.screen.segreterie.SegreterieActionEvent.ShowMessage(
                        e.message ?: "Impossibile aprire lo statino"
                    )
                )
            } finally {
                _isActionInProgress.value = false
            }
        }
    }

    private suspend fun refreshCareer(career: Career, force: Boolean) {
        val key = SyncKeys.examBookings(career.matricolaId ?: career.studentId)
        val refreshBlock: suspend () -> Result<Unit> = {
            examRepository.refreshBookings(career.matricolaId)
        }

        if (force) {
            resourceSyncManager.refresh(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        } else {
            resourceSyncManager.refreshIfStale(key, SyncPolicies.Default, refreshBlock = refreshBlock)
        }
    }
}
