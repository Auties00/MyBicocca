package it.attendance100.mybicocca.ui.screen.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.calendar.component.CalendarSegmentedControl
import it.attendance100.mybicocca.ui.screen.calendar.component.DayView
import it.attendance100.mybicocca.ui.screen.calendar.component.MonthView
import it.attendance100.mybicocca.ui.screen.calendar.component.TodayFab
import it.attendance100.mybicocca.ui.screen.calendar.component.WeekView
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.EventDetailSheet
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.monthAgenda.MonthAgendaSheet
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarOneShotEvent
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import it.attendance100.mybicocca.ui.screen.calendar.theme.ProvideEventPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    bottomNavBarPadding: PaddingValues,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val weekStart by viewModel.weekStart.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val eventsByDay by viewModel.eventsByDay.collectAsStateWithLifecycle()
    val dayEventsLoadable by viewModel.dayEvents.collectAsStateWithLifecycle()
    val monthEventsLoadable by viewModel.events.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current

    // No filter chips in v1; clear any registration the previous tab left.
    LaunchedEffect(Unit) { onProvideFilterToggle(null) }

    LaunchedEffect(Unit) {
        viewModel.oneShotEvents.collect { event ->
            when (event) {
                is CalendarOneShotEvent.RefreshFailed -> snackbar.showError(
                    message = "Sincronizzazione del calendario non riuscita",
                    cause = event.cause,
                )
                CalendarOneShotEvent.RequireSignIn -> Unit
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    val agendaProgress = remember { Animatable(0f) }
    val agendaPresence by animateFloatAsState(
        targetValue = if (viewMode == CalendarViewMode.MONTH) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "agenda_presence",
    )
    LaunchedEffect(viewMode) {
        if (viewMode != CalendarViewMode.MONTH) agendaProgress.snapTo(0f)
    }

    var monthSheetSize by remember { mutableStateOf(IntSize.Zero) }

    ProvideEventPalette {
    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = syncStatus is SyncStatus.Refreshing,
            onRefresh = viewModel::pullToRefresh,
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))
                CalendarSegmentedControl(
                    viewMode = viewMode,
                    selectedDay = selectedDay,
                    weekStart = weekStart,
                    selectedMonth = selectedMonth,
                    onSelect = viewModel::selectViewMode,
                )
                Spacer(Modifier.height(16.dp))

                val viewSpatial = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
                val viewEffects = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        val dir = if (forward) 1 else -1
                        (slideInHorizontally(viewSpatial) { it * dir / 6 } + fadeIn(viewEffects)) togetherWith
                            (slideOutHorizontally(viewSpatial) { -it * dir / 6 } + fadeOut(viewEffects))
                    },
                    label = "calendar_view_transition",
                    modifier = Modifier.fillMaxSize()
                ) { mode ->
                    val filteredEventsByDay = remember(searchQuery, eventsByDay) {
                        if (searchQuery.isBlank()) eventsByDay
                        else eventsByDay.mapValues { it.value.applySearch(searchQuery) }
                    }
                    when (mode) {
                        CalendarViewMode.DAY -> DayView(
                            selectedDay = selectedDay,
                            eventsByDay = filteredEventsByDay,
                            onSelectDay = viewModel::selectDay,
                            onEventClick = { viewModel.openEventDetail(it.id) },
                            modifier = Modifier.fillMaxSize(),
                        )
                        CalendarViewMode.WEEK -> WeekView(
                            selectedDay = selectedDay,
                            weekStart = weekStart,
                            eventsByDay = filteredEventsByDay,
                            onSelectDay = viewModel::selectDay,
                            onEventClick = { viewModel.openEventDetail(it.id) },
                            modifier = Modifier.fillMaxSize(),
                        )
                        CalendarViewMode.MONTH -> MonthView(
                            yearMonth = selectedMonth,
                            selectedDay = selectedDay,
                            eventsByDay = filteredEventsByDay,
                            onSelectDay = viewModel::selectDay,
                            onSelectMonth = viewModel::selectMonth,
                            onEventClick = { viewModel.openEventDetail(it.id) },
                            onMonthSheetSizeChanged = { monthSheetSize = it },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        val keyboardOpen = androidx.compose.foundation.layout.WindowInsets.ime
            .asPaddingValues()
            .calculateBottomPadding() > 0.dp

        if (agendaPresence > 0.01f && !keyboardOpen) {
            MonthAgendaSheet(
                selectedDay = selectedDay,
                events = (eventsByDay[selectedDay] ?: emptyList()),
                onEventClick = { viewModel.openEventDetail(it.id) },
                progress = agendaProgress,
                presence = agendaPresence,
                bottomNavBarPadding = bottomNavBarPadding,
                sheetHeight = monthSheetSize.height
            )
        }

        if (!keyboardOpen) {
            androidx.compose.runtime.key(agendaPresence > 0.01f) {
                TodayFab(
                    viewMode = viewMode,
                    selectedDay = selectedDay,
                    weekStart = weekStart,
                    selectedMonth = selectedMonth,
                    agendaProgress = agendaProgress.value,
                    bottomNavBarPadding = bottomNavBarPadding,
                    onJumpToToday = { viewModel.selectDay(java.time.LocalDate.now()) },
                )
            }
        }

        // Event detail bottom sheet — open iff selectedEventId resolves to a known event.
        val selected = remember(selectedEventId, monthEventsLoadable, dayEventsLoadable) {
            val id = selectedEventId ?: return@remember null
            monthEventsLoadable.valueOrNull()?.firstOrNull { it.id == id }
                ?: dayEventsLoadable.valueOrNull()?.firstOrNull { it.id == id }
        }
        if (selected != null) {
            EventDetailSheet(event = selected, onDismiss = viewModel::closeEventDetail)
        }
    }
    }
}

private fun List<CalendarEvent>.applySearch(query: String): List<CalendarEvent> {
    if (query.isBlank()) return this
    val needle = query.trim().lowercase()
    return filter { e ->
        e.title.lowercase().contains(needle) ||
            (e.shortLabel?.lowercase()?.contains(needle) == true) ||
            (e.location?.room?.lowercase()?.contains(needle) == true) ||
            (e.location?.building?.lowercase()?.contains(needle) == true)
    }
}
