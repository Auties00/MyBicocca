package it.attendance100.mybicocca.ui.component.calendar.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.ui.component.calendar.view.SwipeableCardStack
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig
import it.attendance100.mybicocca.ui.component.calendar.EmptyStateType
import it.attendance100.mybicocca.ui.component.calendar.EventStatus
import it.attendance100.mybicocca.ui.component.calendar.resolveStatus
import it.attendance100.mybicocca.ui.screen.calendar.LocationFilter
import it.attendance100.mybicocca.ui.component.calendar.OverlapCalculator
import it.attendance100.mybicocca.ui.screen.calendar.TimeRange
import it.attendance100.mybicocca.ui.component.calendar.*
import it.attendance100.mybicocca.ui.component.calendar.EventCard
import it.attendance100.mybicocca.ui.component.calendar.EventCardVariant
import kotlinx.coroutines.delay
import java.time.*
import java.util.Locale

@Composable
fun DayTimelineView(
    dayPagerState: PagerState,
    referenceDate: LocalDate,
    eventsForCurrentMonth: List<CalendarEvent>,
    searchQuery: String,
    activeFilters: Set<EventType>,
    timeRange: TimeRange?,
    locationFilter: LocationFilter,
    isLoading: Boolean,
    hasActiveFilters: Boolean,
    onEventClick: (CalendarEvent) -> Unit,
    scrollState: ScrollState
) {
    val colorScheme = MaterialTheme.colorScheme

    HorizontalPager(
        state = dayPagerState,
        modifier = Modifier.fillMaxSize(),
        key = { page -> "$page-${eventsForCurrentMonth.size}" }
    ) { page ->
        val dayOffset = page - CalendarConfig.Pager.INITIAL_PAGE_OFFSET
        val pageDate = referenceDate.plusDays(dayOffset.toLong())

        val pageEvents = eventsForCurrentMonth
            .filter { it.date == pageDate }
            .let {
                filterEvents(
                    it,
                    searchQuery,
                    activeFilters,
                    timeRange,
                    locationFilter
                )
            }

        when {
            isLoading && pageEvents.isEmpty() -> {
                SkeletonLoadingList()
            }
            pageEvents.isEmpty() -> {
                CalendarEmptyState(
                    type = if (searchQuery.isNotEmpty() || hasActiveFilters)
                        EmptyStateType.SEARCH else EmptyStateType.DAY,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                DayTimelineContent(
                    events = pageEvents,
                    selectedDate = pageDate,
                    isLoading = false,
                    onEventClick = onEventClick,
                    scrollState = scrollState
                )
            }
        }
    }
}

@Composable
private fun DayTimelineContent(
    events: List<CalendarEvent>,
    selectedDate: LocalDate,
    isLoading: Boolean,
    onEventClick: (CalendarEvent) -> Unit,
    scrollState: ScrollState
) {
    val isToday = selectedDate == LocalDate.now()

    Column(modifier = Modifier.fillMaxSize()) {
        DayTimelineHeader(eventsCount = events.size)
        when {
            isLoading -> CalendarLoadingState()
            else -> TimelineContentWithZoom(
                events = events,
                isToday = isToday,
                onEventClick = onEventClick,
                scrollState = scrollState
            )
        }
    }
}

@Composable
private fun DayTimelineHeader(eventsCount: Int) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.calendar_events),
            color = colorScheme.primary,
            style = MaterialTheme.typography.titleSmall
        )
        if (eventsCount > 0) {
            Badge(
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer
            ) {
                Text(
                    text = eventsCount.toString(),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun TimelineContentWithZoom(
    events: List<CalendarEvent>,
    isToday: Boolean,
    onEventClick: (CalendarEvent) -> Unit,
    scrollState: ScrollState
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    val currentTime by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            val nextMinute = value.plusMinutes(1).withSecond(0).withNano(0)
            val delayMillis = Duration.between(value, nextMinute).toMillis()
            delay(delayMillis + 50)
        }
    }

    val totalHours = CalendarConfig.TimeSettings.TOTAL_HOURS
    val baseContentHeight = CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT * totalHours

    val zoomState = rememberPinchZoomState(baseContentHeight = baseContentHeight)
    val zoomLevel = zoomState.animatedZoomLevel()
    val isCompactMode = zoomState.isCompactMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zoomableContainer(zoomState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (isCompactMode) {
                val heightModifier = if (zoomState.containerHeight > 0) {
                    Modifier.heightIn(min = with(density) { zoomState.containerHeight.toDp() })
                } else {
                    Modifier.fillMaxSize()
                }

                CompactDayView(
                    events = events,
                    onEventClick = onEventClick,
                    modifier = heightModifier
                )
            } else {
                Box {
                    TimelineBackground(
                        startHour = CalendarConfig.TimeSettings.START_HOUR,
                        endHour = CalendarConfig.TimeSettings.END_HOUR,
                        zoomLevel = zoomLevel
                    )
                    TimelineEventsOverlay(
                        events = events,
                        startHour = CalendarConfig.TimeSettings.START_HOUR,
                        onEventClick = onEventClick,
                        zoomLevel = zoomLevel
                    )

                    if (isToday) {
                        val currentHour = currentTime.hour
                        if (currentHour >= CalendarConfig.TimeSettings.START_HOUR && currentHour <= CalendarConfig.TimeSettings.END_HOUR) {
                            val totalMinutes =
                                (currentHour - CalendarConfig.TimeSettings.START_HOUR) * 60 + currentTime.minute
                            val scaledHourHeight = CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT.value * zoomLevel
                            val offsetY = (totalMinutes / 60f * scaledHourHeight).dp
                            CurrentTimeIndicator(
                                modifier = Modifier.offset(y = offsetY)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDayView(
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedEvents = remember(events) { events.sortedBy { it.startDateTime } }
    val colorScheme = MaterialTheme.colorScheme

    if (sortedEvents.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.EventBusy,
                    contentDescription = null,
                    tint = colorScheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = stringResource(R.string.calendar_no_events),
                    color = colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedEvents.forEach { event ->
                EventCard(
                    event = event,
                    variant = EventCardVariant.TIMELINE,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
}


@Composable
private fun TimelineBackground(
    startHour: Int,
    endHour: Int,
    zoomLevel: Float = 1f
) {
    val colorScheme = MaterialTheme.colorScheme
    val scaledHourSlotHeight = (CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT.value * zoomLevel).dp
    val totalHeight = ((endHour - startHour) * scaledHourSlotHeight.value).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        for (hour in startHour until endHour) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaledHourSlotHeight)
            ) {
                Box(
                    modifier = Modifier.Companion.width(CalendarConfig.Dimensions.TIME_COLUMN_WIDTH),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = String.format(Locale.ROOT, "%02d", hour),
                        color = colorScheme.outline.copy(alpha = 0.6f),
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
                    DashedHourLine(color = colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun TimelineEventsOverlay(
    events: List<CalendarEvent>,
    startHour: Int,
    onEventClick: (CalendarEvent) -> Unit,
    zoomLevel: Float = 1f
) {
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme
    val selectedEventsByGroup = remember { mutableStateMapOf<Int, String>() }
    val overlappingGroups = remember(events, startHour) {
        OverlapCalculator.calculateOverlapGroups(events, startHour)
    }

    val scaledHourSlotHeight = CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT.value * zoomLevel

    LaunchedEffect(events) {
        overlappingGroups.forEachIndexed { groupIndex, group ->
            if (!selectedEventsByGroup.containsKey(groupIndex)) {
                selectedEventsByGroup[groupIndex] = OverlapCalculator.getEarliestEvent(group.events).id
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Layout(
            content = {
                overlappingGroups.forEachIndexed { groupIndex, group ->
                    if (!selectedEventsByGroup.containsKey(groupIndex)) {
                        selectedEventsByGroup[groupIndex] = OverlapCalculator.getEarliestEvent(group.events).id
                    }
                    val selectedEventId =
                        selectedEventsByGroup[groupIndex] ?: group.events.first().id

                    if (group.events.size > 2) {
                        val selectedEvent =
                            group.events.find { it.id == selectedEventId } ?: group.events.first()
                        val durationMinutes = Duration.between(
                            selectedEvent.startDateTime, selectedEvent.endDateTime
                        ).toMinutes()
                        val cardHeight = (durationMinutes / 60f * scaledHourSlotHeight).dp

                        SwipeableCardStack(
                            events = group.events,
                            currentEventId = selectedEventId,
                            onEventSelected = { eventId ->
                                selectedEventsByGroup[groupIndex] = eventId
                            },
                            onEventClick = onEventClick,
                            cardHeight = cardHeight,
                            modifier = Modifier.layoutId("swipe_group_$groupIndex")
                        )
                    } else {
                        group.visibleEvents.forEachIndexed { index, event ->
                            val durationMinutes =
                                Duration.between(event.startDateTime, event.endDateTime)
                                    .toMinutes()
                            val eventHeightDp = (durationMinutes / 60f * scaledHourSlotHeight).dp
                            TimelineEventCard(
                                event = event,
                                height = eventHeightDp,
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
                .padding(start = CalendarConfig.Dimensions.TIME_COLUMN_WIDTH, end = 16.dp),
            measurePolicy = { measurables, constraints ->
                val totalHours = CalendarConfig.TimeSettings.TOTAL_HOURS
                val scaledHourHeightPx = with(density) { scaledHourSlotHeight.dp.toPx() }
                val maxLayoutHeight = (totalHours * scaledHourHeightPx).toInt()
                val placeables = mutableListOf<Triple<Placeable, Int, Int>>()

                overlappingGroups.forEachIndexed { groupIndex, group ->
                    if (group.events.size > 2) {
                        val measurable =
                            measurables.find { (it.layoutId as? String) == "swipe_group_$groupIndex" }
                        measurable?.let {
                            val earliestEvent = OverlapCalculator.getEarliestEvent(group.events)
                            val startMinutes =
                                earliestEvent.startDateTime.hour * 60 + earliestEvent.startDateTime.minute
                            val startOffsetMinutes = startMinutes - (startHour * 60)
                            val stackOffset = with(density) { 16.dp.toPx().toInt() }
                            val topOffsetPx = ((startOffsetMinutes / 60f) * scaledHourHeightPx)
                                .toInt() - stackOffset
                                .coerceAtLeast(0)
                            val placeable = it.measure(
                                constraints.copy(
                                    minWidth = 0, maxWidth = constraints.maxWidth, minHeight = 0
                                )
                            )
                            placeables.add(Triple(placeable, 0, topOffsetPx.coerceAtLeast(0)))
                        }
                    } else {
                        val visibleCount = group.visibleEvents.size
                        val columnWidth = constraints.maxWidth / visibleCount
                        group.visibleEvents.forEachIndexed { index, event ->
                            val measurable =
                                measurables.find { (it.layoutId as? String) == "event_${event.id}" }
                            measurable?.let {
                                val startMinutes =
                                    event.startDateTime.hour * 60 + event.startDateTime.minute
                                val startOffsetMinutes = startMinutes - (startHour * 60)
                                val topOffsetPx = ((startOffsetMinutes / 60f) * scaledHourHeightPx).toInt()
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

@Composable
private fun CurrentTimeIndicator(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.3f, animationSpec = infiniteRepeatable(
            tween(
                CalendarConfig.Animation.PULSE_DURATION_MS, easing = FastOutSlowInEasing
            ), RepeatMode.Reverse
        ), label = "pulse_scale"
    )
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.Companion.width(CalendarConfig.Dimensions.TIME_COLUMN_WIDTH - 6.dp))
        Box(
            modifier = Modifier.Companion
                .size(CalendarConfig.Dimensions.CURRENT_TIME_DOT_SIZE)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(CalendarConfig.Dimensions.CURRENT_TIME_LINE_HEIGHT)
                .background(colorScheme.primary)
        )
    }
}

@Composable
private fun TimelineEventCard(
    event: CalendarEvent,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    stackDepth: Int = 0
) {
    val colorScheme = MaterialTheme.colorScheme
    val eventColor = CalendarUtils.getEventColor(event.eventType, colorScheme.primary)
    val eventStatus = remember(event) { event.resolveStatus() }
    val durationText = remember(event) { CalendarUtils.formatDuration(event.startDateTime, event.endDateTime) }
    val isEnded = eventStatus == EventStatus.ENDED || eventStatus == EventStatus.CANCELLED

    Box(modifier = modifier) {
        if (stackDepth > 0) {
            val stackOffset = 5.dp
            for (i in stackDepth downTo 1) {
                Card(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = stackOffset * i, y = -(stackOffset * i)),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {}
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = if (isEnded)
                    colorScheme.surfaceContainerLow
                else
                    colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (eventStatus == EventStatus.IN_PROGRESS) 4.dp else 1.dp
            ),
            onClick = onClick
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                EventColorBar(
                    color = eventColor,
                    status = eventStatus,
                    width = CalendarConfig.Dimensions.COLOR_BAR_WIDTH,
                    cornerRadius = 12.dp // Match M3 medium shape
                )
                EventCardContent(
                    event = event,
                    height = height,
                    durationText = durationText,
                    eventStatus = eventStatus,
                    eventColor = eventColor,
                    isEnded = isEnded
                )
            }
        }
    }
}

@Composable
private fun EventCardContent(
    event: CalendarEvent,
    height: Dp,
    durationText: String,
    eventStatus: EventStatus,
    eventColor: Color,
    isEnded: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    val showCompactContent = height < CalendarConfig.Heights.COMPACT_THRESHOLD
    val contentAlpha = if (isEnded) 0.6f else 1f

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
                text = event.title,
                color = colorScheme.onSurface.copy(alpha = contentAlpha),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            EventStatusIndicator(event = event, eventStatus = eventStatus, eventColor = eventColor)
        }

        val outlineColor = colorScheme.outline.copy(alpha = contentAlpha)

        if (showCompactContent) {
            EventInfoChip(
                icon = Icons.Outlined.Schedule,
                text = "${event.startDateTime.format(CalendarUtils.timeFormatter)} - ${
                    event.endDateTime.format(CalendarUtils.timeFormatter)
                }",
                color = outlineColor
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EventInfoChip(
                    icon = Icons.Outlined.Schedule,
                    text = "${event.startDateTime.format(CalendarUtils.timeFormatter)} - ${
                        event.endDateTime.format(CalendarUtils.timeFormatter)
                    }",
                    color = outlineColor
                )
                EventInfoChip(icon = Icons.Outlined.Timer, text = durationText, color = outlineColor)
            }
            CalendarUtils.formatEventLocation(event.location, event.buildingCode)?.let {
                EventInfoChip(
                    icon = Icons.Outlined.LocationOn, text = it, color = outlineColor
                )
            }
            event.teacherName?.let {
                EventInfoChip(
                    icon = Icons.Outlined.Person, text = it, color = outlineColor
                )
            }
        }
    }
}
