package it.attendance100.mybicocca.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.repository.CalendarRepository
import it.attendance100.mybicocca.data.sync.ResourceSyncManager
import it.attendance100.mybicocca.data.sync.SyncKeys
import it.attendance100.mybicocca.data.sync.SyncPolicies
import it.attendance100.mybicocca.data.sync.SyncUiState
import it.attendance100.mybicocca.util.NetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject

enum class CalendarViewMode { LIST, WEEK, MONTH }

data class TimeRange(
    val startMinutes: Int = 7 * 60,
    val endMinutes: Int = 22 * 60,
) {
    val startHour: Int get() = startMinutes / 60
    val startMinute: Int get() = startMinutes % 60
    val endHour: Int get() = endMinutes / 60
    val endMinute: Int get() = endMinutes % 60
    val isDefault: Boolean get() = startMinutes == 7 * 60 && endMinutes == 22 * 60
    fun contains(time: LocalTime): Boolean {
        val m = time.hour * 60 + time.minute
        return m in startMinutes until endMinutes
    }
}

data class LocationFilter(
    val selectedRooms: Set<String> = emptySet(),
) {
    val isEmpty: Boolean get() = selectedRooms.isEmpty()
    val count: Int get() = selectedRooms.size
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val resourceSyncManager: ResourceSyncManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    // Navigation
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _viewMode = MutableStateFlow(CalendarViewMode.LIST)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    private val _displayedWeekStart = MutableStateFlow(LocalDate.now().with(DayOfWeek.MONDAY))
    val displayedWeekStart: StateFlow<LocalDate> = _displayedWeekStart.asStateFlow()

    // Data
    val eventsForMonth: StateFlow<List<CalendarEvent>> = _currentMonth
        .flatMapLatest { calendarRepository.observeEventsForMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val futureEvents: StateFlow<List<CalendarEvent>> = calendarRepository.observeFutureEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val syncState: StateFlow<SyncUiState> = _currentMonth
        .flatMapLatest { month -> resourceSyncManager.observe(SyncKeys.calendar(month)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    val isRefreshing: StateFlow<Boolean> = syncState
        .map { it.isRefreshing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val error: StateFlow<String?> = syncState
        .map { it.errorMessage }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Filters
    private val _activeFilters = MutableStateFlow<Set<EventType>>(emptySet())
    val activeFilters: StateFlow<Set<EventType>> = _activeFilters.asStateFlow()

    private val _timeRange = MutableStateFlow<TimeRange?>(null)
    val timeRange: StateFlow<TimeRange?> = _timeRange.asStateFlow()

    private val _locationFilter = MutableStateFlow(LocationFilter())
    val locationFilter: StateFlow<LocationFilter> = _locationFilter.asStateFlow()

    // Dialogs
    private val _selectedEvent = MutableStateFlow<CalendarEvent?>(null)
    val selectedEvent: StateFlow<CalendarEvent?> = _selectedEvent.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    // One-shot events
    private val _scrollToDateEvent = MutableSharedFlow<LocalDate>()
    val scrollToDateEvent: SharedFlow<LocalDate> = _scrollToDateEvent.asSharedFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            _currentMonth.collectLatest { month ->
                refreshMonth(month, force = false)
            }
        }
    }

    // === Navigation ===

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        val newMonth = YearMonth.from(date)
        if (_currentMonth.value != newMonth) _currentMonth.value = newMonth
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun previousMonth() {
        val newMonth = _currentMonth.value.minusMonths(1)
        val newDay = minOf(_selectedDate.value.dayOfMonth, newMonth.lengthOfMonth())
        _currentMonth.value = newMonth
        _selectedDate.value = newMonth.atDay(newDay)
    }

    fun nextMonth() {
        val newMonth = _currentMonth.value.plusMonths(1)
        val newDay = minOf(_selectedDate.value.dayOfMonth, newMonth.lengthOfMonth())
        _currentMonth.value = newMonth
        _selectedDate.value = newMonth.atDay(newDay)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _currentMonth.value = YearMonth.now()
        _viewMode.value = CalendarViewMode.LIST
        viewModelScope.launch { _scrollToDateEvent.emit(today) }
    }

    fun jumpToDate(date: LocalDate) {
        _selectedDate.value = date
        _currentMonth.value = YearMonth.from(date)
        viewModelScope.launch { _scrollToDateEvent.emit(date) }
    }

    fun jumpToNextExam() = jumpToNextEventOfType(EventType.EXAM, "Nessun esame in programma")

    fun jumpToNextLesson() = jumpToNextEventOfType(EventType.LECTURE, "Nessuna lezione in programma")

    private fun jumpToNextEventOfType(type: EventType, fallbackMessage: String) {
        val now = LocalDateTime.now()
        val next = futureEvents.value
            .filter { it.eventType == type && it.startDateTime.isAfter(now) }
            .minByOrNull { it.startDateTime }

        if (next != null) {
            jumpToDate(next.date)
            _showFilters.value = false
            _viewMode.value = CalendarViewMode.LIST
        } else {
            viewModelScope.launch { _snackbarEvent.emit(fallbackMessage) }
        }
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun setDisplayedWeekStart(weekStart: LocalDate) {
        _displayedWeekStart.value = weekStart
    }

    // === Data ===

    fun refresh() {
        val month = _currentMonth.value
        viewModelScope.launch {
            refreshMonth(month, force = true)
        }
    }

    fun refreshIfStale() {
        viewModelScope.launch {
            refreshMonth(_currentMonth.value, force = false)
        }
    }

    // === Filters ===

    fun toggleFilter(eventType: EventType) {
        _activeFilters.value = if (eventType in _activeFilters.value)
            _activeFilters.value - eventType else _activeFilters.value + eventType
    }

    fun setTimeRange(range: TimeRange?) {
        _timeRange.value = range
    }

    fun setLocationFilter(filter: LocationFilter) {
        _locationFilter.value = filter
    }

    fun clearFilters() {
        _activeFilters.value = emptySet()
        _timeRange.value = null
        _locationFilter.value = LocationFilter()
    }

    // === Dialogs ===

    fun selectEvent(event: CalendarEvent?) {
        _selectedEvent.value = event
    }

    fun setShowDatePicker(show: Boolean) {
        _showDatePicker.value = show
    }

    fun toggleFiltersVisibility() {
        _showFilters.value = !_showFilters.value
    }

    fun clearError() {
        resourceSyncManager.clearError(SyncKeys.calendar(_currentMonth.value))
    }

    // === Computed helpers ===

    val hasActiveFilters: Boolean
        get() {
            val tr = _timeRange.value
            val hasTimeFilter = tr != null && !tr.isDefault
            return _activeFilters.value.isNotEmpty() || hasTimeFilter || !_locationFilter.value.isEmpty
        }

    private suspend fun refreshMonth(month: YearMonth, force: Boolean) {
        val key = SyncKeys.calendar(month)
        val range = month.atDay(1)..month.atEndOfMonth()

        if (force) {
            resourceSyncManager.refresh(key, SyncPolicies.Default) {
                calendarRepository.refreshAll(range)
            }
        } else {
            resourceSyncManager.refreshIfStale(key, SyncPolicies.Default) {
                calendarRepository.refreshAll(range)
            }
        }
    }
}
