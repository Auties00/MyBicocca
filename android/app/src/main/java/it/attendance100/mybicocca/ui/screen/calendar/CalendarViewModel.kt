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
import it.attendance100.mybicocca.domain.usecase.calendar.IsCalendarMonthFreshUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveDayEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveMonthEventsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.ObserveMonthHydratedUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.PrefetchAdjacentMonthsUseCase
import it.attendance100.mybicocca.domain.usecase.calendar.RefreshCalendarMonthUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.course.ObserveCoursesByActivityCodeUseCase
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

/**
 * Backs the calendar tab: a unified schedule of lessons, booked exams, assignment
 * deadlines, appointments and library reservations, read month-by-month from the local
 * cache as the source of truth and refreshed per month on demand.
 *
 * Selection state — [viewMode], [selectedMonth], [selectedDay] and [selectedEventId] — is
 * user input persisted through [SavedStateHandle] so it survives process death;
 * [weekStart] derives from the selected day. Cached-data streams: [events] and
 * [dayEvents] expose the visible month and day as [Loadable], so the UI can tell an
 * unloaded cache from an empty one; [eventsByDay] merges the visible month with both
 * neighbors so week and day views spanning month edges render seamlessly;
 * [coursesByActivityCode] maps activity codes to the e-learning courses they resolve to.
 * Sync state is independent of the data: [syncStatus] tracks the in-flight refresh,
 * [initialLoading] flags a month awaiting its first-ever sync, and [oneShotEvents]
 * delivers fire-exactly-once failures.
 *
 * A career or month change triggers a freshness-gated refresh plus an adjacent-month
 * prefetch; [pullToRefresh] forces one. The select/shift methods move the visible
 * day/week/month, and [openEventDetail]/[closeEventDetail] drive the detail sheet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    observeActiveAccount: ObserveActiveAccountUseCase,
    private val observeMonthEvents: ObserveMonthEventsUseCase,
    private val observeDayEvents: ObserveDayEventsUseCase,
    observeMonthHydrated: ObserveMonthHydratedUseCase,
    private val isMonthFresh: IsCalendarMonthFreshUseCase,
    private val refreshMonth: RefreshCalendarMonthUseCase,
    private val prefetchAdjacent: PrefetchAdjacentMonthsUseCase,
    observeCoursesByActivityCode: ObserveCoursesByActivityCodeUseCase,
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

    /**
     * E-learning course editions keyed by university activity code, letting the event
     * detail offer "Apri corso" whenever an event's course also exists on e-learning.
     */
    val coursesByActivityCode: StateFlow<Map<String, List<EnrolledCourse>>> = activeAccountId
        .flatMapLatest { account ->
            if (account == null) flowOf(emptyMap())
            else observeCoursesByActivityCode(account)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), emptyMap())

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

    /**
     * True until the visible month completes its first-ever sync: the screen keeps the
     * pull-to-refresh indicator pinned over the still-empty calendar. Gated on the
     * persisted sync stamp — a cache snapshot, not a timing heuristic — so an
     * already-synced month renders instantly even while a background refresh is in flight.
     */
    val initialLoading: StateFlow<Boolean> =
        combine(
            combine(activeCareerId, selectedMonth) { c, ym -> c to ym }
                .flatMapLatest { (c, ym) ->
                    if (c == null) flowOf(true)
                    else observeMonthHydrated(c, ym)
                },
            _syncStatus,
        ) { hydrated, status ->
            !hydrated && status !is SyncStatus.Failed
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_KEEP_ALIVE_MS), false)

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

    /** Selects [date], keeping the month aligned so [events] and [eventsByDay] always cover the selected day's month. */
    fun selectDay(date: LocalDate) {
        savedState[KEY_DAY] = date.toString()
        val ym = YearMonth.from(date)
        if (ym != selectedMonth.value) savedState[KEY_MONTH] = ym.toString()
    }

    /** Moves the selected day by [deltaDays], the visible week and month snapping along. */
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

    /**
     * Awaits the month refresh unconditionally — TTL-fresh sources no-op inside — but
     * raises the sync indicator only when network work is actually expected: swiping
     * across already-fresh months must not flash the spinner.
     */
    private suspend fun runRefresh(careerId: CareerId, yearMonth: YearMonth, force: Boolean) {
        val showIndicator = force || runCatching { !isMonthFresh(careerId, yearMonth) }.getOrDefault(true)
        if (showIndicator) _syncStatus.value = SyncStatus.Refreshing
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

