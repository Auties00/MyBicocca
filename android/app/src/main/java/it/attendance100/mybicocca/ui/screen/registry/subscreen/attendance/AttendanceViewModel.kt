package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.BuildConfig
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.data.deeplink.PendingPresenceScan
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendanceStatus
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.SessionAttendance
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.GetPendingAttendanceCoursesUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.MarkPresenceUseCase
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

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val getPendingCourses: GetPendingAttendanceCoursesUseCase,
    private val markPresence: MarkPresenceUseCase,
    private val parseScan: ParsePresenceScanUseCase,
    private val pendingPresenceScan: PendingPresenceScan,
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
            pendingPresenceScan.pending.filterNotNull().collect { raw ->
                pendingPresenceScan.consume()
                _events.send(AttendanceEvent.OpenRilevaSheet)
                submitScan(raw)
            }
        }
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    // Handles both an in-app QR scan and a typed lesson code: the parser resolves
    // which provider can register it.
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
                    _courses.value = Loadable.Loaded(
                        if (BuildConfig.DEBUG) list + demoCourses() else list,
                    )
                    _syncStatus.value = SyncStatus.Idle
                },
                onFailure = { cause -> _syncStatus.value = SyncStatus.Failed(cause) },
            )
        } finally {
            refreshMutex.unlock()
        }
    }

    // DEBUG-only showcase courses covering the attendance UI states: no recordings at
    // all, and recorded data on both sources; never present in release builds.
    private fun demoCourses(): List<CourseAttendance> = listOf(
        CourseAttendance(
            name = "Demo · Nessuna rilevazione",
            code = "DEMO-0",
            year = StudyYear(1),
            semester = Semester.Unknown,
            credits = 6f,
            teacherName = "Docente di prova",
            classroomAttendance = null,
            sessionAttendance = emptyList(),
        ),
        CourseAttendance(
            name = "Demo · Frequenza registrata",
            code = "DEMO-1",
            year = StudyYear(1),
            semester = Semester.Unknown,
            credits = 8f,
            teacherName = "Docente di prova",
            classroomAttendance = ClassroomAttendance(
                attendancePercentage = 82.0,
                lessonsAttended = 18,
                hoursCompleted = 44,
                requirementProgressPercentage = 96.0,
                status = ClassroomAttendanceStatus.Attending,
            ),
            sessionAttendance = listOf(
                SessionAttendance(
                    label = "Presenze LABORATORIO",
                    attendedSessions = 7,
                    recordedPercentage = 70.0,
                    totalSessions = 12,
                    overallPercentage = 58.0,
                    pointsLabel = "14/24",
                    bestPossiblePercentage = 91.0,
                ),
            ),
        ),
        CourseAttendance(
            name = "Demo · Solo registro e-learning",
            code = "DEMO-2",
            year = StudyYear(2),
            semester = Semester.Unknown,
            credits = 6f,
            teacherName = "Docente di prova",
            classroomAttendance = null,
            sessionAttendance = listOf(
                SessionAttendance(
                    label = "Presenze LEZIONE",
                    attendedSessions = 3,
                    recordedPercentage = 60.0,
                    totalSessions = 10,
                    overallPercentage = 30.0,
                ),
            ),
        ),
    )
}
