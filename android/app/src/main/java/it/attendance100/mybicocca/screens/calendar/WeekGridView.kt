package it.attendance100.mybicocca.screens.calendar


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.utils.*
import java.time.*


// CONSTANTS

/**
 * Costanti condivise tra CalendarScreen e WeekGridView per garantire allineamento.
 * Questa costante crea un accoppiamento tra CalendarScreen (che usa uno Spacer
 * nel selettore dei giorni) e WeekGridView (che usa questa larghezza per la colonna oraria).
 * Modificare questo valore richiede modifiche in entrambi i componenti per mantenere l'allineamento.
 */
object CalendarLayoutConstants {
    val TIME_COLUMN_WIDTH = 50.dp
}

private object WeekGridConstants {
    const val EVENT_HORIZONTAL_PADDING = 2
    const val TOTAL_DAYS = 7
}


// MAIN COMPONENT - WEEK GRID VIEW

/**
 * Vista griglia settimanale.
 */
@Composable
fun WeekGridView(
    displayedWeekStart: LocalDate,
    events: List<CourseEvent>,
    isLoading: Boolean,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit
) {
    val daysOfWeek = remember(displayedWeekStart) {
        (0..6).map { displayedWeekStart.plusDays(it.toLong()) }
    }

    val weekEvents = remember(events, daysOfWeek) {
        events.filter { event ->
            val eventDate = event.startTime.toLocalDate()
            eventDate >= daysOfWeek.first() && eventDate <= daysOfWeek.last()
        }
    }

    when {
        isLoading -> {
            WeekGridLoadingState(primaryColor = primaryColor)
        }
        else -> {
            WeekGridContent(
                daysOfWeek = daysOfWeek,
                events = weekEvents,
                grayColor = grayColor,
                primaryColor = primaryColor,
                onEventClick = onEventClick
            )
        }
    }
}


// GRID CONTENT

@Composable
private fun WeekGridContent(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Griglia di sfondo con linee orarie
        WeekGridBackground(
            startHour = CalendarUtils.WEEK_START_HOUR,
            endHour = CalendarUtils.WEEK_END_HOUR,
            grayColor = grayColor
        )

        // Eventi posizionati sopra la griglia
        WeekEventsOverlay(
            daysOfWeek = daysOfWeek,
            events = events,
            startHour = CalendarUtils.WEEK_START_HOUR,
            onEventClick = onEventClick,
            primaryColor = primaryColor
        )
    }
}


// GRID BACKGROUND

@Composable
private fun WeekGridBackground(
    startHour: Int,
    endHour: Int,
    grayColor: Color
) {
    val hourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
    val totalHeight = ((endHour - startHour) * hourSlotHeight.value).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        for (hour in startHour until endHour) {
            WeekGridHourRow(
                hour = hour,
                hourSlotHeight = hourSlotHeight,
                grayColor = grayColor
            )
        }
    }
}

@Composable
private fun WeekGridHourRow(
    hour: Int,
    hourSlotHeight: Dp,
    grayColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(hourSlotHeight)
    ) {
        // Colonna orario (stessa larghezza del selettore giorni)
        Box(
            modifier = Modifier
                .width(CalendarLayoutConstants.TIME_COLUMN_WIDTH)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = String.format("%02d", hour),
                color = grayColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .offset(y = (-12).dp)
            )
        }

        // Area griglia con linea orizzontale
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            HorizontalDivider(
                color = grayColor.copy(alpha = 0.15f),
                thickness = 1.dp,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}


// EVENTS OVERLAY

@Composable
private fun WeekEventsOverlay(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    startHour: Int,
    onEventClick: (CourseEvent) -> Unit,
    primaryColor: Color
) {
    val hourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT

    Layout(
        content = {
            daysOfWeek.forEachIndexed { dayIndex, date ->
                val dayEvents = events.filter { event ->
                    event.startTime.toLocalDate() == date
                }

                dayEvents.forEach { event ->
                    val durationMinutes = java.time.Duration.between(
                        event.startTime,
                        event.endTime
                    ).toMinutes()
                    val eventHeightDp = (durationMinutes / 60f * hourSlotHeight.value).dp

                    WeekEventBox(
                        event = event,
                        height = eventHeightDp,
                        onClick = { onEventClick(event) },
                        primaryColor = primaryColor,
                        modifier = Modifier.layoutId("${dayIndex}_${event.id}")
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalendarLayoutConstants.TIME_COLUMN_WIDTH)
    ) { measurables, constraints ->
        val dayWidth = constraints.maxWidth / WeekGridConstants.TOTAL_DAYS
        val totalHours = CalendarUtils.WEEK_TOTAL_HOURS
        val maxLayoutHeight = (totalHours * hourSlotHeight.toPx()).toInt()

        val placeables = measurables.mapNotNull { measurable ->
            val layoutId = measurable.layoutId as String
            val parts = layoutId.split("_")
            val dayIndex = parts[0].toInt()
            val eventId = parts[1].toLong()

            val event = events.firstOrNull { it.id == eventId }
            event?.let {
                val startMinutes = it.startTime.hour * 60 + it.startTime.minute
                val startOffsetMinutes = startMinutes - (startHour * 60)
                val topOffsetPx = (startOffsetMinutes / 60f * hourSlotHeight.toPx()).toInt()

                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = 0,
                        maxWidth = dayWidth - WeekGridConstants.EVENT_HORIZONTAL_PADDING * 2,
                        minHeight = 0
                    )
                )

                Triple(
                    placeable,
                    dayIndex * dayWidth + WeekGridConstants.EVENT_HORIZONTAL_PADDING,
                    topOffsetPx
                )
            }
        }

        layout(constraints.maxWidth, maxLayoutHeight) {
            placeables.forEach { (placeable, x, y) ->
                placeable.place(x, y)
            }
        }
    }
}


// EVENT BOX

@Composable
private fun WeekEventBox(
    event: CourseEvent,
    height: Dp,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)

    Surface(
        modifier = modifier.height(height),
        shape = RoundedCornerShape(8.dp),
        color = eventColor,
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = event.courseName,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = calculateMaxLines(height),
                    lineHeight = 11.sp,
                    overflow = TextOverflow.Ellipsis
                )

                if (height > 50.dp) {
                    event.room?.let { room ->
                        Text(
                            text = room,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun calculateMaxLines(height: Dp): Int {
    return when {
        height < 35.dp -> 1
        height < 50.dp -> 2
        else -> 3
    }
}


// LOADING STATE

@Composable
private fun WeekGridLoadingState(primaryColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            strokeWidth = 3.dp,
            color = primaryColor
        )
    }
}
