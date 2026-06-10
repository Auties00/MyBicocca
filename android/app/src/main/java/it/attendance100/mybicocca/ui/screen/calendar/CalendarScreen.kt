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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.screen.calendar.component.CalendarSegmentedControl
import it.attendance100.mybicocca.ui.screen.calendar.component.DayView
import it.attendance100.mybicocca.ui.screen.calendar.component.MonthView
import it.attendance100.mybicocca.ui.screen.calendar.component.TIMELINE_ZOOM_DEFAULT
import it.attendance100.mybicocca.ui.screen.calendar.component.TodayFab
import it.attendance100.mybicocca.ui.screen.calendar.component.WeekView
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarOneShotEvent
import it.attendance100.mybicocca.ui.screen.calendar.state.CalendarViewMode
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.eventDetail.EventDetailSheet
import it.attendance100.mybicocca.ui.screen.calendar.subscreen.monthAgenda.MonthAgendaSheet
import it.attendance100.mybicocca.ui.screen.calendar.theme.ProvideEventPalette

/**
 * The calendar tab: the student's unified schedule — lessons, booked exams, assignment
 * deadlines, segreteria appointments and library seat reservations — switchable between
 * day, week and month layouts through the segmented control on top. Day and week are
 * pinch-zoomable timelines sharing one zoom level; month is a busy-tinted grid with a
 * draggable agenda sheet for the selected day. A "go to today" FAB floats whenever today
 * is off-screen, mode changes slide the content horizontally toward the picked mode, and
 * the whole tab is pull-to-refresh. Tapping any event opens its detail sheet, which shows
 * whenever the selected event id resolves to an event in the loaded month or day data.
 *
 * A month's first-ever load renders the empty calendar with the refresh indicator pinned
 * for the entire initial fetch (the same language as the e-learning tab), driven by
 * [CalendarViewModel.initialLoading] on top of the plain refreshing status. A failed sync
 * replaces the calendar with a centered error state and a retry button; refresh failures
 * raise no transient snackbar.
 *
 * The agenda sheet and the FAB draw in their own popup windows, which no in-content cover
 * can clip — they would bleed over anything composed above the tab — so the screen hides
 * them itself: they show only while this is the visible tab, no shell chrome covers it,
 * the keyboard is closed and the error state is not replacing the calendar.
 *
 * @param isActive true only while this is the visible tab. The shell keeps every tab
 *   composed in its pager cache, so per-tab chrome must be claimed on each activation
 *   rather than once on composition; the calendar offers no filter chrome and clears any
 *   registration another tab left behind.
 * @param coverProgress how far shell-level chrome covers the tab — the max of the
 *   sub-page transition and the search-overlay open fraction, 0 when the tab root is
 *   fully on top. Gating the popups on it hides them the instant a sub-page push/pop or a
 *   search open begins, in both directions and during predictive back. Null means no
 *   shell hosts the screen (previews) and counts as settled.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    coverProgress: State<Float>? = null,
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    onOpenCourse: (CourseId) -> Unit = {},
    onOpenAssignment: (assignmentId: Int, courseId: Int) -> Unit = { _, _ -> },
    onOpenReservation: (CalendarEvent) -> Unit = {},
    bottomNavBarPadding: PaddingValues,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val weekStart by viewModel.weekStart.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val initialLoading by viewModel.initialLoading.collectAsStateWithLifecycle()
    val eventsByDay by viewModel.eventsByDay.collectAsStateWithLifecycle()
    val dayEventsLoadable by viewModel.dayEvents.collectAsStateWithLifecycle()
    val monthEventsLoadable by viewModel.events.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()
    val coursesByActivityCode by viewModel.coursesByActivityCode.collectAsStateWithLifecycle()

    LaunchedEffect(isActive) { if (isActive) onProvideFilterToggle(null) }

    LaunchedEffect(Unit) {
        viewModel.oneShotEvents.collect { event ->
            when (event) {
                is CalendarOneShotEvent.RefreshFailed -> Unit
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
    var timelineZoom by rememberSaveable { mutableFloatStateOf(TIMELINE_ZOOM_DEFAULT) }

    ProvideEventPalette {
    Box(modifier = modifier
        .testTag(CalendarTestTags.ROOT)
        .fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = syncStatus is SyncStatus.Refreshing || initialLoading,
            onRefresh = viewModel::pullToRefresh,
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            val failure = syncStatus as? SyncStatus.Failed
            if (failure != null) {
                EmptyState(
                    icon = Icons.Outlined.CloudOff,
                    title = "Sincronizzazione del calendario non riuscita",
                    body = failure.cause.friendlyMessage(),
                    action = { RetryButton(onClick = viewModel::pullToRefresh) },
                    modifier = Modifier.testTag(CalendarTestTags.STATE_ERROR),
                )
            } else {
            Column(modifier = Modifier
                .testTag(CalendarTestTags.STATE_CONTENT)
                .fillMaxSize()) {
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
                    when (mode) {
                        CalendarViewMode.DAY -> DayView(
                            selectedDay = selectedDay,
                            eventsByDay = eventsByDay,
                            onSelectDay = viewModel::selectDay,
                            onEventClick = { viewModel.openEventDetail(it.id) },
                            modifier = Modifier.fillMaxSize(),
                            zoom = timelineZoom,
                            onZoomChange = { timelineZoom = it },
                        )
                        CalendarViewMode.WEEK -> WeekView(
                            weekStart = weekStart,
                            eventsByDay = eventsByDay,
                            onSelectDay = viewModel::selectDay,
                            onEventClick = { viewModel.openEventDetail(it.id) },
                            modifier = Modifier.fillMaxSize(),
                            zoom = timelineZoom,
                            onZoomChange = { timelineZoom = it },
                        )
                        CalendarViewMode.MONTH -> MonthView(
                            yearMonth = selectedMonth,
                            selectedDay = selectedDay,
                            eventsByDay = eventsByDay,
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
        }

        val keyboardOpen = androidx.compose.foundation.layout.WindowInsets.ime
            .asPaddingValues()
            .calculateBottomPadding() > 0.dp

        val chromeVisible = isActive && (coverProgress?.value ?: 0f) < 0.01f &&
            syncStatus !is SyncStatus.Failed

        if (chromeVisible && agendaPresence > 0.01f && !keyboardOpen) {
            MonthAgendaSheet(
                selectedDay = selectedDay,
                events = (eventsByDay[selectedDay] ?: emptyList()),
                onEventClick = { viewModel.openEventDetail(it.id) },
                elearningCoursesFor = { it.activityCode?.let(coursesByActivityCode::get).orEmpty() },
                onOpenCourse = onOpenCourse,
                onOpenAssignment = onOpenAssignment,
                onOpenReservation = onOpenReservation,
                progress = agendaProgress,
                presence = agendaPresence,
                bottomNavBarPadding = bottomNavBarPadding,
                sheetHeight = monthSheetSize.height
            )
        }

        if (chromeVisible && !keyboardOpen) {
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

        val selected = remember(selectedEventId, monthEventsLoadable, dayEventsLoadable) {
            val id = selectedEventId ?: return@remember null
            monthEventsLoadable.valueOrNull()?.firstOrNull { it.id == id }
                ?: dayEventsLoadable.valueOrNull()?.firstOrNull { it.id == id }
        }
        if (selected != null) {
            EventDetailSheet(
                event = selected,
                elearningCourses = selected.activityCode?.let(coursesByActivityCode::get).orEmpty(),
                onOpenCourse = onOpenCourse,
                onOpenAssignment = onOpenAssignment,
                onOpenReservation = onOpenReservation,
                onDismiss = viewModel::closeEventDetail,
            )
        }
    }
    }
}
