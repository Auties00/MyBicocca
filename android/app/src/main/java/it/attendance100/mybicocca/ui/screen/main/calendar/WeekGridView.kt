package it.attendance100.mybicocca.ui.screen.main.calendar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.data.local.entity.CourseEvent
import it.attendance100.mybicocca.util.CalendarUtils
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
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
        when {
            isLoading -> WeekGridLoadingState(primaryColor)
            else -> WeekGridContentWithZoom(
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

// LOADING STATE

@Composable
private fun WeekGridLoadingState(primaryColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = primaryColor)
    }
}

// GRID CONTENT WITH PINCH-TO-ZOOM

@Composable
private fun WeekGridContentWithZoom(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    grayColor: Color,
    primaryColor: Color,
    scrollState: ScrollState,
    onEventClick: (CourseEvent) -> Unit,
    onSwipeWeek: (Int) -> Unit,
    onDayZoom: (LocalDate) -> Unit
) {
    val density = LocalDensity.current
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    val totalHours = CalendarUtils.WEEK_END_HOUR - CalendarUtils.WEEK_START_HOUR
    val baseContentHeight = CalendarUtils.HOUR_SLOT_HEIGHT * totalHours

    var containerHeight by remember { mutableFloatStateOf(0f) }

    val minZoom by remember(containerHeight) {
        derivedStateOf {
            if (containerHeight > 0) {
                val baseHeightPx = with(density) { baseContentHeight.toPx() }
                (containerHeight / baseHeightPx).coerceIn(0.3f, 1f)
            } else {
                0.5f
            }
        }
    }

    var targetZoom by remember { mutableFloatStateOf(CalendarUtils.DEFAULT_ZOOM) }
    val zoomLevel by animateFloatAsState(
        targetValue = targetZoom,
        animationSpec = tween(durationMillis = 150),
        label = "zoomAnimation"
    )

    val isCompactMode by remember(zoomLevel, minZoom) {
        derivedStateOf { zoomLevel <= minZoom * 1.1f }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerHeight = size.height.toFloat()
            }
            .pointerInput(minZoom) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var secondPointer: PointerId? = null
                    var initialSpan = 0f

                    withTimeoutOrNull(100L) {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pointers = event.changes.filter { it.pressed }

                            if (pointers.size >= 2) {
                                secondPointer = pointers[1].id
                                val p1 = pointers[0].position
                                val p2 = pointers[1].position
                                initialSpan = (p1 - p2).getDistance()
                                break
                            }

                            if (pointers.isEmpty()) {
                                break
                            }
                        }
                    }

                    if (secondPointer != null && initialSpan > 0f) {
                        val initialZoom = targetZoom

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pointers = event.changes.filter { it.pressed }

                            if (pointers.size < 2) break

                            val currentSpan =
                                (pointers[0].position - pointers[1].position).getDistance()
                            val scaleFactor = currentSpan / initialSpan
                            val newZoom = (initialZoom * scaleFactor).coerceIn(
                                minZoom,
                                CalendarUtils.MAX_ZOOM
                            )

                            targetZoom = newZoom
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                var totalDragX = 0f
                var isSwiping = false
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        isSwiping = false
                    },
                    onDragEnd = {
                        if (isSwiping) {
                            val direction = if (totalDragX > 0) 1 else -1
                            onSwipeWeek(direction)
                        }
                        isSwiping = false
                    },
                    onDragCancel = { isSwiping = false },
                    onHorizontalDrag = { change, dragAmount ->
                        totalDragX += dragAmount
                        if (totalDragX.absoluteValue > 100f) {
                            isSwiping = true
                            change.consume()
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (isCompactMode) {
                // Passiamo l'altezza del container per garantire che riempia lo schermo
                // anche all'interno della Column scrollabile
                val density = LocalDensity.current
                val heightModifier = if (containerHeight > 0) {
                    Modifier.height(with(density) { containerHeight.toDp() })
                } else {
                    Modifier.fillMaxSize()
                }

                CompactWeekView(
                    daysOfWeek = daysOfWeek,
                    events = events,
                    onEventClick = onEventClick,
                    onDayZoom = onDayZoom,
                    primaryColor = primaryColor,
                    grayColor = grayColor,
                    modifier = heightModifier
                )
            } else {
                Box {
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
    }
}

// COMPACT WEEK VIEW (per zoom minimo)

@Composable
private fun CompactWeekView(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    onEventClick: (CourseEvent) -> Unit,
    onDayZoom: (LocalDate) -> Unit,
    primaryColor: Color,
    grayColor: Color,
    modifier: Modifier = Modifier
) {
    val eventsByDay = remember(events, daysOfWeek) {
        daysOfWeek.associateWith { date ->
            events.filter { it.startTime.toLocalDate() == date }
                .sortedBy { it.startTime }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(start = CalendarUtils.TIME_COLUMN_WIDTH)
    ) {
        val availableHeight = maxHeight
        val eventSpacing = 2.dp
        val verticalPadding = 8.dp
        val maxComfortableEventHeight = 120.dp

        // Calcolo globale per uniformare l'altezza delle card in tutti i giorni
        val maxEventsCount = eventsByDay.values.maxOfOrNull { it.size } ?: 0
        val totalSpacingForMax = if (maxEventsCount > 0) {
            (eventSpacing * (maxEventsCount - 1)) + verticalPadding
        } else 0.dp

        val totalNeededHeightForMax =
            (maxComfortableEventHeight * maxEventsCount) + totalSpacingForMax
        val useCompactHeight = totalNeededHeightForMax > availableHeight

        val unifiedEventHeight = if (useCompactHeight && maxEventsCount > 0) {
            (availableHeight - totalSpacingForMax) / maxEventsCount
        } else {
            maxComfortableEventHeight
        }

        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            daysOfWeek.forEachIndexed { index, date ->
                val dayEvents = eventsByDay[date] ?: emptyList()
                val isToday = date == LocalDate.now()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(if (index < 6) Modifier.padding(end = 1.dp) else Modifier)
                        .background(
                            if (isToday) primaryColor.copy(alpha = 0.05f)
                            else Color.Transparent
                        )
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(eventSpacing)
                ) {
                    dayEvents.forEach { event ->
                        val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
                        dayEvents.any { other ->
                            other.id != event.id &&
                                    event.startTime.isBefore(other.endTime) &&
                                    event.endTime.isAfter(other.startTime)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(unifiedEventHeight),
                            shape = RoundedCornerShape(6.dp),
                            color = eventColor,
                            onClick = { onEventClick(event) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = event.courseName,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = if (unifiedEventHeight > 60.dp) 3 else 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 11.sp
                                    )

                                    if (unifiedEventHeight > 40.dp) {
                                        Text(
                                            text = event.startTime.format(CalendarUtils.timeFormatter),
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 7.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
    val scaledHourSlotHeight = (CalendarUtils.HOUR_SLOT_HEIGHT.value * zoomLevel).dp
    val totalHeight = ((endHour - startHour) * scaledHourSlotHeight.value).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        for (hour in startHour until endHour) {
            WeekGridHourRow(hour, scaledHourSlotHeight, grayColor)
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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            DashedDivider(
                color = grayColor.copy(alpha = 0.15f),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun DashedDivider(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// OVERLAP DETECTION & GROUPING

/**
 * Rappresenta un gruppo di eventi che si sovrappongono temporalmente.
 */
private data class OverlapGroup(
    val events: List<CourseEvent>,
    val groupStartMinutes: Int,
    val groupEndMinutes: Int
)

/**
 * Raggruppa gli eventi di un giorno in base alle sovrapposizioni.
 */
private fun groupOverlappingEvents(
    dayEvents: List<CourseEvent>,
    startHour: Int
): List<OverlapGroup> {
    if (dayEvents.isEmpty()) return emptyList()

    val sortedEvents = dayEvents.sortedBy { it.startTime }
    val groups = mutableListOf<OverlapGroup>()

    var currentGroupEvents = mutableListOf<CourseEvent>()
    var currentGroupEnd = Int.MIN_VALUE

    sortedEvents.forEach { event ->
        val eventStartMinutes = (event.startTime.hour - startHour) * 60 + event.startTime.minute
        val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime).toInt()
        val eventEndMinutes = eventStartMinutes + maxOf(durationMinutes, 15)

        if (currentGroupEvents.isEmpty()) {
            currentGroupEvents.add(event)
            currentGroupEnd = eventEndMinutes
        } else if (eventStartMinutes < currentGroupEnd) {
            currentGroupEvents.add(event)
            currentGroupEnd = maxOf(currentGroupEnd, eventEndMinutes)
        } else {
            val groupStart = (currentGroupEvents.first().startTime.hour - startHour) * 60 +
                    currentGroupEvents.first().startTime.minute
            groups.add(OverlapGroup(currentGroupEvents.toList(), groupStart, currentGroupEnd))

            currentGroupEvents = mutableListOf(event)
            currentGroupEnd = eventEndMinutes
        }
    }

    if (currentGroupEvents.isNotEmpty()) {
        val groupStart = (currentGroupEvents.first().startTime.hour - startHour) * 60 +
                currentGroupEvents.first().startTime.minute
        groups.add(OverlapGroup(currentGroupEvents.toList(), groupStart, currentGroupEnd))
    }

    return groups
}

// EVENTS OVERLAY - STACKING APPROACH

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
    val scaledHourSlotHeight = (CalendarUtils.HOUR_SLOT_HEIGHT.value * zoomLevel).dp
    val density = LocalDensity.current

    val groupsByDay = remember(events, daysOfWeek, startHour) {
        daysOfWeek.mapIndexed { dayIndex, date ->
            val dayEvents = events.filter { it.startTime.toLocalDate() == date }
            dayIndex to groupOverlappingEvents(dayEvents, startHour)
        }.toMap()
    }

    Layout(
        content = {
            daysOfWeek.forEachIndexed { dayIndex, date ->
                val dayGroups = groupsByDay[dayIndex] ?: emptyList()

                dayGroups.forEachIndexed { groupIndex, group ->
                    if (group.events.size == 1) {
                        // Evento singolo - larghezza piena
                        val event = group.events.first()
                        val durationMinutes =
                            ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                        val visualDuration = maxOf(durationMinutes, 15L)
                        val eventHeightDp = (visualDuration / 60f * scaledHourSlotHeight.value).dp

                        WeekEventCard(
                            event = event,
                            height = eventHeightDp,
                            onClick = { onEventClick(event) },
                            primaryColor = primaryColor,
                            isStacked = false,
                            stackPosition = 0,
                            totalInStack = 1,
                            modifier = Modifier.layoutId("single_${dayIndex}_${event.id}")
                        )
                    } else {
                        // Gruppo sovrapposto - stacking visivo
                        val sortedGroupEvents = group.events.sortedBy { it.startTime }

                        sortedGroupEvents.forEachIndexed { stackIndex, event ->
                            val durationMinutes =
                                ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                            val visualDuration = maxOf(durationMinutes, 15L)
                            val eventHeightDp =
                                (visualDuration / 60f * scaledHourSlotHeight.value).dp

                            WeekEventCard(
                                event = event,
                                height = eventHeightDp,
                                onClick = { onDayZoom(date) },
                                primaryColor = primaryColor,
                                isStacked = true,
                                stackPosition = stackIndex,
                                totalInStack = group.events.size,
                                modifier = Modifier.layoutId("stacked_${dayIndex}_${groupIndex}_${event.id}")
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalendarUtils.TIME_COLUMN_WIDTH)
    ) { measurables, constraints ->

        val totalDays = CalendarUtils.TOTAL_DAYS
        val dayWidthPx = constraints.maxWidth / totalDays.toFloat()
        val totalHours = CalendarUtils.WEEK_TOTAL_HOURS
        val hourHeightPx = scaledHourSlotHeight.toPx()
        val maxLayoutHeight = (totalHours * hourHeightPx).toInt()

        val dayPaddingPx = with(density) { 2.dp.toPx() }
        val stackOffsetXPx = with(density) { 5.dp.toPx() }
        val stackOffsetYPx = with(density) { 3.dp.toPx() }

        val placeables = mutableListOf<PlaceableInfo>()

        measurables.forEach { measurable ->
            val layoutId = measurable.layoutId as String
            val parts = layoutId.split("_")

            when (parts[0]) {
                "single" -> {
                    val dayIndex = parts[1].toInt()
                    val eventId = parts[2].toLong()
                    val event = events.first { it.id == eventId }

                    val eventStartMinutes =
                        (event.startTime.hour - startHour) * 60 + event.startTime.minute
                    val durationMinutes =
                        ChronoUnit.MINUTES.between(event.startTime, event.endTime).toInt()
                    val visualDuration = maxOf(durationMinutes, 15)

                    val eventWidth = (dayWidthPx - dayPaddingPx * 2).toInt().coerceAtLeast(1)
                    val eventHeight = (visualDuration / 60f * hourHeightPx).toInt().coerceAtLeast(1)

                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = eventWidth,
                            maxWidth = eventWidth,
                            minHeight = eventHeight,
                            maxHeight = eventHeight
                        )
                    )

                    val xPos = (dayIndex * dayWidthPx + dayPaddingPx).toInt()
                    val yPos = (eventStartMinutes / 60f * hourHeightPx).toInt()

                    placeables.add(PlaceableInfo(placeable, xPos, yPos, zIndex = 0f))
                }

                "stacked" -> {
                    val dayIndex = parts[1].toInt()
                    val groupIndex = parts[2].toInt()
                    val eventId = parts[3].toLong()
                    val event = events.first { it.id == eventId }

                    val dayGroups = groupsByDay[dayIndex] ?: emptyList()
                    val group = dayGroups.getOrNull(groupIndex) ?: return@forEach
                    val sortedGroupEvents = group.events.sortedBy { it.startTime }
                    val stackIndex = sortedGroupEvents.indexOfFirst { it.id == eventId }

                    if (stackIndex < 0) return@forEach

                    val eventStartMinutes =
                        (event.startTime.hour - startHour) * 60 + event.startTime.minute
                    val durationMinutes =
                        ChronoUnit.MINUTES.between(event.startTime, event.endTime).toInt()
                    val visualDuration = maxOf(durationMinutes, 15)

                    // Larghezza ridotta per vedere le card dietro
                    val maxVisibleStack = minOf(group.events.size, 3)
                    val totalStackOffset = (maxVisibleStack - 1) * stackOffsetXPx
                    val baseWidth = dayWidthPx - dayPaddingPx * 2 - totalStackOffset
                    val eventWidth = baseWidth.toInt().coerceAtLeast(1)
                    val eventHeight = (visualDuration / 60f * hourHeightPx).toInt().coerceAtLeast(1)

                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = eventWidth,
                            maxWidth = eventWidth,
                            minHeight = eventHeight,
                            maxHeight = eventHeight
                        )
                    )
                    val visualStackIndex = minOf(stackIndex, 2)
                    val xPos =
                        (dayIndex * dayWidthPx + dayPaddingPx + visualStackIndex * stackOffsetXPx).toInt()
                    val yPos =
                        (eventStartMinutes / 60f * hourHeightPx + visualStackIndex * stackOffsetYPx).toInt()

                    // Z-index: eventi più recenti (in fondo alla lista) sopra
                    val zIndex = stackIndex.toFloat()

                    placeables.add(PlaceableInfo(placeable, xPos, yPos, zIndex))
                }
            }
        }

        layout(constraints.maxWidth, maxLayoutHeight) {
            placeables.sortedBy { it.zIndex }.forEach { info ->
                info.placeable.place(info.x, info.y, zIndex = info.zIndex)
            }
        }
    }
}

private data class PlaceableInfo(
    val placeable: Placeable,
    val x: Int,
    val y: Int,
    val zIndex: Float
)

// EVENT CARD

@Composable
private fun WeekEventCard(
    event: CourseEvent,
    height: Dp,
    onClick: () -> Unit,
    primaryColor: Color,
    isStacked: Boolean,
    stackPosition: Int,
    totalInStack: Int,
    modifier: Modifier = Modifier
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val showText = height >= 16.dp
    val showLocation = height >= 40.dp

    val elevation = if (isStacked) (stackPosition + 2).dp else 2.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(6.dp),
        color = eventColor,
        shadowElevation = elevation,
        border = BorderStroke(
            width = 0.5.dp,
            color = Color.White.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (showText) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = event.courseName,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = if (showLocation) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 11.sp
                    )

                    val locationText = CalendarUtils.formatEventLocation(event.room, event.building)
                    if (showLocation && !locationText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = locationText,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 8.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Badge +N per eventi sovrapposti
            if (isStacked && stackPosition == totalInStack - 1 && totalInStack > 1) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Text(
                        text = "$totalInStack",
                        color = eventColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// CURRENT TIME INDICATOR

@Composable
private fun CurrentTimeWeekIndicator(
    daysOfWeek: List<LocalDate>,
    currentTime: LocalTime,
    startHour: Int,
    primaryColor: Color,
    zoomLevel: Float
) {
    val today = LocalDate.now()
    val todayIndex = daysOfWeek.indexOfFirst { it == today }

    if (todayIndex < 0) return
    if (currentTime.hour < startHour || currentTime.hour >= CalendarUtils.WEEK_END_HOUR) return

    val scaledHourSlotHeight = (CalendarUtils.HOUR_SLOT_HEIGHT.value * zoomLevel).dp
    val minutesSinceStart = (currentTime.hour - startHour) * 60 + currentTime.minute
    val topOffset = (minutesSinceStart / 60f * scaledHourSlotHeight.value).dp
    val dotSize = 10.dp
    val lineHeight = 2.dp

    Layout(
        content = {
            Spacer(
                modifier = Modifier
                    .layoutId("line")
                    .fillMaxWidth()
                    .height(lineHeight)
                    .background(primaryColor)
            )
            Spacer(
                modifier = Modifier
                    .layoutId("dot")
                    .size(dotSize)
                    .background(primaryColor, CircleShape)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalendarUtils.TIME_COLUMN_WIDTH)
    ) { measurables, constraints ->
        val lineMeasurable = measurables.first { it.layoutId == "line" }
        val dotMeasurable = measurables.first { it.layoutId == "dot" }

        val linePlaceable = lineMeasurable.measure(constraints)
        val dotPlaceable = dotMeasurable.measure(
            constraints.copy(minWidth = 0, minHeight = 0)
        )

        val dayWidthPx = constraints.maxWidth / CalendarUtils.TOTAL_DAYS.toFloat()
        val topPx = topOffset.roundToPx()

        val dotX = ((todayIndex + 1) * dayWidthPx - dotPlaceable.width / 2).toInt()
            .coerceIn(0, constraints.maxWidth - dotPlaceable.width)

        val dotY = topPx - (dotPlaceable.height - linePlaceable.height) / 2

        layout(constraints.maxWidth, (topPx + dotPlaceable.height).coerceAtLeast(1)) {
            linePlaceable.place(0, topPx)
            dotPlaceable.place(dotX, dotY)
        }
    }
}