package it.attendance100.mybicocca.screens.calendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.components.SwipeableCardStack
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.delay
import java.time.*

@Composable
fun DayTimelineView(
    events: List<CourseEvent>,
    selectedDate: LocalDate,
    isLoading: Boolean,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit,
    scrollState: ScrollState // [SYNC] Scroll condiviso
) {
    val isToday = selectedDate == LocalDate.now()

    Column(modifier = Modifier.fillMaxSize()) {
        DayTimelineHeader(eventsCount = events.size, primaryColor = primaryColor)
        when {
            isLoading -> TimelineLoadingState(primaryColor = primaryColor)
            else -> TimelineContent(
                events = events,
                isToday = isToday,
                textColor = textColor,
                grayColor = grayColor,
                primaryColor = primaryColor,
                onEventClick = onEventClick,
                scrollState = scrollState
            )
        }
    }
}

@Composable
private fun DayTimelineHeader(eventsCount: Int, primaryColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.calendar_events),
            color = primaryColor,
            style = MaterialTheme.typography.titleSmall
        )
        if (eventsCount > 0) {
            Surface(shape = RoundedCornerShape(12.dp), color = primaryColor.copy(alpha = 0.12f)) {
                Text(
                    text = eventsCount.toString(),
                    color = primaryColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineContent(
    events: List<CourseEvent>,
    isToday: Boolean,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit,
    scrollState: ScrollState
) {
    val currentTime by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            // Calculate time until next minute to be precise and save resources
            val nextMinute = value.plusMinutes(1).withSecond(0).withNano(0)
            val delayMillis = java.time.Duration.between(value, nextMinute).toMillis()
            // Aggiungi un piccolo buffer di 50ms per sicurezza
            delay(delayMillis + 50)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        TimelineBackground(
            startHour = CalendarUtils.START_HOUR,
            endHour = CalendarUtils.END_HOUR,
            grayColor = grayColor
        )
        TimelineEventsOverlay(
            events = events,
            startHour = CalendarUtils.START_HOUR,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            onEventClick = onEventClick
        )

        if (isToday) {
            val currentHour = currentTime.hour
            if (currentHour >= CalendarUtils.START_HOUR && currentHour <= CalendarUtils.END_HOUR) {
                val totalMinutes =
                    (currentHour - CalendarUtils.START_HOUR) * 60 + currentTime.minute
                val offsetY = (totalMinutes / 60f * CalendarUtils.HOUR_SLOT_HEIGHT.value).dp
                CurrentTimeIndicator(
                    primaryColor = primaryColor, modifier = Modifier.offset(y = offsetY)
                )
            }
        }
    }
}

@Composable
private fun TimelineBackground(startHour: Int, endHour: Int, grayColor: Color) {
    val totalHeight = ((endHour - startHour) * CalendarUtils.HOUR_SLOT_HEIGHT.value).dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        for (hour in startHour until endHour) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CalendarUtils.HOUR_SLOT_HEIGHT)
            ) {
                Box(
                    modifier = Modifier.width(CalendarUtils.TIME_COLUMN_WIDTH),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = String.format("%02d", hour),
                        color = grayColor.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .offset(y = (-12).dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 16.dp)
                ) {
                    DashedHourLine(grayColor = grayColor)
                }
            }
        }
    }
}

