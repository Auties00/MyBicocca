package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.CalendarEvent
import it.attendance100.mybicocca.ui.theme.EventExamColor
import it.attendance100.mybicocca.ui.screen.main.calendar.CalendarUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
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
    onMonthChange: (YearMonth) -> Unit
) {
    var isSwiping by remember { mutableStateOf(false) }

    // Raggruppa eventi per data per un accesso efficiente
    val eventsByDate = remember(events) {
        events.groupBy { it.startTime.toLocalDate() }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
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
            isLoading -> MonthGridLoadingState(primaryColor)
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
            .clip(RoundedCornerShape(12.dp))
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
        if (hasEvents) {
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
    events: List<CalendarEvent>,
    isSelected: Boolean,
    primaryColor: Color
) {
    // Mostra fino a 3 indicatori colorati per tipo di evento
    val eventTypes = events.map { it.type }.distinct().take(3)

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

// LOADING STATE

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
        events.filter { it.startTime.toLocalDate() == selectedDate }
            .sortedBy { it.startTime }
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
                        text = formatSelectedDate(selectedDate),
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (dayEvents.isEmpty()) {
                        Text(
                            text = "Nessun evento",
                            color = grayColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (dayEvents.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${dayEvents.size} eventi",
                            color = primaryColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (dayEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

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
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onShowAllEvents,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text("Vedi tutti (${dayEvents.size})")
                    }
                }
            }
        }
    }
}

@Composable
private fun EventPreviewItem(
    event: CalendarEvent,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val eventColor = CalendarUtils.getEventColor(event.type, primaryColor)
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
                    text = event.name,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.startTime.format(timeFormatter)} - ${
                        event.endTime.format(
                            timeFormatter
                        )
                    }",
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

// UPCOMING EXAMS SECTION - Sezione esami in arrivo

@Composable
fun UpcomingExamsSection(
    exams: List<CalendarEvent>,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onExamClick: (CalendarEvent) -> Unit
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
    exam: CalendarEvent,
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
                    text = exam.name,
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
