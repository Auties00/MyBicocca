package it.attendance100.mybicocca.screens.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.EventDetailDialog
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.*
import java.time.temporal.*
import java.util.*

// ============================================================================
// VIEW MODE ENUM - Aggiunta modalità MONTH
// ============================================================================

enum class CalendarViewMode {
    LIST,   // Vista giornaliera timeline
    WEEK,   // Vista settimanale griglia
    MONTH   // Vista mensile completa
}

// ============================================================================
// CALENDAR ROUTE - Entry point con ViewModel
// ============================================================================

@Composable
fun CalendarRoute(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.observeAsState(LocalDate.now())
    val currentMonth by viewModel.currentMonth.observeAsState(YearMonth.now())
    val eventsForSelectedDate by viewModel.eventsForSelectedDate.observeAsState(emptyList())
    val eventsForCurrentMonth by viewModel.eventsForCurrentMonth.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    CalendarScreen(
        selectedDate = selectedDate,
        currentMonth = currentMonth,
        eventsForSelectedDate = eventsForSelectedDate,
        eventsForCurrentMonth = eventsForCurrentMonth,
        isLoading = isLoading,
        onDateSelected = { viewModel.selectDate(it) },
        onMonthChange = { viewModel.setCurrentMonth(it) },
        onPreviousMonth = { viewModel.previousMonth() },
        onNextMonth = { viewModel.nextMonth() },
        onTodayClick = { viewModel.goToToday() }
    )
}