@Composable
private fun TimelineEventsOverlay(
    events: List<CourseEvent>,
    startHour: Int,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit
) {
    val density = LocalDensity.current
    val selectedEventsByGroup = remember { mutableStateMapOf<Int, Long>() }
    val overlappingGroups = remember(events) { calculateOverlappingGroups(events) }

    LaunchedEffect(events) {
        overlappingGroups.forEachIndexed { groupIndex, group ->
            if (!selectedEventsByGroup.containsKey(groupIndex)) {
                selectedEventsByGroup[groupIndex] = getEarliestEvent(group.events).id
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Layout(
            content = {
                overlappingGroups.forEachIndexed { groupIndex, group ->
                    if (!selectedEventsByGroup.containsKey(groupIndex)) {
                        selectedEventsByGroup[groupIndex] = getEarliestEvent(group.events).id
                    }
                    val selectedEventId =
                        selectedEventsByGroup[groupIndex] ?: group.events.first().id

                    if (group.events.size > 2) {
                        val selectedEvent =
                            group.events.find { it.id == selectedEventId } ?: group.events.first()
                        val durationMinutes = java.time.Duration.between(
                            selectedEvent.startTime, selectedEvent.endTime
                        ).toMinutes()
                        val cardHeight =
                            (durationMinutes / 60f * CalendarUtils.HOUR_SLOT_HEIGHT.value).dp

                        SwipeableCardStack(
                            events = group.events,
                            currentEventId = selectedEventId,
                            onEventSelected = { eventId ->
                                selectedEventsByGroup[groupIndex] = eventId
                            },
                            onEventClick = onEventClick,
                            cardHeight = cardHeight,
                            textColor = textColor,
                            grayColor = grayColor,
                            primaryColor = primaryColor,
                            modifier = Modifier.layoutId("swipe_group_$groupIndex")
                        )
                    } else {
                        group.visibleEvents.forEachIndexed { index, event ->
                            val durationMinutes =
                                java.time.Duration.between(event.startTime, event.endTime)
                                    .toMinutes()
                            val eventHeightDp =
                                (durationMinutes / 60f * CalendarUtils.HOUR_SLOT_HEIGHT.value).dp
                            TimelineEventCard(
                                event = event,
                                height = eventHeightDp,
                                textColor = textColor,
                                grayColor = grayColor,
                                primaryColor = primaryColor,
                                onClick = { onEventClick(event) },
                                modifier = Modifier.layoutId("event_${event.id}"),
                                stackDepth = 0
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = CalendarUtils.TIME_COLUMN_WIDTH, end = 16.dp),
            measurePolicy = { measurables, constraints ->
                val totalHours = CalendarUtils.END_HOUR - CalendarUtils.START_HOUR
                val maxLayoutHeight =
                    with(density) { (totalHours * CalendarUtils.HOUR_SLOT_HEIGHT).toPx().toInt() }
                val placeables = mutableListOf<Triple<Placeable, Int, Int>>()

                overlappingGroups.forEachIndexed { groupIndex, group ->
                    if (group.events.size > 2) {
                        val measurable =
                            measurables.find { (it.layoutId as? String) == "swipe_group_$groupIndex" }
                        measurable?.let {
                            val earliestEvent = getEarliestEvent(group.events)
                            val startMinutes =
                                earliestEvent.startTime.hour * 60 + earliestEvent.startTime.minute
                            val startOffsetMinutes = startMinutes - (startHour * 60)
                            val stackOffset = with(density) { 16.dp.toPx().toInt() }
                            val topOffsetPx = with(density) {
                                (startOffsetMinutes / 60f * CalendarUtils.HOUR_SLOT_HEIGHT).toPx()
                                    .toInt() - stackOffset
                            }.coerceAtLeast(0)
                            val placeable = it.measure(
                                constraints.copy(
                                    minWidth = 0, maxWidth = constraints.maxWidth, minHeight = 0
                                )
                            )
                            placeables.add(Triple(placeable, 0, topOffsetPx))
                        }
                    } else {
                        val visibleCount = group.visibleEvents.size
                        val columnWidth = constraints.maxWidth / visibleCount
                        group.visibleEvents.forEachIndexed { index, event ->
                            val measurable =
                                measurables.find { (it.layoutId as? String) == "event_${event.id}" }
                            measurable?.let {
                                val startMinutes =
                                    event.startTime.hour * 60 + event.startTime.minute
                                val startOffsetMinutes = startMinutes - (startHour * 60)
                                val topOffsetPx = with(density) {
                                    (startOffsetMinutes / 60f * CalendarUtils.HOUR_SLOT_HEIGHT).toPx()
                                        .toInt()
                                }
                                val leftOffset = index * columnWidth
                                val placeable = it.measure(
                                    constraints.copy(
                                        minWidth = 0, maxWidth = columnWidth - 4, minHeight = 0
                                    )
                                )
                                placeables.add(Triple(placeable, leftOffset, topOffsetPx))
                            }
                        }
                    }
                }
                layout(constraints.maxWidth, maxLayoutHeight) {
                    placeables.forEach { (placeable, x, y) -> placeable.place(x, y) }
                }
            })
    }
}

private fun calculateOverlappingGroups(events: List<CourseEvent>): List<OverlappingGroup> {
    val sortedEvents = events.sortedBy { it.startTime }
    val groups = mutableListOf<OverlappingGroup>()
    
    sortedEvents.forEach { event ->
        val overlappingGroup = groups.find { group ->
            group.events.any { groupEvent ->
                event.startTime < groupEvent.endTime && event.endTime > groupEvent.startTime
            }
        }

        if (overlappingGroup != null) {
            overlappingGroup.events.add(event)
        } else {
            groups.add(OverlappingGroup(mutableListOf(event)))
        }
    }
    
    groups.forEach { group -> group.events.sortBy { it.startTime } }
    return groups
}

private data class OverlappingGroup(val events: MutableList<CourseEvent>) {
    val visibleEvents: List<CourseEvent> get() = if (events.size > 2) listOf(events.first()) else events
}

private fun getEarliestEvent(events: List<CourseEvent>): CourseEvent =
    events.minByOrNull { it.startTime } ?: events.first()

@Composable
private fun CurrentTimeIndicator(primaryColor: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f, animationSpec = infiniteRepeatable(
            tween(
                CalendarUtils.PULSE_DURATION_MS, easing = FastOutSlowInEasing
            ), RepeatMode.Reverse
        ), label = "pulse_scale"
    )
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(CalendarUtils.TIME_COLUMN_WIDTH - 6.dp))
        Box(
            modifier = Modifier
                .size(CalendarUtils.CURRENT_TIME_DOT_SIZE)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(primaryColor)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(CalendarUtils.CURRENT_TIME_LINE_HEIGHT)
                .background(primaryColor)
        )
    }
}

@Composable
private fun DashedHourLine(grayColor: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        val dashWidth = 6.dp.toPx();
        val dashGap = 4.dp.toPx();
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = grayColor.copy(alpha = 0.25f),
                start = Offset(x, 0f),
                end = Offset((x + dashWidth).coerceAtMost(size.width), 0f),
                strokeWidth = 1.dp.toPx()
            )
            x += dashWidth + dashGap
        }
    }
}

