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
import androidx.hilt.navigation.compose.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.EventDetailDialog
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import it.attendance100.mybicocca.viewmodel.*
import java.time.*
import java.time.format.*
import java.time.temporal.*
import java.util.*


// VIEW MODE ENUM

enum class CalendarViewMode {
    LIST,
    WEEK
}


// SHARED CONSTANTS
/**
 * Costanti condivise tra CalendarScreen e WeekGridView per garantire allineamento.
 * Questa costante crea un accoppiamento tra CalendarScreen (che usa uno Spacer
 * nel selettore dei giorni) e WeekGridView (che usa questa larghezza per la colonna oraria).
 * Modificare questo valore richiede modifiche in entrambi i componenti per mantenere l'allineamento.
 */
object CalendarLayoutConstants {
    val TIME_COLUMN_WIDTH = 50.dp
}


// MAIN CALENDAR SCREEN

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val grayColor = if (backgroundColor == BackgroundColor) GrayColor else GrayColorLight

    val selectedDate by viewModel.selectedDate.observeAsState(LocalDate.now())
    val currentMonth by viewModel.currentMonth.observeAsState(YearMonth.now())
    val eventsForSelectedDate by viewModel.eventsForSelectedDate.observeAsState(emptyList())
    val eventsForCurrentMonth by viewModel.eventsForCurrentMonth.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)

    var viewMode by remember { mutableStateOf(CalendarViewMode.LIST) }
    var displayedWeekStart by remember { mutableStateOf(selectedDate.with(DayOfWeek.MONDAY)) }
    var todayPressCount by remember { mutableIntStateOf(0) }
    var selectedEvent by remember { mutableStateOf<CourseEvent?>(null) }

    LaunchedEffect(selectedDate) {
        displayedWeekStart = selectedDate.with(DayOfWeek.MONDAY)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header
        CalendarHeader(
            currentMonth = currentMonth,
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onToday = {
                viewModel.goToToday()
                todayPressCount++
            },
            textColor = textColor,
            primaryColor = primaryColor
        )

        // Selettore giorni - SEMPRE VISIBILE, IDENTICO per entrambe le viste
        HorizontalDaySelector(
            selectedDate = selectedDate,
            viewMode = viewMode,
            todayPressCount = todayPressCount,
            onDateSelected = { date -> viewModel.selectDate(date) },
            onWeekChanged = { weekStartDate ->
                val weekMonth = YearMonth.from(weekStartDate)
                if (currentMonth != weekMonth) {
                    viewModel.setCurrentMonth(weekMonth)
                }
                displayedWeekStart = weekStartDate
            },
            onTodayScrollHandled = { todayPressCount = 0 },
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor
        )

        HorizontalDivider(color = grayColor.copy(alpha = 0.2f))

        // Contenuto con animazione fluida tra le viste
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                // Animazione di fade + slide orizzontale
                if (targetState == CalendarViewMode.WEEK) {
                    // Da lista a griglia: slide da destra + fade
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth / 3 },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(400, easing = LinearEasing)
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(
                            animationSpec = tween(400, easing = LinearEasing)
                        )
                    )
                } else {
                    // Da griglia a lista: slide da sinistra + fade
                    (slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth / 3 },
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(400, easing = LinearEasing)
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(
                            animationSpec = tween(400, easing = LinearEasing)
                        )
                    )
                }
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
                        onEventClick = { event -> selectedEvent = event }
                    )
                }
                CalendarViewMode.WEEK -> {
                    WeekGridView(
                        displayedWeekStart = displayedWeekStart,
                        events = eventsForCurrentMonth,
                        isLoading = isLoading,
                        grayColor = grayColor,
                        primaryColor = primaryColor,
                        onEventClick = { event -> selectedEvent = event }
                    )
                }
            }
        }
    }

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


// CALENDAR HEADER

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
        // Pulsante Oggi
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Navigazione mese
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.calendar_previous_month),
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = currentMonth.format(monthYearFormatter).replaceFirstChar { it.uppercase() },
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 2.dp)
            )

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.calendar_next_month),
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Toggle vista
        FilledTonalIconButton(
            onClick = {
                onViewModeChange(
                    if (viewMode == CalendarViewMode.LIST) CalendarViewMode.WEEK
                    else CalendarViewMode.LIST
                )
            },
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (viewMode == CalendarViewMode.LIST) primaryColor else Color.White,
                contentColor = if (viewMode == CalendarViewMode.LIST) Color.White else primaryColor
            )
        ) {
            Icon(
                imageVector = if (viewMode == CalendarViewMode.LIST) {
                    Icons.Outlined.ViewAgenda
                } else {
                    Icons.Outlined.CalendarViewWeek
                },
                contentDescription = if (viewMode == CalendarViewMode.LIST) {
                    stringResource(R.string.calendar_list_view)
                } else {
                    stringResource(R.string.calendar_week_view)
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}


// HORIZONTAL DAY SELECTOR (Allineato alla griglia)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalDaySelector(
    selectedDate: LocalDate,
    viewMode: CalendarViewMode,
    todayPressCount: Int,
    onDateSelected: (LocalDate) -> Unit,
    onWeekChanged: (LocalDate) -> Unit,
    onTodayScrollHandled: () -> Unit,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color
) {
    val referenceDate = remember { LocalDate.of(2020, 1, 1) }

    val initialPage = remember(selectedDate) {
        val weeksBetween = ChronoUnit.WEEKS.between(
            referenceDate.with(DayOfWeek.MONDAY),
            selectedDate.with(DayOfWeek.MONDAY)
        )
        1000 + weeksBetween.toInt()
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 2000 }
    )

    LaunchedEffect(todayPressCount) {
        if (todayPressCount > 0) {
            val today = LocalDate.now()
            val weeksBetween = ChronoUnit.WEEKS.between(
                referenceDate.with(DayOfWeek.MONDAY),
                today.with(DayOfWeek.MONDAY)
            )
            val targetPage = 1000 + weeksBetween.toInt()
            pagerState.animateScrollToPage(targetPage)
            onTodayScrollHandled()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val weekOffset = pagerState.currentPage - 1000
        val weekStart = referenceDate.with(DayOfWeek.MONDAY).plusWeeks(weekOffset.toLong())
        onWeekChanged(weekStart)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) { page ->
        val weekOffset = page - 1000
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

/**
 * Row dei giorni della settimana.
 * In modalità WEEK ha uno Spacer iniziale per allinearsi alla colonna orario della griglia.
 */
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
        // Spacer per allinearsi alla colonna orario (solo in WEEK mode)
        if (viewMode == CalendarViewMode.WEEK) {
            Spacer(modifier = Modifier.width(CalendarLayoutConstants.TIME_COLUMN_WIDTH))
        }

        // 7 giorni con peso uguale
        (0..6).forEach { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val isSelected = date == selectedDate
            val isToday = CalendarUtils.isToday(date)
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
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
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) primaryColor
                else Color.Transparent
            )
            .clickable { onDateSelected(date) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName.take(3).uppercase(),
            color = when {
                isSelected -> Color.White.copy(alpha = 0.8f)
                isToday -> primaryColor
                else -> grayColor
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = date.dayOfMonth.toString(),
            color = when {
                isSelected -> Color.White
                isToday -> primaryColor
                else -> textColor
            },
            fontSize = 17.sp,
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