// ============================================================================
// MAIN CALENDAR SCREEN
// ============================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    eventsForSelectedDate: List<CourseEvent>,
    eventsForCurrentMonth: List<CourseEvent>,
    isLoading: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val grayColor = if (backgroundColor == BackgroundColor) GrayColor else GrayColorLight

    var viewMode by remember { mutableStateOf(CalendarViewMode.LIST) }
    var displayedWeekStart by remember { mutableStateOf(selectedDate.with(DayOfWeek.MONDAY)) }
    var todayPressCount by remember { mutableIntStateOf(0) }
    var selectedEvent by remember { mutableStateOf<CourseEvent?>(null) }

    // [SYNC] Scroll State condiviso per mantenere l'allineamento verticale tra le viste
    val sharedScrollState = rememberScrollState()

    // Scope per animazioni
    val scope = rememberCoroutineScope()

    // Setup Pager
    val referenceDate = remember { LocalDate.now() }
    val initialPage = remember(selectedDate) {
        val weeksBetween = ChronoUnit.WEEKS.between(
            referenceDate.with(DayOfWeek.MONDAY),
            selectedDate.with(DayOfWeek.MONDAY)
        )
        CalendarUtils.PAGER_INITIAL_PAGE_OFFSET + weeksBetween.toInt()
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { CalendarUtils.PAGER_PAGE_COUNT }
    )

    LaunchedEffect(selectedDate) {
        displayedWeekStart = selectedDate.with(DayOfWeek.MONDAY)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header con navigazione e toggle vista
        CalendarHeader(
            currentMonth = currentMonth,
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onToday = {
                onTodayClick()
                todayPressCount++
                // Se in vista settimanale o mensile, passa alla giornaliera
                if (viewMode != CalendarViewMode.LIST) {
                    viewMode = CalendarViewMode.LIST
                }
            },
            textColor = textColor,
            primaryColor = primaryColor
        )

        // Gestione click "Oggi"
        LaunchedEffect(todayPressCount) {
            if (todayPressCount > 0) {
                val today = LocalDate.now()
                val weeksBetween = ChronoUnit.WEEKS.between(
                    referenceDate.with(DayOfWeek.MONDAY),
                    today.with(DayOfWeek.MONDAY)
                )
                val targetPage = CalendarUtils.PAGER_INITIAL_PAGE_OFFSET + weeksBetween.toInt()
                pagerState.animateScrollToPage(targetPage)
            }
        }

        // Sincronizzazione Pager -> Mese/Settimana corrente
        LaunchedEffect(pagerState.currentPage) {
            val weekOffset = pagerState.currentPage - CalendarUtils.PAGER_INITIAL_PAGE_OFFSET
            val weekStart = referenceDate.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())

            val weekMonth = YearMonth.from(weekStart.plusDays(3))
            if (currentMonth != weekMonth) {
                onMonthChange(weekMonth)
            }
            displayedWeekStart = weekStart
        }

        // Selettore giorni orizzontale (nascosto in vista mensile)
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
                    onDateSelected(date)
                    // Se in vista settimanale, passa alla giornaliera
                    if (viewMode == CalendarViewMode.WEEK) {
                        viewMode = CalendarViewMode.LIST
                    }
                },
                textColor = textColor,
                grayColor = grayColor,
                primaryColor = primaryColor
            )
        }

        HorizontalDivider(color = grayColor.copy(alpha = 0.2f))

        // Contenuto principale con transizioni animate
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                when {
                    // Da LIST a WEEK
                    initialState == CalendarViewMode.LIST && targetState == CalendarViewMode.WEEK -> {
                        (slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn()).togetherWith(
                            slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut()
                        )
                    }
                    // Da WEEK a LIST
                    initialState == CalendarViewMode.WEEK && targetState == CalendarViewMode.LIST -> {
                        (slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()).togetherWith(
                            slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut()
                        )
                    }
                    // Da/verso MONTH
                    targetState == CalendarViewMode.MONTH -> {
                        (slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()).togetherWith(
                            slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut()
                        )
                    }
                    else -> {
                        (slideInVertically(initialOffsetY = { -it / 4 }) + fadeIn()).togetherWith(
                            slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
                        )
                    }
                }.using(SizeTransform(clip = false))
            },
            label = "ViewModeTransition"
        ) { mode ->
            when (mode) {
                CalendarViewMode.LIST -> {
                    DayTimelineView(
                        events = eventsForSelectedDate,
                        selectedDate = selectedDate,
                        isLoading = isLoading,
                        textColor = textColor,
                        grayColor = grayColor,
                        primaryColor = primaryColor,
                        onEventClick = { event -> selectedEvent = event },
                        scrollState = sharedScrollState
                    )
                }

                CalendarViewMode.WEEK -> {
                    WeekGridView(
                        displayedWeekStart = displayedWeekStart,
                        events = eventsForCurrentMonth,
                        isLoading = isLoading,
                        grayColor = grayColor,
                        primaryColor = primaryColor,
                        onEventClick = { event -> selectedEvent = event },
                        scrollState = sharedScrollState,
                        onSwipeWeek = { direction ->
                            scope.launch {
                                val newPage = pagerState.currentPage - direction
                                pagerState.animateScrollToPage(newPage)
                            }
                        },
                        onDayZoom = { date ->
                            onDateSelected(date)
                            viewMode = CalendarViewMode.LIST
                        }
                    )
                }

                CalendarViewMode.MONTH -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        MonthGridView(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            events = eventsForCurrentMonth,
                            isLoading = isLoading,
                            textColor = textColor,
                            grayColor = grayColor,
                            primaryColor = primaryColor,
                            onDateSelected = { date ->
                                onDateSelected(date)
                                // Aggiorna anche il pager per mantenere sincronizzazione
                                scope.launch {
                                    val weeksBetween = ChronoUnit.WEEKS.between(
                                        referenceDate.with(DayOfWeek.MONDAY),
                                        date.with(DayOfWeek.MONDAY)
                                    )
                                    val targetPage = CalendarUtils.PAGER_INITIAL_PAGE_OFFSET + weeksBetween.toInt()
                                    pagerState.scrollToPage(targetPage)
                                }
                            },
                            onMonthChange = onMonthChange
                        )

                        // Anteprima eventi del giorno selezionato
                        SelectedDateEventsPreview(
                            selectedDate = selectedDate,
                            events = eventsForCurrentMonth,
                            textColor = textColor,
                            grayColor = grayColor,
                            primaryColor = primaryColor,
                            onEventClick = { event -> selectedEvent = event },
                            onShowAllEvents = {
                                viewMode = CalendarViewMode.LIST
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Dialog dettagli evento
    selectedEvent?.let { event ->
        EventDetailDialog(
            event = event,
            onDismiss = { selectedEvent = null },
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            backgroundColor = backgroundColor
        )
    }
}

// ============================================================================
// CALENDAR HEADER - Con toggle vista a 3 modalità
// ============================================================================

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    textColor: Color,
    primaryColor: Color
) {
    val locale = Locale.getDefault()
    val monthYearFormatter = CalendarUtils.monthYearFormatter(locale)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pulsante "Oggi"
        FilledTonalButton(
            onClick = onToday,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = primaryColor.copy(alpha = 0.12f),
                contentColor = primaryColor
            )
        ) {
            Text(
                text = stringResource(R.string.calendar_today),
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Navigazione mese
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Default.ChevronLeft,
                    stringResource(R.string.calendar_previous_month),
                    tint = textColor
                )
            }
            Text(
                text = currentMonth.format(monthYearFormatter).replaceFirstChar { it.uppercase() },
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Default.ChevronRight,
                    stringResource(R.string.calendar_next_month),
                    tint = textColor
                )
            }
        }

        // Toggle vista con menu a 3 opzioni
        ViewModeToggle(
            currentMode = viewMode,
            onModeChange = onViewModeChange,
            primaryColor = primaryColor
        )
    }
}

