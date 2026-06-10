package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ConsumePresenceScanUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.GetPendingAttendanceCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.MarkPresenceUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ObservePendingPresenceScanUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.ParsePresenceScanUseCase
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state.AttendanceEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.state.MarkUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

/**
 * Owns the Presenze sheet: the pending-attendance course list for the active career and the
 * presence-marking flow.
 *
 * Streams by role: [courses] is the data to render, [syncStatus] the independent refresh
 * state, [markState] the lifecycle of a presence submission, and [events] one-shot requests
 * to the UI. The course list is fetched on every active-career change. A presence QR scanned
 * outside the app is observed from the pending-scan store and consumed on arrival: the
 * ViewModel emits [AttendanceEvent.OpenRilevaSheet] and submits the payload immediately, so
 * the UI lands directly on the in-flight progress page.
 *
 * Public actions: [refresh] re-fetches the course list, [submitScan] registers a scanned QR
 * payload or typed lesson code, and [resetMarkState] rearms the marking flow.
 */
@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val getPendingCourses: GetPendingAttendanceCoursesUseCase,
    private val markPresence: MarkPresenceUseCase,
    private val parseScan: ParsePresenceScanUseCase,
    observePendingPresenceScan: ObservePendingPresenceScanUseCase,
    consumePresenceScan: ConsumePresenceScanUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _courses = MutableStateFlow<Loadable<List<CourseAttendance>>>(Loadable.NotYetLoaded)
    val courses: StateFlow<Loadable<List<CourseAttendance>>> = _courses.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _markState = MutableStateFlow<MarkUiState>(MarkUiState.Idle)
    val markState: StateFlow<MarkUiState> = _markState.asStateFlow()

    private val _events = Channel<AttendanceEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { careerId -> fetch(careerId) }
        }
        viewModelScope.launch {
            observePendingPresenceScan().filterNotNull().collect { raw ->
                consumePresenceScan()
                _events.send(AttendanceEvent.OpenRilevaSheet)
                submitScan(raw)
            }
        }
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    /**
     * Handles both an in-app QR scan and a typed lesson code: the parser resolves which
     * provider can register it. A recorded outcome triggers a course-list refresh.
     */
    fun submitScan(raw: String) {
        val careerId = activeCareerId.value
        if (careerId == null) {
            _markState.value = MarkUiState.Done(PresenceMarkOutcome.Failed("Nessuna carriera attiva"))
            return
        }
        viewModelScope.launch {
            _markState.value = MarkUiState.Submitting
            val outcome = markPresence(parseScan(raw), careerId)
            _markState.value = MarkUiState.Done(outcome)
            if (outcome is PresenceMarkOutcome.Recorded) refresh()
        }
    }

    fun resetMarkState() {
        _markState.value = MarkUiState.Idle
    }

    private suspend fun fetch(careerId: CareerId) {
        if (!refreshMutex.tryLock()) return
        try {
            _syncStatus.value = SyncStatus.Refreshing
            runCatching { getPendingCourses(careerId) }.fold(
                onSuccess = { list ->
                    _courses.value = Loadable.Loaded(list)
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }
}
