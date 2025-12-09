package it.attendance100.mybicocca.screens.calendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import java.time.*
import java.time.format.*
import java.time.temporal.*
import java.util.*
import kotlin.math.absoluteValue

// ============================================================================
// MAIN COMPONENT - MONTH GRID VIEW
// ============================================================================

@Composable
fun MonthGridView(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<CourseEvent>,
    isLoading: Boolean,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit
) {
    var isSwiping by remember { mutableStateOf(false) }

    // Raggruppa eventi per data per un accesso efficiente
    val eventsByDate = remember(events) {
        events.groupBy { it.startTime.toLocalDate() }
    }

    // Calcola informazioni intelligenti sul mese
    val monthInsights = remember(events, currentMonth) {
        calculateMonthInsights(events, currentMonth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
        // Header con insight mensili
        MonthInsightsHeader(
            monthInsights = monthInsights,
            primaryColor = primaryColor,
            grayColor = grayColor
        )

        when {
            isLoading -> MonthGridLoadingState(primaryColor)
            else -> MonthGridContent(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                eventsByDate = eventsByDate,
                textColor = textColor,
                grayColor = grayColor,
                primaryColor = primaryColor,
                onDateSelected = onDateSelected
            )
        }
    }
}

// ============================================================================
// MONTH INSIGHTS - Funzionalità Intelligenti
// ============================================================================

data class MonthInsights(
    val totalEvents: Int,
    val lectureCount: Int,
    val labCount: Int,
    val examCount: Int,
    val busiestDay: LocalDate?,
    val busiestDayCount: Int,
    val daysWithEvents: Int,
    val upcomingExams: List<CourseEvent>
)

private fun calculateMonthInsights(
    events: List<CourseEvent>,
    currentMonth: YearMonth
): MonthInsights {
    val monthEvents = events.filter {
        YearMonth.from(it.startTime.toLocalDate()) == currentMonth
    }

    val eventsByDate = monthEvents.groupBy { it.startTime.toLocalDate() }
    val busiestEntry = eventsByDate.maxByOrNull { it.value.size }

    val upcomingExams = monthEvents
        .filter { it.eventType == EventType.EXAM && it.startTime.isAfter(LocalDateTime.now()) }
        .sortedBy { it.startTime }
        .take(3)

    return MonthInsights(
        totalEvents = monthEvents.size,
        lectureCount = monthEvents.count { it.eventType == EventType.LECTURE },
        labCount = monthEvents.count { it.eventType == EventType.LAB },
        examCount = monthEvents.count { it.eventType == EventType.EXAM },
        busiestDay = busiestEntry?.key,
        busiestDayCount = busiestEntry?.value?.size ?: 0,
        daysWithEvents = eventsByDate.size,
        upcomingExams = upcomingExams
    )
}

@Composable
private fun MonthInsightsHeader(
    monthInsights: MonthInsights,
    primaryColor: Color,
    grayColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = primaryColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Statistiche rapide
            InsightItem(
                value = monthInsights.totalEvents.toString(),
                label = "Eventi",
                primaryColor = primaryColor
            )
            InsightItem(
                value = monthInsights.daysWithEvents.toString(),
                label = "Giorni attivi",
                primaryColor = primaryColor
            )
            InsightItem(
                value = monthInsights.examCount.toString(),
                label = "Esami",
                primaryColor = EventExamColor
            )
        }
    }
}

