package it.attendance100.mybicocca.ui.component.calendar.view

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig
import it.attendance100.mybicocca.ui.component.calendar.CalendarEmptyState
import it.attendance100.mybicocca.ui.component.calendar.EmptyStateType
import it.attendance100.mybicocca.ui.component.calendar.OverlapCalculator
import it.attendance100.mybicocca.ui.component.calendar.OverlapGroup
import it.attendance100.mybicocca.ui.component.calendar.CalendarLoadingState
import it.attendance100.mybicocca.ui.component.calendar.DashedHourLine
import it.attendance100.mybicocca.ui.component.calendar.animatedZoomLevel
import it.attendance100.mybicocca.ui.component.calendar.rememberPinchZoomState
import it.attendance100.mybicocca.ui.component.calendar.zoomableContainer
import it.attendance100.mybicocca.ui.component.calendar.dialog.OverlappingEventsBottomSheet
import kotlinx.coroutines.delay
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue

/**
 * Weekly calendar view with pinch-to-zoom support.
 * Displays events across 7 days with time-based positioning.
 */
@Composable
fun WeekGridView(
    displayedWeekStart: LocalDate,
    events: List<CalendarEvent>,
    isLoading: Boolean,
    hasActiveFilters: Boolean = false,
    searchQuery: String = "",
    grayColor: Color,
    primaryColor: Color,
    scrollState: ScrollState,
    onEventClick: (CalendarEvent) -> Unit,
    onSwipeWeek: (Int) -> Unit,
    onDayZoom: (LocalDate) -> Unit
) {
    val daysOfWeek = remember(displayedWeekStart) {
        (0..6).map { displayedWeekStart.plusDays(it.toLong()) }
    }

    val weekEvents = remember(events, daysOfWeek) {
        events.filter { event ->
            val eventDate = event.date
            !eventDate.isBefore(daysOfWeek.first()) && !eventDate.isAfter(daysOfWeek.last())
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CalendarLoadingState()
            events.isEmpty() && (searchQuery.isNotEmpty() || hasActiveFilters) -> {
                CalendarEmptyState(
                    type = EmptyStateType.SEARCH,
                    modifier = Modifier.fillMaxSize()
                )
            }
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

@Composable
private fun WeekGridContentWithZoom(
    daysOfWeek: List<LocalDate>,
    events: List<CalendarEvent>,
    grayColor: Color,
    primaryColor: Color,
    scrollState: ScrollState,
    onEventClick: (CalendarEvent) -> Unit,
    onSwipeWeek: (Int) -> Unit,
    onDayZoom: (LocalDate) -> Unit
) {
    val density = LocalDensity.current
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var selectedOverlapGroup by remember { mutableStateOf<OverlapGroup?>(null) }

    val totalHours = CalendarConfig.TimeSettings.TOTAL_HOURS
    val baseContentHeight = CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT * totalHours

    val zoomState = rememberPinchZoomState(baseContentHeight = baseContentHeight)
    val zoomLevel = zoomState.animatedZoomLevel()
    val isCompactMode = zoomState.isCompactMode

    // Update current time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }

    // Auto-scroll to current time position when view is shown
    LaunchedEffect(zoomState.containerHeight, zoomLevel) {
        if (zoomState.containerHeight > 0 && !isCompactMode) {
            val now = LocalTime.now()
            if (now.hour >= CalendarConfig.TimeSettings.START_HOUR && now.hour < CalendarConfig.TimeSettings.END_HOUR) {
                val minutesSinceStart = (now.hour - CalendarConfig.TimeSettings.START_HOUR) * 60 + now.minute
                val scaledHourHeight = with(density) { (CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT.value * zoomLevel).dp.toPx() }
                val targetScrollPosition = (minutesSinceStart / 60f * scaledHourHeight).toInt()
                val centeredPosition = (targetScrollPosition - zoomState.containerHeight / 2).toInt().coerceAtLeast(0)
                scrollState.scrollTo(centeredPosition)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zoomableContainer(zoomState)
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
                val compactDensity = LocalDensity.current
                val heightModifier = if (zoomState.containerHeight > 0) {
                    Modifier.height(with(compactDensity) { zoomState.containerHeight.toDp() })
                } else {
                    Modifier.fillMaxSize()
                }

                CompactWeekView(
                    daysOfWeek = daysOfWeek,
                    events = events,
                    onEventClick = onEventClick,
                    primaryColor = primaryColor,
                    modifier = heightModifier
                )
            } else {
                Box {
                    WeekGridBackground(
                        startHour = CalendarConfig.TimeSettings.START_HOUR,
                        endHour = CalendarConfig.TimeSettings.END_HOUR,
                        grayColor = grayColor,
                        zoomLevel = zoomLevel
                    )

                    WeekEventsOverlay(
                        daysOfWeek = daysOfWeek,
                        events = events,
                        startHour = CalendarConfig.TimeSettings.START_HOUR,
                        onEventClick = onEventClick,
                        onGroupClick = { group -> selectedOverlapGroup = group },
                        primaryColor = primaryColor,
                        zoomLevel = zoomLevel
                    )

                    CurrentTimeWeekIndicator(
                        daysOfWeek = daysOfWeek,
                        currentTime = currentTime,
                        startHour = CalendarConfig.TimeSettings.START_HOUR,
                        primaryColor = primaryColor,
                        zoomLevel = zoomLevel
                    )
                }
            }
        }
    }

    // Overlapping events modal
    selectedOverlapGroup?.let { group ->
        OverlappingEventsBottomSheet(
            group = group,
            onDismiss = { selectedOverlapGroup = null },
            primaryColor = primaryColor
        )
    }
}

// COMPACT WEEK VIEW (zoomed out) - Shows ALL events individually

@Composable
private fun CompactWeekView(
    daysOfWeek: List<LocalDate>,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val eventsByDay = remember(events, daysOfWeek) {
        daysOfWeek.associateWith { date ->
            events.filter { it.date == date }.sortedBy { it.startDateTime }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(start = CalendarConfig.Dimensions.TIME_COLUMN_WIDTH)
    ) {
        val availableHeight = maxHeight
        val eventSpacing = 2.dp
        val verticalPadding = 8.dp
        val maxComfortableEventHeight = 120.dp

        val maxEventsCount = eventsByDay.values.maxOfOrNull { it.size } ?: 0
        val totalSpacingForMax = if (maxEventsCount > 0) {
            (eventSpacing * (maxEventsCount - 1)) + verticalPadding
        } else 0.dp

        val totalNeededHeightForMax = (maxComfortableEventHeight * maxEventsCount) + totalSpacingForMax
        val useCompactHeight = totalNeededHeightForMax > availableHeight

        val unifiedEventHeight = if (useCompactHeight && maxEventsCount > 0) {
            (availableHeight - totalSpacingForMax) / maxEventsCount
        } else {
            maxComfortableEventHeight
        }

        Row(modifier = Modifier.fillMaxSize()) {
            daysOfWeek.forEachIndexed { dayIndex, date ->
                val dayEvents = eventsByDay[date] ?: emptyList()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .then(if (dayIndex < 6) Modifier.padding(end = 1.dp) else Modifier)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(eventSpacing)
                ) {
                    dayEvents.forEach { event ->
                        CompactEventCard(
                            event = event,
                            height = unifiedEventHeight,
                            primaryColor = primaryColor,
                            onClick = { onEventClick(event) }
                        )
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
    val scaledHourSlotHeight = (CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT.value * zoomLevel).dp
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
                .width(CalendarConfig.Dimensions.TIME_COLUMN_WIDTH)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = String.format(Locale.ROOT, "%02d", hour),
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
            DashedHourLine(
                color = grayColor.copy(alpha = 0.15f),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

// EVENTS OVERLAY - Uses merged blocks for overlapping events

@Composable
private fun WeekEventsOverlay(
    daysOfWeek: List<LocalDate>,
    events: List<CalendarEvent>,
    startHour: Int,
    onEventClick: (CalendarEvent) -> Unit,
    onGroupClick: (OverlapGroup) -> Unit,
    primaryColor: Color,
    zoomLevel: Float
) {
    val scaledHourSlotHeight = (CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT.value * zoomLevel).dp
    val density = LocalDensity.current

    val groupsByDay = remember(events, daysOfWeek, startHour) {
        daysOfWeek.mapIndexed { dayIndex, date ->
            val dayEvents = events.filter { it.date == date }
            dayIndex to OverlapCalculator.calculateOverlapGroups(dayEvents, startHour)
        }.toMap()
    }

    Layout(
        content = {
            daysOfWeek.forEachIndexed { dayIndex, _ ->
                val dayGroups = groupsByDay[dayIndex] ?: emptyList()

                dayGroups.forEachIndexed { groupIndex, group ->
                    val groupKey = "${dayIndex}_$groupIndex"

                    if (group.events.size == 1) {
                        val event = group.events.first()
                        val durationMinutes = ChronoUnit.MINUTES.between(event.startDateTime, event.endDateTime)
                        val visualDuration = maxOf(durationMinutes, 15L)
                        val eventHeightDp = (visualDuration / 60f * scaledHourSlotHeight.value).dp

                        WeekEventCard(
                            event = event,
                            height = eventHeightDp,
                            onClick = { onEventClick(event) },
                            primaryColor = primaryColor,
                            modifier = Modifier.layoutId("single_${dayIndex}_${event.id}")
                        )
                    } else {
                        val groupDurationMinutes = group.groupEndMinutes - group.groupStartMinutes
                        val visualDuration = maxOf(groupDurationMinutes, 15)
                        val blockHeightDp = (visualDuration / 60f * scaledHourSlotHeight.value).dp

                        MergedEventBlock(
                            group = group,
                            height = blockHeightDp,
                            onClick = { onGroupClick(group) },
                            primaryColor = primaryColor,
                            modifier = Modifier.layoutId("merged_$groupKey")
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalendarConfig.Dimensions.TIME_COLUMN_WIDTH)
    ) { measurables, constraints ->

        val totalDays = CalendarConfig.Dimensions.WEEK_DAYS
        val dayWidthPx = constraints.maxWidth / totalDays.toFloat()
        val totalHours = CalendarConfig.TimeSettings.TOTAL_HOURS
        val hourHeightPx = scaledHourSlotHeight.toPx()
        val maxLayoutHeight = (totalHours * hourHeightPx).toInt()

        val dayPaddingPx = with(density) { 2.dp.toPx() }

        val placeables = mutableListOf<PlaceableInfo>()

        measurables.forEach { measurable ->
            val layoutId = measurable.layoutId as String
            val parts = layoutId.split("_")

            when (parts[0]) {
                "single" -> {
                    val dayIndex = parts[1].toInt()
                    val eventId = parts[2]
                    val event = events.first { it.id == eventId }

                    val eventStartMinutes = (event.startDateTime.hour - startHour) * 60 + event.startDateTime.minute
                    val durationMinutes = ChronoUnit.MINUTES.between(event.startDateTime, event.endDateTime).toInt()
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

                "merged" -> {
                    val dayIndex = parts[1].toInt()
                    val groupIndex = parts[2].toInt()

                    val dayGroups = groupsByDay[dayIndex] ?: emptyList()
                    val group = dayGroups.getOrNull(groupIndex) ?: return@forEach

                    val groupDurationMinutes = group.groupEndMinutes - group.groupStartMinutes
                    val visualDuration = maxOf(groupDurationMinutes, 15)

                    val blockWidth = (dayWidthPx - dayPaddingPx * 2).toInt().coerceAtLeast(1)
                    val blockHeight = (visualDuration / 60f * hourHeightPx).toInt().coerceAtLeast(1)

                    val placeable = measurable.measure(
                        constraints.copy(
                            minWidth = blockWidth,
                            maxWidth = blockWidth,
                            minHeight = blockHeight,
                            maxHeight = blockHeight
                        )
                    )

                    val xPos = (dayIndex * dayWidthPx + dayPaddingPx).toInt()
                    val yPos = (group.groupStartMinutes / 60f * hourHeightPx).toInt()

                    placeables.add(PlaceableInfo(placeable, xPos, yPos, zIndex = 1f))
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
    if (currentTime.hour < startHour || currentTime.hour >= CalendarConfig.TimeSettings.END_HOUR) return

    val scaledHourSlotHeight = (CalendarConfig.Dimensions.HOUR_SLOT_HEIGHT.value * zoomLevel).dp
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
            .padding(start = CalendarConfig.Dimensions.TIME_COLUMN_WIDTH)
    ) { measurables, constraints ->
        val lineMeasurable = measurables.first { it.layoutId == "line" }
        val dotMeasurable = measurables.first { it.layoutId == "dot" }

        val linePlaceable = lineMeasurable.measure(constraints)
        val dotPlaceable = dotMeasurable.measure(
            constraints.copy(minWidth = 0, minHeight = 0)
        )

        val dayWidthPx = constraints.maxWidth / CalendarConfig.Dimensions.WEEK_DAYS.toFloat()
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
