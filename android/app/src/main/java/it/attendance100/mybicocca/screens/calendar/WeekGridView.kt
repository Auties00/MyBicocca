package it.attendance100.mybicocca.screens.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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

    Column(modifier = Modifier.fillMaxSize()) {
        // [SYNC] Header fittizio per allineare verticalmente la griglia con DayTimelineView
        WeekTimelineHeader()

        when {
            isLoading -> WeekGridLoadingState(primaryColor)
            else -> WeekGridContent(
                daysOfWeek = daysOfWeek,
                events = weekEvents,
                grayColor = grayColor,
                primaryColor = primaryColor,
                scrollState = scrollState,
                onEventClick = onEventClick,
                onSwipeWeek = onSwipeWeek,
                onDayZoom = onDayZoom
            )
        }
    }
}

// [SYNC] Header che imita altezza/padding di DayTimelineHeader per allineare le griglie
@Composable
private fun WeekTimelineHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Testo invisibile ma presente per occupare lo stesso spazio
        Text(
            text = stringResource(R.string.calendar_overview),
            color = Color.Transparent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
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
    onEventClick: (CourseEvent) -> Unit,
    onSwipeWeek: (Int) -> Unit,
    onDayZoom: (LocalDate) -> Unit
) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var isSwiping by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        WeekGridBackground(
            startHour = CalendarUtils.WEEK_START_HOUR,
            endHour = CalendarUtils.WEEK_END_HOUR,
            grayColor = grayColor
        )

        WeekEventsOverlay(
            daysOfWeek = daysOfWeek,
            events = events,
            startHour = CalendarUtils.WEEK_START_HOUR,
            onEventClick = onEventClick,
            onDayZoom = onDayZoom,
            primaryColor = primaryColor
        )

        CurrentTimeWeekIndicator(
            daysOfWeek = daysOfWeek,
            currentTime = currentTime,
            startHour = CalendarUtils.WEEK_START_HOUR,
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

    Column(modifier = Modifier
        .fillMaxWidth()
        .height(totalHeight)) {
        for (hour in startHour until endHour) {
            WeekGridHourRow(hour, hourSlotHeight, grayColor)
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
                fontSize = 12.sp, fontWeight = FontWeight.Normal,
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
    primaryColor: Color
) {
    val hourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
    val density = LocalDensity.current

    Layout(
        content = {
            daysOfWeek.forEachIndexed { dayIndex, date ->
                val dayEvents =
                    events.filter { it.startTime.toLocalDate() == date }.sortedBy { it.startTime }
                dayEvents.forEach { event ->
                    val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                    val visualDuration = maxOf(durationMinutes, 15L)
                    val eventHeightDp = (visualDuration / 60f * hourSlotHeight.value).dp

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
        val maxLayoutHeight = (totalHours * hourSlotHeight.toPx()).toInt()
        val placeables = mutableListOf<Triple<Placeable, Int, Int>>()

        val measurablesByDay = measurables.groupBy { (it.layoutId as String).split("_")[0].toInt() }

        measurablesByDay.forEach { (dayIndex, dayMeasurables) ->
            val dayEventInfos = dayMeasurables.mapNotNull { measurable ->
                val eventId = (measurable.layoutId as String).split("_")[1].toLong()
                val event = events.firstOrNull { it.id == eventId } ?: return@mapNotNull null
                val startMinutes = event.startTime.hour * 60 + event.startTime.minute
                val startOffsetMinutes = startMinutes - (startHour * 60)
                val topPx = (startOffsetMinutes / 60f * hourSlotHeight.toPx()).toInt()
                val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                val heightPx = (maxOf(durationMinutes, 15L) / 60f * hourSlotHeight.toPx()).toInt()
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
    primaryColor: Color
) {
    val today = LocalDate.now()
    val density = LocalDensity.current
    val hourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
    val currentHour = currentTime.hour

    if (currentHour >= startHour && currentHour < CalendarUtils.WEEK_END_HOUR) {
        val totalMinutes = (currentHour - startHour) * 60 + currentTime.minute
        val topOffsetDp = (totalMinutes / 60f * hourSlotHeight.value).dp
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalMinutes.dp)
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