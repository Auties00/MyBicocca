package it.attendance100.mybicocca.ui.component.calendar.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.ui.component.calendar.CalendarLoadingState
import it.attendance100.mybicocca.ui.component.calendar.EventCard
import it.attendance100.mybicocca.ui.component.calendar.EventCardVariant
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import it.attendance100.mybicocca.util.*
import java.time.*
import java.time.format.*
import java.util.*
import kotlin.math.absoluteValue

// MAIN COMPONENT - MONTH GRID VIEW

@Composable
fun MonthGridView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    isLoading: Boolean,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onEventSelected: (CalendarEvent) -> Unit,
    onShowAllEvents: () -> Unit
) {
    var isSwiping by remember { mutableStateOf(false) }

    // Raggruppa eventi per data per un accesso efficiente
    val eventsByDate = remember(events) {
        events.groupBy { it.date }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { isSwiping = false },
                        onDragCancel = { isSwiping = false }
                    ) { _, dragAmount ->
                        if (!isSwiping && dragAmount.absoluteValue > 50) {
                            val direction = if (dragAmount > 0) -1L else 1L
                            onMonthChange(currentMonth.plusMonths(direction))
                            isSwiping = true
                        }
                    }
                }
        ) {
            when {
                isLoading -> CalendarLoadingState()
                else -> MonthGridContent(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    eventsByDate = eventsByDate,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor,
                    onDateSelected = onDateSelected,
                    onMonthChange = onMonthChange
                )
            }
        }

        SelectedDateEventsPreview(
            selectedDate = selectedDate,
            events = events,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            onEventClick = onEventSelected,
            onShowAllEvents = onShowAllEvents
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// MONTH GRID CONTENT

@Composable
private fun MonthGridContent(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    
    // Calcola l'offset per iniziare dal giorno corretto (Lunedì = 0)
    val startDayOffset = (firstDayOfMonth.dayOfWeek.value - 1)
    
    // Giorni del mese precedente da mostrare
    val previousMonth = currentMonth.minusMonths(1)
    val daysInPreviousMonth = previousMonth.lengthOfMonth()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        // Header giorni della settimana
        WeekDaysHeader(grayColor = grayColor)

        Spacer(modifier = Modifier.height(8.dp))

        // Griglia del mese
        MonthDaysGrid(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            daysInMonth = daysInMonth,
            startDayOffset = startDayOffset,
            previousMonth = previousMonth,
            daysInPreviousMonth = daysInPreviousMonth,
            eventsByDate = eventsByDate,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            onDateSelected = onDateSelected,
            onMonthChange = onMonthChange
        )
    }
}

@Composable
private fun WeekDaysHeader(grayColor: Color) {
    val locale = Locale.getDefault()
    val daysOfWeek = remember(locale) {
        // Inizia da Lunedì
        DayOfWeek.values().map { day ->
            day.getDisplayName(TextStyle.SHORT, locale).take(3).uppercase()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        daysOfWeek.forEach { dayName ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayName,
                    color = grayColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MonthDaysGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    daysInMonth: Int,
    startDayOffset: Int,
    previousMonth: YearMonth,
    daysInPreviousMonth: Int,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val totalCells = startDayOffset + daysInMonth
    val rows = (totalCells + 6) / 7 // Arrotonda per eccesso

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayInfo = calculateDayInfo(
                        cellIndex = cellIndex,
                        startDayOffset = startDayOffset,
                        daysInMonth = daysInMonth,
                        currentMonth = currentMonth,
                        previousMonth = previousMonth,
                        daysInPreviousMonth = daysInPreviousMonth
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        MonthDayCell(
                            dayInfo = dayInfo,
                            selectedDate = selectedDate,
                            eventsByDate = eventsByDate,
                            textColor = textColor,
                            grayColor = grayColor,
                            primaryColor = primaryColor,
                            onDateSelected = onDateSelected,
                            onMonthChange = onMonthChange
                        )
                    }
                }
            }
        }
    }
}

// DAY CELL - Cella singola del giorno

