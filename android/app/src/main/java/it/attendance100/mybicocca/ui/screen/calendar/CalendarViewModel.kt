package it.attendance100.mybicocca.ui.screen.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveDayEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveMonthEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.PrefetchAdjacentMonthsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.RefreshCalendarMonthUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveEnrolledCoursesUseCase
import it.attendance100.mybicocca.ui.screen.calendar.ext.weekStartFor
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarOneShotEvent
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeMonthEvents: ObserveMonthEventsUseCase,
    private val observeDayEvents: ObserveDayEventsUseCase,
    private val refreshMonth: RefreshCalendarMonthUseCase,
    private val prefetchAdjacent: PrefetchAdjacentMonthsUseCase,
    observeEnrolledCourses: ObserveEnrolledCoursesUseCase,
) : ViewModel() {

    val viewMode: StateFlow<CalendarViewMode> = savedState
        .getStateFlow(KEY_VIEW_MODE, CalendarViewMode.DAY.name)
        .map { runCatching { CalendarViewMode.valueOf(it) }.getOrDefault(CalendarViewMode.DAY) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CalendarViewMode.DAY)

    val selectedMonth: StateFlow<YearMonth> = savedState
        .getStateFlow(KEY_MONTH, YearMonth.now().toString())
        .map { runCatching { YearMonth.parse(it) }.getOrDefault(YearMonth.now()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, YearMonth.now())

    val selectedDay: StateFlow<LocalDate> = savedState
        .getStateFlow(KEY_DAY, LocalDate.now().toString())
        .map { runCatching { LocalDate.parse(it) }.getOrDefault(LocalDate.now()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDate.now())

    val weekStart: StateFlow<LocalDate> = selectedDay
        .map { weekStartFor(it) }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, weekStartFor(LocalDate.now()))

    val selectedEventId: StateFlow<CalendarEventId?> = savedState
        .getStateFlow<String?>(KEY_SELECTED_EVENT, null)
        .map { it?.let(::CalendarEventId) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val activeCareerId: Flow<CareerId?> = observeActiveAccount()
        .map { it?.academic?.selectedCareerId }
        .distinctUntilChanged()

    private val activeAccountId: Flow<AccountId?> = observeActiveAccount()
        .map { it?.id }
        .distinctUntilChanged()

    val enrolledCourses: StateFlow<List<EnrolledCourse>> =
        activeAccountId
            .flatMapLatest { accountId ->
                if (accountId == null) flowOf(emptyList())
                else observeEnrolledCourses(accountId).map { it.valueOrNull().orEmpty() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyList())

    val events: StateFlow<Loadable<List<CalendarEvent>>> =
        combine(activeCareerId, selectedMonth) { c, ym -> c to ym }
            .flatMapLatest { (c, ym) ->
                if (c == null) flowOf(Loadable.Loaded(emptyList()))
                else observeMonthEvents(c, ym)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    val eventsByDay: StateFlow<Map<LocalDate, List<CalendarEvent>>> =
        combine(activeCareerId, selectedMonth) { c, ym -> c to ym }
            .flatMapLatest { (c, ym) ->
                if (c == null) flowOf(emptyMap())
                else combine(
                    observeMonthEvents(c, ym.minusMonths(1)),
                    observeMonthEvents(c, ym),
                    observeMonthEvents(c, ym.plusMonths(1)),
                ) { prev, curr, next ->
                    (
                        prev.valueOrNull().orEmpty() +
                            curr.valueOrNull().orEmpty() +
                            next.valueOrNull().orEmpty()
                        ).groupBy(CalendarEvent::date)
                }
            }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyMap())

    val dayEvents: StateFlow<Loadable<List<CalendarEvent>>> =
        combine(activeCareerId, selectedDay) { c, d -> c to d }
            .flatMapLatest { (c, d) ->
                if (c == null) flowOf(Loadable.Loaded(emptyList()))
                else observeDayEvents(c, d)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), Loadable.NotYetLoaded)

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val oneShotChannel = Channel<CalendarOneShotEvent>(Channel.BUFFERED)
    val oneShotEvents: Flow<CalendarOneShotEvent> = oneShotChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(activeCareerId.filterNotNull(), selectedMonth) { c, ym -> c to ym }
                .distinctUntilChanged()
                .collect { (c, ym) ->
                    runRefresh(c, ym, force = false)
                    launch { runCatching { prefetchAdjacent(c, ym) } }
                }
        }
    }

    fun selectViewMode(mode: CalendarViewMode) {
        savedState[KEY_VIEW_MODE] = mode.name
    }

    fun selectMonth(yearMonth: YearMonth) {
        savedState[KEY_MONTH] = yearMonth.toString()
    }

    fun selectDay(date: LocalDate) {
        savedState[KEY_DAY] = date.toString()
        // Keep month state aligned so events/eventsByDay covers the selected day's month.
        val ym = YearMonth.from(date)
        if (ym != selectedMonth.value) savedState[KEY_MONTH] = ym.toString()
    }

    // Move selected day by N days, snapping the visible week/month to follow.
    fun shiftSelectedDay(deltaDays: Long) {
        selectDay(selectedDay.value.plusDays(deltaDays))
    }

    fun shiftSelectedMonth(deltaMonths: Long) {
        selectMonth(selectedMonth.value.plusMonths(deltaMonths))
    }

    fun openEventDetail(id: CalendarEventId) {
        savedState[KEY_SELECTED_EVENT] = id.value
    }

    fun closeEventDetail() {
        savedState[KEY_SELECTED_EVENT] = null
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            val careerId = activeCareerId.filterNotNull().first()
            runRefresh(careerId, selectedMonth.value, force = true)
        }
    }

    private suspend fun runRefresh(careerId: CareerId, yearMonth: YearMonth, force: Boolean) {
        _syncStatus.value = SyncStatus.Refreshing
        runCatching { refreshMonth(careerId, yearMonth, force) }
            .onSuccess { _syncStatus.value = SyncStatus.Idle }
            .onFailure {
                _syncStatus.value = SyncStatus.Failed(it)
                oneShotChannel.trySend(CalendarOneShotEvent.RefreshFailed(it))
            }
    }

    private companion object {
        const val KEY_VIEW_MODE = "calendar_view_mode"
        const val KEY_MONTH = "calendar_year_month"
        const val KEY_DAY = "calendar_selected_day"
        const val KEY_SELECTED_EVENT = "calendar_selected_event_id"
        const val STATE_KEEP_ALIVE_MS = 5_000L
    }
}

