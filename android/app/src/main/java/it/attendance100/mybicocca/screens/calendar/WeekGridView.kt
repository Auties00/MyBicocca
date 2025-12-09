package it.attendance100.mybicocca.screens.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

// MAIN COMPONENT - WEEK GRID VIEW

@Composable
fun WeekGridView(
    displayedWeekStart: LocalDate,
    events: List<CourseEvent>,
    isLoading: Boolean,
    grayColor: Color,
    primaryColor: Color,
    scrollState: ScrollState,
    onEventClick: (CourseEvent) -> Unit,
    onSwipeWeek: (Int) -> Unit,
    onDayZoom: (LocalDate) -> Unit
) {
    val daysOfWeek = remember(displayedWeekStart) {
        (0..6).map { displayedWeekStart.plusDays(it.toLong()) }
    }

    val weekEvents = remember(events, daysOfWeek) {
        events.filter { event ->
            val eventDate = event.startTime.toLocalDate()
            !eventDate.isBefore(daysOfWeek.first()) && !eventDate.isAfter(daysOfWeek.last())
        }
    }
    
    // Stato zoom per pinch-to-zoom
    var zoomLevel by remember { mutableFloatStateOf(CalendarUtils.DEFAULT_ZOOM) }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> WeekGridLoadingState(primaryColor)
            else -> WeekGridContent(
                daysOfWeek = daysOfWeek,
                events = weekEvents,
                grayColor = grayColor,
                primaryColor = primaryColor,
                scrollState = scrollState,
                zoomLevel = zoomLevel,
                onZoomChange = { newZoom -> 
                    zoomLevel = newZoom.coerceIn(CalendarUtils.MIN_ZOOM, CalendarUtils.MAX_ZOOM)
                },
                onEventClick = onEventClick,
                onSwipeWeek = onSwipeWeek,
                onDayZoom = onDayZoom
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
    scrollState: ScrollState,
    zoomLevel: Float,
    onZoomChange: (Float) -> Unit,
    onEventClick: (CourseEvent) -> Unit,
    onSwipeWeek: (Int) -> Unit,
    onDayZoom: (LocalDate) -> Unit
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var isSwiping by remember { mutableStateOf(false) }
    
    // Calcola se siamo in modalità compatta
    val isCompactMode = zoomLevel < CalendarUtils.COMPACT_THRESHOLD

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(zoomLevel) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) {
                        onZoomChange(zoomLevel * zoom)
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { isSwiping = false },
                    onDragCancel = { isSwiping = false }
                ) { _, dragAmount ->
                    if (!isSwiping && dragAmount.absoluteValue > 20) {
                        val direction = if (dragAmount > 0) 1 else -1
                        onSwipeWeek(direction)
                        isSwiping = true
                    }
                }
            }
            .verticalScroll(scrollState)
    ) {
        if (isCompactMode) {
            // Vista compatta: eventi impilati senza spazi
            CompactWeekView(
                daysOfWeek = daysOfWeek,
                events = events,
                onEventClick = onEventClick,
                onDayZoom = onDayZoom,
                primaryColor = primaryColor,
                grayColor = grayColor
            )
        } else {
            // Vista normale con zoom
            WeekGridBackground(
                startHour = CalendarUtils.WEEK_START_HOUR,
                endHour = CalendarUtils.WEEK_END_HOUR,
                grayColor = grayColor,
                zoomLevel = zoomLevel
            )

            WeekEventsOverlay(
                daysOfWeek = daysOfWeek,
                events = events,
                startHour = CalendarUtils.WEEK_START_HOUR,
                onEventClick = onEventClick,
                onDayZoom = onDayZoom,
                primaryColor = primaryColor,
                zoomLevel = zoomLevel
            )

            CurrentTimeWeekIndicator(
                daysOfWeek = daysOfWeek,
                currentTime = currentTime,
                startHour = CalendarUtils.WEEK_START_HOUR,
                primaryColor = primaryColor,
                zoomLevel = zoomLevel
            )
        }
    }
}

// GRID BACKGROUND