private data class DayInfo(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

private fun calculateDayInfo(
    cellIndex: Int,
    startDayOffset: Int,
    daysInMonth: Int,
    currentMonth: YearMonth,
    previousMonth: YearMonth,
    daysInPreviousMonth: Int
): DayInfo {
    val dayNumber = cellIndex - startDayOffset + 1
    val today = LocalDate.now()

    return when {
        // Giorno del mese precedente
        dayNumber <= 0 -> {
            val prevDay = daysInPreviousMonth + dayNumber
            val date = previousMonth.atDay(prevDay)
            DayInfo(
                date = date,
                isCurrentMonth = false,
                isToday = date == today
            )
        }
        // Giorno del mese corrente
        dayNumber <= daysInMonth -> {
            val date = currentMonth.atDay(dayNumber)
            DayInfo(
                date = date,
                isCurrentMonth = true,
                isToday = date == today
            )
        }
        // Giorno del mese successivo
        else -> {
            val nextDay = dayNumber - daysInMonth
            val nextMonth = currentMonth.plusMonths(1)
            val date = nextMonth.atDay(nextDay)
            DayInfo(
                date = date,
                isCurrentMonth = false,
                isToday = date == today
            )
        }
    }
}

@Composable
private fun MonthDayCell(
    dayInfo: DayInfo,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    val isSelected = dayInfo.date == selectedDate
    val dayEvents = eventsByDate[dayInfo.date] ?: emptyList()
    val hasEvents = dayEvents.isNotEmpty()

    // Animazione per la selezione
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .scale(animatedScale)
            .clip(MaterialTheme.shapes.medium)
            .background(
                when {
                    isSelected -> primaryColor
                    dayInfo.isToday -> primaryColor.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .clickable {
                onDateSelected(dayInfo.date)
                if (!dayInfo.isCurrentMonth) {
                    onMonthChange(YearMonth.from(dayInfo.date))
                }
            }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Numero del giorno
        Text(
            text = dayInfo.date.dayOfMonth.toString(),
            color = when {
                isSelected -> Color.White
                dayInfo.isToday -> primaryColor  // Oggi ha priorità anche su altri mesi
                !dayInfo.isCurrentMonth -> grayColor.copy(alpha = 0.4f)
                else -> textColor
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = when {
                isSelected || dayInfo.isToday -> FontWeight.Bold
                else -> FontWeight.Normal
            }
        )

        // Spacer sempre presente per mantenere allineamento verticale consistente
        Spacer(modifier = Modifier.height(2.dp))

        // Indicatori eventi - Box con altezza fissa per evitare shift del giorno
        Box(modifier = Modifier.height(14.dp)) {
            if (hasEvents) {
                EventIndicators(
                    events = dayEvents,
                    isSelected = isSelected,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
private fun EventIndicators(
    events: List<CalendarEvent>,
    isSelected: Boolean,
    primaryColor: Color
) {
    // Mostra fino a 3 indicatori colorati per tipo di evento
    val eventTypes = events.map { it.eventType }.distinct().take(3)

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        eventTypes.forEach { eventType ->
            val dotColor = if (isSelected) {
                Color.White.copy(alpha = 0.9f)
            } else {
                CalendarUtils.getEventColor(eventType, primaryColor)
            }

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }

        // Se ci sono più di 3 eventi, mostra un badge numerico
        if (events.size > 3) {
            Badge(
                containerColor = if (isSelected) Color.White.copy(alpha = 0.8f) else primaryColor.copy(alpha = 0.15f),
                contentColor = if (isSelected) primaryColor else primaryColor
            ) {
                Text(
                    text = "+${events.size - 3}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// Loading state ora usa CalendarLoadingState da components/shared/LoadingStates.kt

// SELECTED DATE EVENTS PREVIEW - Anteprima eventi del giorno selezionato

@Composable
fun SelectedDateEventsPreview(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CalendarEvent) -> Unit,
    onShowAllEvents: () -> Unit
) {
    val dayEvents = remember(events, selectedDate) {
        events.filter { it.date == selectedDate }
            .sortedBy { it.startDateTime }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = CalendarUtils.formatSelectedDate(selectedDate),
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (dayEvents.isEmpty()) {
                        Text(
                            text = stringResource(R.string.calendar_no_events_short),
                            color = grayColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                if (dayEvents.isNotEmpty()) {
                    Badge(
                        containerColor = primaryColor.copy(alpha = 0.1f),
                        contentColor = primaryColor
                    ) {
                        Text(
                            text = stringResource(R.string.calendar_events_count, dayEvents.size),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (dayEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                // Primi 3 eventi
                dayEvents.take(3).forEach { event ->
                    EventCard(
                        event = event,
                        variant = EventCardVariant.COMPACT,
                        onClick = { onEventClick(event) }
                    )
                    if (event != dayEvents.take(3).last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Link per vedere tutti
                if (dayEvents.size > 3) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onShowAllEvents,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text(stringResource(R.string.calendar_see_all, dayEvents.size))
                    }
                }
            }
        }
    }
}

