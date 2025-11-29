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
import it.attendance100.mybicocca.components.OverlappingEventsDialog
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.delay
import java.time.*


// CONSTANTS

private object DayTimelineConstants {
    val HOUR_SLOT_HEIGHT = 80.dp
    val TIME_COLUMN_WIDTH = 68.dp
    val CURRENT_TIME_DOT_SIZE = 12.dp
    val CURRENT_TIME_LINE_HEIGHT = 2.dp
    val EVENT_CARD_CORNER_RADIUS = 16.dp
    val COLOR_BAR_WIDTH = 4.dp

    const val START_HOUR = 7
    const val END_HOUR = 22
    const val PULSE_DURATION_MS = 2000
}


// OVERLAPPING EVENTS INDICATOR

@Composable
private fun OverlappingEventsIndicator(
    count: Int,
    primaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Indicatore sottile e compatto posizionato in alto
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = primaryColor.copy(alpha = 0.9f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "+$count",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


// MAIN COMPONENT - DAY TIMELINE VIEW

@Composable
fun DayTimelineView(
    events: List<CourseEvent>,
    selectedDate: LocalDate,
    isLoading: Boolean,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit
) {
    val isToday = selectedDate == LocalDate.now()

    Column(modifier = Modifier.fillMaxSize()) {
        DayTimelineHeader(
            eventsCount = events.size,
            primaryColor = primaryColor
        )

        when {
            isLoading -> {
                TimelineLoadingState(primaryColor = primaryColor)
            }
            else -> {
                TimelineContent(
                    events = events,
                    isToday = isToday,
                    textColor = textColor,
                    grayColor = grayColor,
                    primaryColor = primaryColor,
                    onEventClick = onEventClick
                )
            }
        }
    }
}


// HEADER

@Composable
private fun DayTimelineHeader(
    eventsCount: Int,
    primaryColor: Color
) {
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        if (eventsCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = primaryColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = eventsCount.toString(),
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}


// TIMELINE CONTENT

@Composable
private fun TimelineContent(
    events: List<CourseEvent>,
    isToday: Boolean,
    textColor: Color,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit
) {
    val scrollState = rememberScrollState()
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Griglia di sfondo con orari
        TimelineBackground(
            startHour = DayTimelineConstants.START_HOUR,
            endHour = DayTimelineConstants.END_HOUR,
            grayColor = grayColor
        )

        // Eventi posizionati sopra la griglia
        TimelineEventsOverlay(
            events = events,
            startHour = DayTimelineConstants.START_HOUR,
            textColor = textColor,
            grayColor = grayColor,
            primaryColor = primaryColor,
            onEventClick = onEventClick
        )

        // Indicatore orario corrente
        if (isToday) {
            val currentHour = currentTime.hour
            if (currentHour >= DayTimelineConstants.START_HOUR && currentHour <= DayTimelineConstants.END_HOUR) {
                val totalMinutes = (currentHour - DayTimelineConstants.START_HOUR) * 60 + currentTime.minute
                val offsetY = (totalMinutes / 60f * DayTimelineConstants.HOUR_SLOT_HEIGHT.value).dp

                CurrentTimeIndicator(
                    primaryColor = primaryColor,
                    modifier = Modifier.offset(y = offsetY)
                )
            }
        }
    }
}


// TIMELINE BACKGROUND

@Composable
private fun TimelineBackground(
    startHour: Int,
    endHour: Int,
    grayColor: Color
) {
    val totalHeight = ((endHour - startHour) * DayTimelineConstants.HOUR_SLOT_HEIGHT.value).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        for (hour in startHour until endHour) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DayTimelineConstants.HOUR_SLOT_HEIGHT)
            ) {
                Box(
                    modifier = Modifier
                        .width(DayTimelineConstants.TIME_COLUMN_WIDTH),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = String.format("%02d", hour),
                        color = grayColor.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
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


// EVENTS OVERLAY

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
    var overlappingEventsToShow by remember { mutableStateOf<List<CourseEvent>?>(null) }
    // Mappa gruppo -> evento selezionato (per default il primo)
    val selectedEventsByGroup = remember { mutableStateMapOf<Int, Long>() }
    
    // Inizializza eventi selezionati per ogni gruppo appena cambiano gli eventi
    LaunchedEffect(events) {
        val overlappingGroups = calculateOverlappingGroups(events)
        overlappingGroups.forEachIndexed { groupIndex, group ->
            if (!selectedEventsByGroup.containsKey(groupIndex) && group.events.size > 2) {
                // Inizializza con l'evento che inizia prima
                selectedEventsByGroup[groupIndex] = getEarliestEvent(group.events).id
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Layout(
            content = {
                // Calcola gruppi prima di creare i composable
                val overlappingGroups = calculateOverlappingGroups(events)
                
                overlappingGroups.forEachIndexed { groupIndex, group ->
                    // Inizializza evento selezionato per questo gruppo se non esiste
                    // Usa l'evento che inizia prima
                    if (!selectedEventsByGroup.containsKey(groupIndex)) {
                        selectedEventsByGroup[groupIndex] = getEarliestEvent(group.events).id
                    }
                    
                    val selectedEventId = selectedEventsByGroup[groupIndex]
                    
                    // Filtra eventi da mostrare: se >2 sovrapposizioni, mostra solo quello selezionato
                    val eventsToDisplay = if (group.events.size > 2) {
                        // Trova l'evento selezionato, o usa quello che inizia prima se non trovato
                        val selectedEvent = group.events.find { it.id == selectedEventId }
                        if (selectedEvent != null) {
                            listOf(selectedEvent)
                        } else {
                            listOf(getEarliestEvent(group.events))
                        }
                    } else {
                        group.visibleEvents
                    }
                    
                    eventsToDisplay.forEachIndexed { index, event ->
                        val durationMinutes = java.time.Duration.between(
                            event.startTime,
                            event.endTime
                        ).toMinutes()
                        val eventHeightDp = (durationMinutes / 60f * DayTimelineConstants.HOUR_SLOT_HEIGHT.value).dp

                        TimelineEventCard(
                            event = event,
                            height = eventHeightDp,
                            textColor = textColor,
                            grayColor = grayColor,
                            primaryColor = primaryColor,
                            onClick = { onEventClick(event) },
                            modifier = Modifier.layoutId("event_${event.id}"),
                            stackDepth = if (group.hasHiddenEvents) minOf(group.hiddenCount, 2) else 0
                        )
                    }
                    
                    // Indicatore per eventi nascosti (posizionato in alto)
                    if (group.hasHiddenEvents) {
                        // Usa l'evento selezionato per posizionare l'indicatore
                        val selectedEvent = group.events.find { it.id == selectedEventId } 
                            ?: group.events.first()
                        OverlappingEventsIndicator(
                            count = group.hiddenCount,
                            primaryColor = primaryColor,
                            onClick = { 
                                overlappingEventsToShow = group.events
                            },
                            modifier = Modifier.layoutId("indicator_${groupIndex}_${selectedEvent.id}")
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = DayTimelineConstants.TIME_COLUMN_WIDTH, end = 16.dp),
            measurePolicy = { measurables, constraints ->
                val totalHours = DayTimelineConstants.END_HOUR - DayTimelineConstants.START_HOUR
                val maxLayoutHeight = with(density) {
                    (totalHours * DayTimelineConstants.HOUR_SLOT_HEIGHT).toPx().toInt()
                }

                val overlappingGroups = calculateOverlappingGroups(events)
                val placeables = mutableListOf<Triple<Placeable, Int, Int>>()

                overlappingGroups.forEachIndexed { groupIndex, group ->
                    // Calcola eventi da visualizzare (stesso filtro del content)
                    val selectedEventId = selectedEventsByGroup[groupIndex]
                    val eventsToDisplay = if (group.events.size > 2 && selectedEventId != null) {
                        val selectedEvent = group.events.find { it.id == selectedEventId }
                        if (selectedEvent != null) {
                            listOf(selectedEvent)
                        } else {
                            listOf(getEarliestEvent(group.events))
                        }
                    } else {
                        group.visibleEvents
                    }
                    
                    val visibleCount = eventsToDisplay.size
                    val availableWidth = constraints.maxWidth
                    val columnWidth = availableWidth / visibleCount

                    // Posiziona eventi visibili
                    eventsToDisplay.forEachIndexed { index, event ->
                        val measurable = measurables.find { 
                            (it.layoutId as? String) == "event_${event.id}" 
                        }
                        
                        measurable?.let {
                            val startMinutes = event.startTime.hour * 60 + event.startTime.minute
                            val startOffsetMinutes = startMinutes - (startHour * 60)
                            val topOffsetPx = with(density) {
                                (startOffsetMinutes / 60f * DayTimelineConstants.HOUR_SLOT_HEIGHT).toPx().toInt()
                            }

                            val leftOffset = index * columnWidth

                            val placeable = it.measure(
                                constraints.copy(
                                    minWidth = 0,
                                    maxWidth = columnWidth - 4,
                                    minHeight = 0
                                )
                            )

                            placeables.add(Triple(placeable, leftOffset, topOffsetPx))
                        }
                    }

                    // Posiziona indicatore se presente (in alto a destra sulla card)
                    if (group.hasHiddenEvents) {
                        // Trova l'evento selezionato per posizionare l'indicatore
                        val selectedEvent = eventsToDisplay.firstOrNull() ?: group.events.first()
                        val indicatorMeasurable = measurables.find { 
                            (it.layoutId as? String) == "indicator_${groupIndex}_${selectedEvent.id}" 
                        }
                        
                        indicatorMeasurable?.let {
                            val startMinutes = selectedEvent.startTime.hour * 60 + selectedEvent.startTime.minute
                            val startOffsetMinutes = startMinutes - (startHour * 60)
                            
                            val placeable = it.measure(constraints.copy(minWidth = 0, minHeight = 0))
                            
                            // Posiziona in alto, con piccolo offset dall'inizio dell'evento
                            val topOffsetPx = with(density) {
                                (startOffsetMinutes / 60f * DayTimelineConstants.HOUR_SLOT_HEIGHT).toPx().toInt() + 4.dp.toPx().toInt()
                            }

                            // Posiziona in alto a destra sulla card
                            val leftOffset = availableWidth - placeable.width - with(density) { 8.dp.toPx().toInt() }

                            placeables.add(Triple(placeable, leftOffset, topOffsetPx))
                        }
                    }
                }

                layout(constraints.maxWidth, maxLayoutHeight) {
                    placeables.forEach { (placeable, x, y) ->
                        placeable.place(x, y)
                    }
                }
            }
        )
    }

    // Dialog per eventi sovrapposti
    overlappingEventsToShow?.let { overlappingEvents ->
        // Calcola gruppi per trovare l'indice corretto
        val overlappingGroups = calculateOverlappingGroups(events)
        val groupIndex = overlappingGroups.indexOfFirst { group ->
            group.events.any { groupEvent ->
                overlappingEvents.any { it.id == groupEvent.id }
            }
        }
        
        // Usa gli eventi ordinati dal gruppo
        val orderedEvents = if (groupIndex >= 0) overlappingGroups[groupIndex].events else overlappingEvents
        
        // Ottieni l'evento selezionato (dovrebbe essere già inizializzato da LaunchedEffect)
        val selectedEventId = if (groupIndex >= 0) {
            val storedId = selectedEventsByGroup[groupIndex]
            // Usa l'ID memorizzato solo se esiste ancora nel gruppo, altrimenti fallback al primo
            if (storedId != null && orderedEvents.any { it.id == storedId }) {
                storedId
            } else {
                // Fallback se non inizializzato o non valido
                val id = orderedEvents.first().id
                selectedEventsByGroup[groupIndex] = id
                id
            }
        } else {
            orderedEvents.firstOrNull()?.id
        }
        
        OverlappingEventsDialog(
            events = orderedEvents,
            selectedEventId = selectedEventId,
            onEventSelected = { eventId ->
                if (groupIndex >= 0) {
                    selectedEventsByGroup[groupIndex] = eventId
                }
            },
            onDismiss = { overlappingEventsToShow = null },
            primaryColor = primaryColor,
            backgroundColor = MaterialTheme.colorScheme.background,
            textColor = textColor,
            grayColor = grayColor
        )
    }
}

/**
 * Calcola i gruppi di eventi sovrapposti.
 * Ogni gruppo rappresenta eventi che si sovrappongono temporalmente.
 */
private fun calculateOverlappingGroups(events: List<CourseEvent>): List<OverlappingGroup> {
    val sortedEvents = events.sortedBy { it.startTime }
    val groups = mutableListOf<OverlappingGroup>()

    sortedEvents.forEach { event ->
        var addedToGroup = false

        for (group in groups) {
            // Controlla se l'evento si sovrappone a qualsiasi evento nel gruppo
            val overlaps = group.events.any { groupEvent ->
                event.startTime < groupEvent.endTime && event.endTime > groupEvent.startTime
            }

            if (overlaps) {
                group.events.add(event)
                addedToGroup = true
                break
            }
        }

        if (!addedToGroup) {
            groups.add(OverlappingGroup(mutableListOf(event)))
        }
    }

    // Ordina eventi all'interno di ogni gruppo per startTime
    groups.forEach { group ->
        group.events.sortBy { it.startTime }
    }

    return groups
}

/**
 * Rappresenta un gruppo di eventi sovrapposti.
 */
private data class OverlappingGroup(
    val events: MutableList<CourseEvent>
) {
    val visibleEvents: List<CourseEvent>
        get() = if (events.size > 2) {
            // 3+ eventi: mostra solo il primo (quello che inizia prima)
            listOf(events.first())
        } else {
            // 1-2 eventi: mostra tutti
            events
        }
    
    val hiddenEvents: List<CourseEvent>
        get() = if (events.size > 2) {
            // Se 3+, gli altri sono nascosti (tutti tranne il primo)
            events.drop(1)
        } else {
            emptyList()
        }
    
    val hasHiddenEvents: Boolean
        get() = events.size > 2
    
    val hiddenCount: Int
        get() = if (events.size > 2) events.size - 1 else 0
}

@Composable
private fun CurrentTimeIndicator(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(DayTimelineConstants.PULSE_DURATION_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(DayTimelineConstants.TIME_COLUMN_WIDTH - 6.dp))

        Box(
            modifier = Modifier
                .size(DayTimelineConstants.CURRENT_TIME_DOT_SIZE)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(primaryColor)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(DayTimelineConstants.CURRENT_TIME_LINE_HEIGHT)
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
        val dashWidth = 6.dp.toPx()
        val dashGap = 4.dp.toPx()
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


// EVENT CARD

private enum class TimelineEventStatus {
    ENDED,
    IN_PROGRESS,
    UPCOMING
}

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

    val durationText = remember(event) {
        CalendarUtils.formatDuration(event.startTime, event.endTime)
    }

    val cardAlpha = if (eventStatus == TimelineEventStatus.ENDED) 0.6f else 1f

    Box(modifier = modifier) {
        // Background Stack Cards
        if (stackDepth > 0) {
            val stackOffset = 5.dp
            // Render cards from back to front
            for (i in stackDepth downTo 1) {
                Surface(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = stackOffset * i, y = -(stackOffset * i)),
                    shape = RoundedCornerShape(DayTimelineConstants.EVENT_CARD_CORNER_RADIUS),
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
            shape = RoundedCornerShape(DayTimelineConstants.EVENT_CARD_CORNER_RADIUS),
            color = cardBackgroundColor,
            shadowElevation = if (eventStatus == TimelineEventStatus.IN_PROGRESS) 4.dp else 1.dp,
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                EventColorBar(
                    color = eventColor,
                    status = eventStatus
                )

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
private fun EventColorBar(
    color: Color,
    status: TimelineEventStatus
) {
    val barColor = when (status) {
        TimelineEventStatus.IN_PROGRESS -> EventInProgressColor
        else -> color
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bar_pulse")
    val animatedAlpha by if (status == TimelineEventStatus.IN_PROGRESS) {
        infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Box(
        modifier = Modifier
            .width(DayTimelineConstants.COLOR_BAR_WIDTH)
            .fillMaxHeight()
            .clip(
                RoundedCornerShape(
                    topStart = DayTimelineConstants.EVENT_CARD_CORNER_RADIUS,
                    bottomStart = DayTimelineConstants.EVENT_CARD_CORNER_RADIUS
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
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            EventStatusIndicator(
                event = event,
                eventStatus = eventStatus,
                eventColor = eventColor
            )
        }

        if (showCompactContent) {
            // Versione compatta: solo orario
            EventInfoChip(
                icon = Icons.Outlined.Schedule,
                text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${event.endTime.format(CalendarUtils.timeFormatter)}",
                color = grayColor
            )
        } else {
            // Versione completa
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EventInfoChip(
                    icon = Icons.Outlined.Schedule,
                    text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${event.endTime.format(CalendarUtils.timeFormatter)}",
                    color = grayColor
                )

                EventInfoChip(
                    icon = Icons.Outlined.Timer,
                    text = durationText,
                    color = grayColor
                )
            }

            val location = CalendarUtils.formatEventLocation(event.room, event.building)
            if (location != null) {
                EventInfoChip(
                    icon = Icons.Outlined.LocationOn,
                    text = location,
                    color = grayColor
                )
            }

            event.professor?.let { professor ->
                EventInfoChip(
                    icon = Icons.Outlined.Person,
                    text = professor,
                    color = grayColor
                )
            }
        }
    }
}

@Composable
private fun EventStatusIndicator(
    event: CourseEvent,
    eventStatus: TimelineEventStatus,
    eventColor: Color
) {
    when (eventStatus) {
        TimelineEventStatus.ENDED -> {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = EventInProgressColor,
                modifier = Modifier.size(24.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = stringResource(R.string.event_status_ended),
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        TimelineEventStatus.IN_PROGRESS -> {
            val infiniteTransition = rememberInfiniteTransition(label = "in_progress")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = getEventTypeIcon(event.eventType),
                        contentDescription = null,
                        tint = eventColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EventInfoChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getEventTypeIcon(eventType: EventType): ImageVector {
    return when (eventType) {
        EventType.LECTURE -> Icons.Outlined.School
        EventType.LAB -> Icons.Outlined.Science
        EventType.EXAM -> Icons.AutoMirrored.Outlined.Assignment
        EventType.OTHER -> Icons.Outlined.Event
    }
}


// LOADING STATE

@Composable
private fun TimelineLoadingState(primaryColor: Color) {
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

private fun getEarliestEvent(events: List<CourseEvent>): CourseEvent {
    return events.minByOrNull { it.startTime } ?: events.first()
}