// ============================================================================
// VIEW MODE TOGGLE - Cycling button con animazione rotazione
// ============================================================================

@Composable
private fun ViewModeToggle(
    currentMode: CalendarViewMode,
    onModeChange: (CalendarViewMode) -> Unit,
    primaryColor: Color
) {
    // Stato per tracciare l'animazione
    var isAnimating by remember { mutableStateOf(false) }
    
    // Animazione rotazione completa (0 -> 180 -> 0)
    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        finishedListener = { isAnimating = false },
        label = "viewModeRotation"
    )

    FilledTonalIconButton(
        onClick = {
            isAnimating = true
            // Cicla alla prossima vista
            val nextMode = when (currentMode) {
                CalendarViewMode.LIST -> CalendarViewMode.WEEK
                CalendarViewMode.WEEK -> CalendarViewMode.MONTH
                CalendarViewMode.MONTH -> CalendarViewMode.LIST
            }
            onModeChange(nextMode)
        },
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = primaryColor,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = when (currentMode) {
                CalendarViewMode.LIST -> Icons.Outlined.ViewAgenda
                CalendarViewMode.WEEK -> Icons.Outlined.CalendarViewWeek
                CalendarViewMode.MONTH -> Icons.Outlined.CalendarMonth
            },
            contentDescription = stringResource(R.string.calendar_view_mode),
            modifier = Modifier.graphicsLayer {
                rotationY = rotation
            }
        )
    }
}

// ============================================================================
// HORIZONTAL DAY SELECTOR
// ============================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalDaySelector(
    pagerState: PagerState,
    selectedDate: LocalDate,
    referenceDate: LocalDate,
    viewMode: CalendarViewMode,
    onDateSelected: (LocalDate) -> Unit,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(CalendarUtils.DAY_SELECTOR_HEIGHT)
    ) { page ->
        val weekOffset = page - CalendarUtils.PAGER_INITIAL_PAGE_OFFSET
        val weekStart = referenceDate.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())

        WeekDaysRow(
            weekStart = weekStart,
            selectedDate = selectedDate,
            viewMode = viewMode,
            onDateSelected = onDateSelected,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor
        )
    }
}

@Composable
private fun WeekDaysRow(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    viewMode: CalendarViewMode,
    onDateSelected: (LocalDate) -> Unit,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (viewMode == CalendarViewMode.WEEK) {
            Spacer(modifier = Modifier.width(CalendarUtils.TIME_COLUMN_WIDTH))
        }

        (0..6).forEach { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val isSelected = date == selectedDate
            val isToday = CalendarUtils.isToday(date)
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                DaySelectorItem(
                    date = date,
                    dayName = dayName,
                    isSelected = isSelected,
                    isToday = isToday,
                    onDateSelected = onDateSelected,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun DaySelectorItem(
    date: LocalDate,
    dayName: String,
    isSelected: Boolean,
    isToday: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 7.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) primaryColor else Color.Transparent)
            .clickable { onDateSelected(date) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName.take(3).uppercase(),
            color = when {
                isSelected -> Color.White.copy(alpha = 0.8f)
                isToday -> primaryColor
                else -> grayColor
            },
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = date.dayOfMonth.toString(),
            color = when {
                isSelected -> Color.White
                isToday -> primaryColor
                else -> textColor
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
        if (isToday && !isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
        }
    }
}