@Composable
private fun TimelineEventCard(
    event: CourseEvent,
    height: Dp,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    stackDepth: Int = 0
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val eventStatus = remember(event) { getEventStatus(event) }
    val cardBackgroundColor = MaterialTheme.colorScheme.surface
    val durationText =
        remember(event) { CalendarUtils.formatDuration(event.startTime, event.endTime) }
    val cardAlpha = if (eventStatus == TimelineEventStatus.ENDED) 0.6f else 1f

    Box(modifier = modifier) {
        if (stackDepth > 0) {
            val stackOffset = 5.dp
            for (i in stackDepth downTo 1) {
                Surface(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = stackOffset * i, y = -(stackOffset * i)),
                    shape = RoundedCornerShape(CalendarUtils.EVENT_CARD_CORNER_RADIUS),
                    color = cardBackgroundColor,
                    border = BorderStroke(1.dp, grayColor.copy(alpha = 0.1f)),
                    shadowElevation = 1.dp
                ) {}
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer { alpha = cardAlpha },
            shape = RoundedCornerShape(CalendarUtils.EVENT_CARD_CORNER_RADIUS),
            color = cardBackgroundColor,
            shadowElevation = if (eventStatus == TimelineEventStatus.IN_PROGRESS) 4.dp else 1.dp,
            onClick = onClick
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                EventColorBar(color = eventColor, status = eventStatus)
                EventCardContent(
                    event = event,
                    height = height,
                    durationText = durationText,
                    eventStatus = eventStatus,
                    eventColor = eventColor,
                    textColor = textColor,
                    grayColor = grayColor
                )
            }
        }
    }
}