@Composable
private fun WeekGridBackground(
    startHour: Int,
    endHour: Int,
    grayColor: Color,
    zoomLevel: Float
) {
    val baseHourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
    val scaledHourSlotHeight = (baseHourSlotHeight.value * zoomLevel).dp
    val totalHeight = ((endHour - startHour) * scaledHourSlotHeight.value).dp

    Column(modifier = Modifier
        .fillMaxWidth()
        .height(totalHeight)) {
        for (hour in startHour until endHour) {
            WeekGridHourRow(hour, scaledHourSlotHeight, grayColor)
        }
    }
}

@Composable
private fun WeekGridHourRow(hour: Int, hourSlotHeight: Dp, grayColor: Color) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(hourSlotHeight)) {
        Box(
            modifier = Modifier
                .width(CalendarUtils.TIME_COLUMN_WIDTH)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = String.format("%02d", hour),
                color = grayColor.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .offset(y = (-8).dp)
            )
        }
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            DashedDivider(
                color = grayColor.copy(alpha = 0.15f),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun DashedDivider(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// EVENTS OVERLAY

@Composable
private fun WeekEventsOverlay(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    startHour: Int,
    onEventClick: (CourseEvent) -> Unit,
    onDayZoom: (LocalDate) -> Unit,
    primaryColor: Color,
    zoomLevel: Float
) {
    val baseHourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
    val scaledHourSlotHeight = (baseHourSlotHeight.value * zoomLevel).dp
    val density = LocalDensity.current

    Layout(
        content = {
            daysOfWeek.forEachIndexed { dayIndex, date ->
                val dayEvents =
                    events.filter { it.startTime.toLocalDate() == date }.sortedBy { it.startTime }
                dayEvents.forEach { event ->
                    val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                    val visualDuration = maxOf(durationMinutes, 15L)
                    val eventHeightDp = (visualDuration / 60f * scaledHourSlotHeight.value).dp

                    val hasOverlap = remember(dayEvents) {
                        dayEvents.any { other ->
                            other.id != event.id &&
                                    (event.startTime.isBefore(other.endTime) && event.endTime.isAfter(other.startTime))
                        }
                    }

                    WeekEventBox(
                        event = event,
                        height = eventHeightDp,
                        onClick = {
                            if (hasOverlap) onDayZoom(date) else onEventClick(event)
                        },
                        primaryColor = primaryColor,
                        modifier = Modifier.layoutId("${dayIndex}_${event.id}")
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalendarUtils.TIME_COLUMN_WIDTH)
    ) { measurables, constraints ->
        val dayWidthPx = constraints.maxWidth / CalendarUtils.TOTAL_DAYS.toFloat()
        val totalHours = CalendarUtils.WEEK_TOTAL_HOURS
        val maxLayoutHeight = (totalHours * scaledHourSlotHeight.toPx()).toInt()
        val placeables = mutableListOf<Triple<Placeable, Int, Int>>()

        val measurablesByDay = measurables.groupBy { (it.layoutId as String).split("_")[0].toInt() }

        measurablesByDay.forEach { (dayIndex, dayMeasurables) ->
            val dayEventInfos = dayMeasurables.mapNotNull { measurable ->
                val eventId = (measurable.layoutId as String).split("_")[1].toLong()
                val event = events.firstOrNull { it.id == eventId } ?: return@mapNotNull null
                val startMinutes = event.startTime.hour * 60 + event.startTime.minute
                val startOffsetMinutes = startMinutes - (startHour * 60)
                val topPx = (startOffsetMinutes / 60f * scaledHourSlotHeight.toPx()).toInt()
                val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                val heightPx = (maxOf(durationMinutes, 15L) / 60f * scaledHourSlotHeight.toPx()).toInt()
                EventLayoutInfo(measurable, topPx, topPx + heightPx)
            }.sortedBy { it.topPx }

            val columns = mutableListOf<MutableList<EventLayoutInfo>>()
            dayEventInfos.forEach { info ->
                var placed = false
                for (column in columns) {
                    if (info.topPx >= column.last().bottomPx) {
                        column.add(info)
                        info.columnIndex = columns.indexOf(column)
                        placed = true
                        break
                    }
                }
                if (!placed) {
                    val newCol = mutableListOf(info)
                    columns.add(newCol)
                    info.columnIndex = columns.size - 1
                }
            }

            dayEventInfos.forEach { info ->
                val colIndex = info.columnIndex
                val overlapOffsetPx =
                    with(density) { CalendarUtils.OVERLAP_OFFSET.toPx() } * colIndex
                val widthPx =
                    (dayWidthPx - overlapOffsetPx - with(density) { 2.dp.toPx() }).coerceAtLeast(10f)
                val placeable = info.measurable.measure(
                    constraints.copy(
                        minWidth = 0,
                        maxWidth = widthPx.toInt(),
                        minHeight = 0
                    )
                )
                val xPos =
                    (dayIndex * dayWidthPx + overlapOffsetPx + with(density) { 1.dp.toPx() }).toInt()
                placeables.add(Triple(placeable, xPos, info.topPx))
            }
        }
        layout(constraints.maxWidth, maxLayoutHeight) {
            placeables.forEach { (placeable, x, y) -> placeable.place(x, y) }
        }
    }
}

private data class EventLayoutInfo(
    val measurable: Measurable,
    val topPx: Int,
    val bottomPx: Int,
    var columnIndex: Int = 0
)

@Composable
private fun WeekEventBox(
    event: CourseEvent,
    height: Dp,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val showText = height >= 14.dp
    val showDetails = height >= 35.dp

    Surface(
        modifier = modifier
            .height(height)
            .padding(bottom = 1.dp),
        shape = RoundedCornerShape(6.dp),
        color = eventColor.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        if (showText) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 2.dp)) {
                Text(
                    text = event.courseName,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (showDetails) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 10.sp
                )
                if (showDetails) {
                    event.room?.let { room ->
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = room,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentTimeWeekIndicator(
    daysOfWeek: List<LocalDate>,
    currentTime: LocalTime,
    startHour: Int,
    primaryColor: Color,
    zoomLevel: Float
) {
    val today = LocalDate.now()
    val density = LocalDensity.current
    val baseHourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
    val scaledHourSlotHeight = (baseHourSlotHeight.value * zoomLevel).dp
    val currentHour = currentTime.hour

    if (currentHour >= startHour && currentHour < CalendarUtils.WEEK_END_HOUR) {
        val totalMinutes = (currentHour - startHour) * 60 + currentTime.minute
        val topOffsetDp = (totalMinutes / 60f * scaledHourSlotHeight.value).dp
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(topOffsetDp + 10.dp)
                .padding(start = CalendarUtils.TIME_COLUMN_WIDTH)
        ) {
            val lineY = with(density) { topOffsetDp.toPx() }
            drawLine(
                color = primaryColor,
                start = Offset(0f, lineY),
                end = Offset(size.width, lineY),
                strokeWidth = 1.dp.toPx()
            )
            if (daysOfWeek.contains(today)) {
                val dayIndex = daysOfWeek.indexOf(today)
                val dayWidth = size.width / CalendarUtils.TOTAL_DAYS
                val circleX = (dayIndex * dayWidth)
                drawCircle(
                    color = primaryColor,
                    radius = 3.dp.toPx(),
                    center = Offset(circleX, lineY)
                )
            }
        }
    }
}

// COMPACT WEEK VIEW - Eventi impilati senza spazi temporali

@Composable
private fun CompactWeekView(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    onEventClick: (CourseEvent) -> Unit,
    onDayZoom: (LocalDate) -> Unit,
    primaryColor: Color,
    grayColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalendarUtils.TIME_COLUMN_WIDTH)
    ) {
        daysOfWeek.forEach { date ->
            val dayEvents = events
                .filter { it.startTime.toLocalDate() == date }
                .sortedBy { it.startTime }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 1.dp)
            ) {
                if (dayEvents.isEmpty()) {
                    // Giorno vuoto - mostra placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CalendarUtils.COMPACT_EVENT_HEIGHT)
                            .padding(1.dp)
                            .background(
                                grayColor.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "—",
                            color = grayColor.copy(alpha = 0.3f),
                            fontSize = 10.sp
                        )
                    }
                } else {
                    dayEvents.forEach { event ->
                        val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
                        val hasOverlap = dayEvents.any { other ->
                            other.id != event.id &&
                                    (event.startTime.isBefore(other.endTime) && event.endTime.isAfter(other.startTime))
                        }
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(CalendarUtils.COMPACT_EVENT_HEIGHT)
                                .padding(vertical = 1.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = eventColor,
                            onClick = {
                                if (hasOverlap) onDayZoom(date) else onEventClick(event)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = event.courseName,
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekGridLoadingState(primaryColor: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            color = primaryColor
        )
    }
}