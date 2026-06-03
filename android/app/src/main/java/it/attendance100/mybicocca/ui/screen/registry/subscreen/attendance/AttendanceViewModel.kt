package it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.attendance.GetPendingAttendanceCoursesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val getPendingCourses: GetPendingAttendanceCoursesUseCase,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val activeCareerId: StateFlow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _courses = MutableStateFlow<Loadable<List<CourseAttendance>>>(Loadable.NotYetLoaded)
    val courses: StateFlow<Loadable<List<CourseAttendance>>> = _courses.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    // 0 = all years; survives process death because it's user input.
    val selectedYear: StateFlow<StudyYear?> = savedState
        .getStateFlow(KEY_YEAR_FILTER, 0)
        .map { value -> value.takeIf { it > 0 }?.let(::StudyYear) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val refreshMutex = Mutex()

    init {
        viewModelScope.launch {
            activeCareerId.filterNotNull().collect { careerId ->
                fetch(careerId)
            }
        }
    }

    fun selectYear(year: StudyYear?) {
        savedState[KEY_YEAR_FILTER] = year?.value ?: 0
    }

    fun refresh() {
        val careerId = activeCareerId.value ?: return
        viewModelScope.launch { fetch(careerId) }
    }

    fun pullToRefresh() {
        val careerId = activeCareerId.value ?: return
        _courses.value = Loadable.NotYetLoaded
        viewModelScope.launch { fetch(careerId) }
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

    private companion object {
        const val KEY_YEAR_FILTER = "attendance_year_filter"
    }
}