@Composable
private fun EventColorBar(color: Color, status: TimelineEventStatus) {
    val barColor = if (status == TimelineEventStatus.IN_PROGRESS) EventInProgressColor else color
    val infiniteTransition = rememberInfiniteTransition(label = "bar_pulse")
    val animatedAlpha by if (status == TimelineEventStatus.IN_PROGRESS) {
        infiniteTransition.animateFloat(
            initialValue = 0.7f, targetValue = 1f, animationSpec = infiniteRepeatable(
                tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse
            ), label = "bar_alpha"
        )
    } else remember { mutableFloatStateOf(1f) }
    Box(
        modifier = Modifier
            .width(CalendarUtils.COLOR_BAR_WIDTH)
            .fillMaxHeight()
            .clip(
                RoundedCornerShape(
                    topStart = CalendarUtils.EVENT_CARD_CORNER_RADIUS,
                    bottomStart = CalendarUtils.EVENT_CARD_CORNER_RADIUS
                )
            )
            .background(barColor.copy(alpha = animatedAlpha))
    )
}

@Composable
private fun EventCardContent(
    event: CourseEvent,
    height: Dp,
    durationText: String,
    eventStatus: TimelineEventStatus,
    eventColor: Color,
    textColor: Color,
    grayColor: Color
) {
    val showCompactContent = height < 80.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (showCompactContent) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (showCompactContent) 4.dp else 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = event.courseName,
                color = textColor,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            EventStatusIndicator(event = event, eventStatus = eventStatus, eventColor = eventColor)
        }
        if (showCompactContent) {
            EventInfoChip(
                icon = Icons.Outlined.Schedule,
                text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${
                    event.endTime.format(CalendarUtils.timeFormatter)
                }",
                color = grayColor
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EventInfoChip(
                    icon = Icons.Outlined.Schedule,
                    text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${
                        event.endTime.format(CalendarUtils.timeFormatter)
                    }",
                    color = grayColor
                )
                EventInfoChip(icon = Icons.Outlined.Timer, text = durationText, color = grayColor)
            }
            CalendarUtils.formatEventLocation(event.room, event.building)?.let {
                EventInfoChip(
                    icon = Icons.Outlined.LocationOn, text = it, color = grayColor
                )
            }
            event.professor?.let {
                EventInfoChip(
                    icon = Icons.Outlined.Person, text = it, color = grayColor
                )
            }
        }
    }
}

@Composable
private fun EventStatusIndicator(
    event: CourseEvent, eventStatus: TimelineEventStatus, eventColor: Color
) {
    when (eventStatus) {
        TimelineEventStatus.ENDED -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EventInProgressColor,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Outlined.Check,
                        stringResource(R.string.event_status_ended),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        TimelineEventStatus.IN_PROGRESS -> {
            val infiniteTransition = rememberInfiniteTransition(label = "in_progress")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f, targetValue = 1.2f, animationSpec = infiniteRepeatable(
                    tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse
                ), label = "scale"
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale), contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(EventInProgressColor)
                )
            }
        }

        TimelineEventStatus.UPCOMING -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = eventColor.copy(alpha = 0.15f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        getEventTypeIcon(event.eventType),
                        null,
                        tint = eventColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventInfoChip(icon: ImageVector, text: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getEventTypeIcon(eventType: EventType): ImageVector = when (eventType) {
    EventType.LECTURE -> Icons.Outlined.School
    EventType.LAB -> Icons.Outlined.Science
    EventType.EXAM -> Icons.AutoMirrored.Outlined.Assignment
    EventType.OTHER -> Icons.Outlined.Event
}

private enum class TimelineEventStatus { ENDED, IN_PROGRESS, UPCOMING }

private fun getEventStatus(event: CourseEvent): TimelineEventStatus {
    val now = LocalDateTime.now()
    return when {
        event.isCancelled -> TimelineEventStatus.ENDED
        now.isAfter(event.endTime) -> TimelineEventStatus.ENDED
        now.isAfter(event.startTime) && now.isBefore(event.endTime) -> TimelineEventStatus.IN_PROGRESS
        else -> TimelineEventStatus.UPCOMING
    }
}

@Composable
private fun TimelineLoadingState(primaryColor: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp), strokeWidth = 3.dp, color = primaryColor
        )
    }
}