@Composable
private fun InsightItem(
    value: String,
    label: String,
    primaryColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = primaryColor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = primaryColor.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// ============================================================================
// MONTH GRID CONTENT
// ============================================================================

@Composable
private fun MonthGridContent(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CourseEvent>>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit
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
            onDateSelected = onDateSelected
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
    eventsByDate: Map<LocalDate, List<CourseEvent>>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit
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
                            onDateSelected = onDateSelected
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// DAY CELL - Cella singola del giorno
// ============================================================================

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

    return when {
        // Giorno del mese precedente
        dayNumber <= 0 -> {
            val prevDay = daysInPreviousMonth + dayNumber
            DayInfo(
                date = previousMonth.atDay(prevDay),
                isCurrentMonth = false,
                isToday = false
            )
        }
        // Giorno del mese corrente
        dayNumber <= daysInMonth -> {
            val date = currentMonth.atDay(dayNumber)
            DayInfo(
                date = date,
                isCurrentMonth = true,
                isToday = date == LocalDate.now()
            )
        }
        // Giorno del mese successivo
        else -> {
            val nextDay = dayNumber - daysInMonth
            val nextMonth = currentMonth.plusMonths(1)
            DayInfo(
                date = nextMonth.atDay(nextDay),
                isCurrentMonth = false,
                isToday = false
            )
        }
    }
}

@Composable
private fun MonthDayCell(
    dayInfo: DayInfo,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CourseEvent>>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onDateSelected: (LocalDate) -> Unit
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
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> primaryColor
                    dayInfo.isToday -> primaryColor.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = dayInfo.isCurrentMonth) {
                onDateSelected(dayInfo.date)
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
                !dayInfo.isCurrentMonth -> grayColor.copy(alpha = 0.4f)
                dayInfo.isToday -> primaryColor
                else -> textColor
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = when {
                isSelected || dayInfo.isToday -> FontWeight.Bold
                else -> FontWeight.Normal
            }
        )

        // Indicatori eventi (solo per il mese corrente)
        if (dayInfo.isCurrentMonth && hasEvents) {
            Spacer(modifier = Modifier.height(2.dp))
            EventIndicators(
                events = dayEvents,
                isSelected = isSelected,
                primaryColor = primaryColor
            )
        }
    }
}

@Composable
private fun EventIndicators(
    events: List<CourseEvent>,
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
            Text(
                text = "+${events.size - 3}",
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else primaryColor,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp
            )
        }
    }
}

// ============================================================================
// LOADING STATE
// ============================================================================

@Composable
private fun MonthGridLoadingState(primaryColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            color = primaryColor
        )
    }
}

// ============================================================================
// SELECTED DATE EVENTS PREVIEW - Anteprima eventi del giorno selezionato
// ============================================================================

@Composable
fun SelectedDateEventsPreview(
    selectedDate: LocalDate,
    events: List<CourseEvent>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit,
    onShowAllEvents: () -> Unit
) {
    val dayEvents = remember(events, selectedDate) {
        events.filter { it.startTime.toLocalDate() == selectedDate }
            .sortedBy { it.startTime }
    }

    if (dayEvents.isEmpty()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header con data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSelectedDate(selectedDate),
                    color = primaryColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = primaryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${dayEvents.size} eventi",
                        color = primaryColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primi 3 eventi
            dayEvents.take(3).forEach { event ->
                EventPreviewItem(
                    event = event,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor,
                    onClick = { onEventClick(event) }
                )
                if (event != dayEvents.take(3).last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Link per vedere tutti
            if (dayEvents.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onShowAllEvents,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Vedi tutti gli eventi (${dayEvents.size})",
                        color = primaryColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventPreviewItem(
    event: CourseEvent,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val timeFormatter = CalendarUtils.timeFormatter

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = eventColor.copy(alpha = 0.1f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra colorata laterale
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Info evento
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.courseName,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.startTime.format(timeFormatter)} - ${event.endTime.format(timeFormatter)}",
                    color = grayColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Aula
            event.room?.let { room ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = grayColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = room,
                        color = grayColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

private fun formatSelectedDate(date: LocalDate): String {
    val locale = Locale.getDefault()
    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase() }
    val day = date.dayOfMonth
    val month = date.month.getDisplayName(TextStyle.FULL, locale)
    return "$dayOfWeek $day $month"
}

// ============================================================================
// UPCOMING EXAMS SECTION - Sezione esami in arrivo
// ============================================================================

@Composable
fun UpcomingExamsSection(
    exams: List<CourseEvent>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onExamClick: (CourseEvent) -> Unit
) {
    if (exams.isEmpty()) return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = EventExamColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = EventExamColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Esami in arrivo",
                    color = EventExamColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            exams.forEach { exam ->
                ExamPreviewItem(
                    exam = exam,
                    textColor = textColor,
                    grayColor = grayColor,
                    onClick = { onExamClick(exam) }
                )
                if (exam != exams.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ExamPreviewItem(
    exam: CourseEvent,
    textColor: Color,
    grayColor: Color,
    onClick: () -> Unit
) {
    val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), exam.startTime.toLocalDate()).toInt()
    val daysText = when {
        daysUntil == 0 -> "Oggi"
        daysUntil == 1 -> "Domani"
        else -> "Tra $daysUntil giorni"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.courseName,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exam.room ?: "Aula da definire",
                    color = grayColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EventExamColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = daysText,
                    color = EventExamColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
