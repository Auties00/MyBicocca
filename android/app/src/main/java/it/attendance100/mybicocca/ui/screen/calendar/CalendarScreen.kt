package it.attendance100.mybicocca.ui.screen.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.component.NetworkStatusBar
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import it.attendance100.mybicocca.ui.component.calendar.dialog.DatePickerDialog
import it.attendance100.mybicocca.ui.component.calendar.dialog.EventDetailDialog
import it.attendance100.mybicocca.ui.component.calendar.filter.CalendarFilterModal
import it.attendance100.mybicocca.ui.component.calendar.header.CalendarTopBar
import it.attendance100.mybicocca.ui.component.calendar.header.HorizontalDaySelector
import it.attendance100.mybicocca.ui.component.calendar.view.DayTimelineView
import it.attendance100.mybicocca.ui.component.calendar.view.MonthGridView
import it.attendance100.mybicocca.ui.component.calendar.view.WeekGridView
import it.attendance100.mybicocca.ui.theme.BackgroundColor
import it.attendance100.mybicocca.ui.theme.GrayColorDark
import it.attendance100.mybicocca.ui.theme.GrayColorLight
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarRoute(
    searchQuery: String = "",
    onProvideFilterToggle: ((() -> Unit)?) -> Unit = {},
    onFilterActiveChanged: (Boolean) -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(
        checkNotNull<ViewModelStoreOwner>(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    // Collect all state
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val displayedWeekStart by viewModel.displayedWeekStart.collectAsStateWithLifecycle()
    val eventsForMonth by viewModel.eventsForMonth.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val activeFilters by viewModel.activeFilters.collectAsStateWithLifecycle()
    val timeRange by viewModel.timeRange.collectAsStateWithLifecycle()
    val locationFilter by viewModel.locationFilter.collectAsStateWithLifecycle()
    val selectedEvent by viewModel.selectedEvent.collectAsStateWithLifecycle()
    val showDatePicker by viewModel.showDatePicker.collectAsStateWithLifecycle()
    val showFilters by viewModel.showFilters.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    // Register filter callbacks with parent
    DisposableEffect(viewModel) {
        onProvideFilterToggle { viewModel.toggleFiltersVisibility() }
        onDispose { onProvideFilterToggle(null) }
    }
    LaunchedEffect(showFilters, activeFilters, timeRange, locationFilter) {
        onFilterActiveChanged(showFilters || viewModel.hasActiveFilters)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { snackbarHostState.showSnackbar(it) }
    }
    // Derived state
    val filteredEvents = remember(eventsForMonth, activeFilters, timeRange, locationFilter, searchQuery) {
        CalendarUtils.filterEvents(eventsForMonth, activeFilters, timeRange, locationFilter)
            .filter { event ->
                searchQuery.isBlank() || listOfNotNull(
                    event.title,
                    event.teacherName,
                    event.location,
                    event.buildingCode,
                ).any { it.contains(searchQuery, ignoreCase = true) }
            }
    }
    val buildingsWithRooms = remember(eventsForMonth) {
        CalendarUtils.extractBuildingsWithRooms(eventsForMonth)
    }
    val hasActiveFilters = remember(activeFilters, timeRange, locationFilter) {
        viewModel.hasActiveFilters
    }

    // Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val grayColor = if (backgroundColor == BackgroundColor) GrayColorDark else GrayColorLight

    // Pager state
    val sharedScrollState = rememberScrollState()
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val referenceDate = remember { LocalDate.now() }
    val pagerState = rememberPagerState(
        initialPage = CalendarConfig.Pager.calculateWeekPage(referenceDate, selectedDate),
        pageCount = { CalendarConfig.Pager.PAGE_COUNT }
    )
    val dayPagerState = rememberPagerState(
        initialPage = CalendarConfig.Pager.calculateDayPage(referenceDate, selectedDate),
        pageCount = { CalendarConfig.Pager.PAGE_COUNT }
    )

    // Pager sync
    LaunchedEffect(selectedDate) {
        viewModel.setDisplayedWeekStart(selectedDate.with(DayOfWeek.MONDAY))
        val targetDay = CalendarConfig.Pager.calculateDayPage(referenceDate, selectedDate)
        if (dayPagerState.currentPage != targetDay) dayPagerState.scrollToPage(targetDay)
        val targetWeek = CalendarConfig.Pager.calculateWeekPage(referenceDate, selectedDate)
        if (pagerState.currentPage != targetWeek) pagerState.scrollToPage(targetWeek)
    }
    LaunchedEffect(dayPagerState.currentPage) {
        if (viewMode == CalendarViewMode.LIST) {
            val offset = dayPagerState.currentPage - CalendarConfig.Pager.INITIAL_PAGE_OFFSET
            val newDate = referenceDate.plusDays(offset.toLong())
            if (newDate != selectedDate) viewModel.selectDate(newDate)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        val offset = pagerState.currentPage - CalendarConfig.Pager.INITIAL_PAGE_OFFSET
        val weekStart = referenceDate.with(DayOfWeek.MONDAY).plusWeeks(offset.toLong())
        viewModel.setDisplayedWeekStart(weekStart)
        if (viewMode == CalendarViewMode.WEEK) {
            val inWeek = selectedDate >= weekStart && selectedDate < weekStart.plusWeeks(1)
            if (!inWeek) viewModel.selectDate(weekStart)
        }
    }

    Scaffold(
        topBar = {
            CalendarTopBar(
                currentMonth = currentMonth,
                viewMode = viewMode,
                onViewModeChange = viewModel::setViewMode,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                onToday = viewModel::goToToday,
                onMonthClick = { viewModel.setShowDatePicker(true) },
                primaryColor = primaryColor
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor,
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            state = pullToRefreshState,
            indicator = {},
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                NetworkStatusBar(isOnline = isOnline, errorMessage = error, onDismissError = viewModel::clearError)

                AnimatedVisibility(
                    visible = viewMode != CalendarViewMode.MONTH,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    HorizontalDaySelector(
                        pagerState = pagerState,
                        selectedDate = selectedDate,
                        referenceDate = referenceDate,
                        viewMode = viewMode,
                        onDateSelected = { date ->
                            viewModel.selectDate(date)
                            if (viewMode == CalendarViewMode.WEEK) viewModel.setViewMode(CalendarViewMode.LIST)
                        },
                        textColor = textColor,
                        grayColor = grayColor,
                        primaryColor = primaryColor
                    )
                }

                HorizontalDivider(color = grayColor.copy(alpha = 0.2f))

                CalendarViewContent(
                    viewMode = viewMode,
                    eventsForMonth = eventsForMonth,
                    filteredEvents = filteredEvents,
                    selectedDate = selectedDate,
                    currentMonth = currentMonth,
                    displayedWeekStart = displayedWeekStart,
                    activeFilters = activeFilters,
                    timeRange = timeRange,
                    locationFilter = locationFilter,
                    isLoading = isRefreshing,
                    hasActiveFilters = hasActiveFilters,
                    dayPagerState = dayPagerState,
                    referenceDate = referenceDate,
                    onEventSelected = viewModel::selectEvent,
                    onDateSelected = viewModel::selectDate,
                    onMonthChange = viewModel::setCurrentMonth,
                    onViewModeChange = viewModel::setViewMode,
                    onSwipeWeek = { direction ->
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - direction) }
                    },
                    scrollState = sharedScrollState,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor
                )
            }
        }
    }

    // Dialogs / Modals

    if (showFilters) {
        CalendarFilterModal(
            onDismiss = viewModel::toggleFiltersVisibility,
            activeFilters = activeFilters,
            onFilterToggle = viewModel::toggleFilter,
            timeRange = timeRange,
            onTimeRangeChange = viewModel::setTimeRange,
            locationFilter = locationFilter,
            onLocationFilterChange = viewModel::setLocationFilter,
            buildings = buildingsWithRooms,
            onClearAll = viewModel::clearFilters,
            onJumpToNextExam = viewModel::jumpToNextExam,
            onJumpToNextLesson = viewModel::jumpToNextLesson
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { viewModel.setShowDatePicker(false) },
            onDateSelected = { date ->
                viewModel.jumpToDate(date)
                viewModel.setShowDatePicker(false)
            },
            initialDate = selectedDate,
            primaryColor = primaryColor
        )
    }

    selectedEvent?.let { event ->
        EventDetailDialog(
            event = event,
            onDismiss = { viewModel.selectEvent(null) },
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            backgroundColor = backgroundColor
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalendarViewContent(
    viewMode: CalendarViewMode,
    eventsForMonth: List<CalendarEvent>,
    filteredEvents: List<CalendarEvent>,
    selectedDate: LocalDate,
    currentMonth: java.time.YearMonth,
    displayedWeekStart: LocalDate,
    activeFilters: Set<it.attendance100.mybicocca.data.model.calendar.EventType>,
    timeRange: TimeRange?,
    locationFilter: LocationFilter,
    isLoading: Boolean,
    hasActiveFilters: Boolean,
    dayPagerState: PagerState,
    referenceDate: LocalDate,
    onEventSelected: (CalendarEvent) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (java.time.YearMonth) -> Unit,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onSwipeWeek: (Int) -> Unit,
    scrollState: ScrollState,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    AnimatedContent(
        targetState = viewMode,
        transitionSpec = {
            when {
                initialState == CalendarViewMode.LIST && targetState == CalendarViewMode.WEEK ->
                    (slideInHorizontally { it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { -it / 3 } + fadeOut())
                initialState == CalendarViewMode.WEEK && targetState == CalendarViewMode.LIST ->
                    (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(slideOutHorizontally { it / 3 } + fadeOut())
                targetState == CalendarViewMode.MONTH ->
                    (slideInVertically { it / 4 } + fadeIn()).togetherWith(slideOutVertically { -it / 4 } + fadeOut())
                else ->
                    (slideInVertically { -it / 4 } + fadeIn()).togetherWith(slideOutVertically { it / 4 } + fadeOut())
            }.using(SizeTransform(clip = false))
        },
        label = "ViewModeTransition"
    ) { mode ->
        when (mode) {
            CalendarViewMode.LIST -> DayTimelineView(
                dayPagerState = dayPagerState,
                referenceDate = referenceDate,
                eventsForCurrentMonth = eventsForMonth,
                searchQuery = "",
                activeFilters = activeFilters,
                timeRange = timeRange,
                locationFilter = locationFilter,
                isLoading = isLoading,
                hasActiveFilters = hasActiveFilters,
                onEventClick = onEventSelected,
                scrollState = scrollState
            )
            CalendarViewMode.WEEK -> WeekGridView(
                displayedWeekStart = displayedWeekStart,
                events = filteredEvents,
                isLoading = isLoading,
                hasActiveFilters = hasActiveFilters,
                searchQuery = "",
                onEventClick = onEventSelected,
                scrollState = scrollState,
                onSwipeWeek = onSwipeWeek,
                onDayZoom = { date ->
                    onDateSelected(date)
                    onViewModeChange(CalendarViewMode.LIST)
                },
                grayColor = grayColor,
                primaryColor = primaryColor
            )
            CalendarViewMode.MONTH -> MonthGridView(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                events = filteredEvents,
                isLoading = isLoading,
                onDateSelected = onDateSelected,
                onMonthChange = onMonthChange,
                onEventSelected = onEventSelected,
                onShowAllEvents = { onViewModeChange(CalendarViewMode.LIST) },
                textColor = textColor,
                grayColor = grayColor,
                primaryColor = primaryColor
            )
        }
    }
}
