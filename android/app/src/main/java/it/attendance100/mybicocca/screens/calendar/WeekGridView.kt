package it.attendance100.mybicocca.screens.calendar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.utils.*
import kotlinx.coroutines.delay
import java.time.*
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

// ============================================================================
// MAIN COMPONENT - WEEK GRID VIEW
// ============================================================================

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

// ============================================================================
// GRID CONTENT WITH PINCH-TO-ZOOM
// ============================================================================

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

    // Calcola altezza totale del contenuto al 100% zoom
    val totalHours = CalendarUtils.WEEK_END_HOUR - CalendarUtils.WEEK_START_HOUR
    val baseContentHeight = CalendarUtils.HOUR_SLOT_HEIGHT * totalHours

    // Stato per l'altezza del container disponibile
    var containerHeight by remember { mutableFloatStateOf(0f) }

    // Calcola MIN_ZOOM dinamicamente: quando il contenuto occupa tutto lo schermo
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

    // Stato zoom con animazione
    var targetZoom by remember { mutableFloatStateOf(CalendarUtils.DEFAULT_ZOOM) }
    val zoomLevel by animateFloatAsState(
        targetValue = targetZoom,
        animationSpec = tween(durationMillis = 150),
        label = "zoomAnimation"
    )

    // Stato per gesture swipe orizzontale
    var isSwiping by remember { mutableStateOf(false) }

    // Determina se siamo in modalità compatta
    val isCompactMode by remember(zoomLevel, minZoom) {
        derivedStateOf { zoomLevel <= minZoom * 1.1f }
    }

    // Aggiorna l'ora corrente ogni minuto
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000L)
        }
    }

    // Effetto per aggiustare lo scroll quando cambia lo zoom
    LaunchedEffect(zoomLevel) {
        // Mantiene la posizione relativa dello scroll quando si zooma
        val maxScroll = scrollState.maxValue
        if (maxScroll > 0) {
            val currentRatio = scrollState.value.toFloat() / maxScroll
            // Lo scroll verrà aggiornato automaticamente dal nuovo contenuto
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                containerHeight = size.height.toFloat()
            }
            .pointerInput(minZoom) {
                // Gestione custom delle gesture per separare pinch da scroll
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)

                    // Aspetta per vedere se arriva un secondo dito
                    var secondPointer: PointerId? = null
                    var isPinching = false

                    // Track per horizontal swipe
                    var totalDragX = 0f
                    var hasSwiped = false

                    do {
                        val event = awaitPointerEvent()
                        val pointers = event.changes.filter { it.pressed }

                        when {
                            // Due o più dita = pinch to zoom
                            pointers.size >= 2 && !hasSwiped -> {
                                isPinching = true

                                val pointer1 = pointers[0]
                                val pointer2 = pointers[1]

                                // Calcola distanza attuale e precedente
                                val currentDistance = (pointer1.position - pointer2.position).getDistance()
                                val previousDistance = (pointer1.previousPosition - pointer2.previousPosition).getDistance()

                                if (previousDistance > 0f && currentDistance > 0f) {
                                    val zoomChange = currentDistance / previousDistance

                                    // Applica il cambio di zoom
                                    if (zoomChange != 1f) {
                                        val newZoom = (targetZoom * zoomChange).coerceIn(minZoom, CalendarUtils.MAX_ZOOM)
                                        targetZoom = newZoom
                                    }
                                }

                                // Consuma gli eventi per evitare scroll
                                pointers.forEach { it.consume() }
                            }

                            // Un dito solo = potenziale swipe orizzontale
                            pointers.size == 1 && !isPinching -> {
                                val change = pointers.first()
                                val dragX = change.position.x - change.previousPosition.x
                                val dragY = change.position.y - change.previousPosition.y

                                // Se il movimento è più orizzontale che verticale
                                if (!hasSwiped && dragX.absoluteValue > dragY.absoluteValue * 1.5f) {
                                    totalDragX += dragX

                                    // Threshold per swipe settimana
                                    if (totalDragX.absoluteValue > 80f && !isSwiping) {
                                        isSwiping = true
                                        hasSwiped = true
                                        val direction = if (totalDragX > 0) 1 else -1
                                        onSwipeWeek(direction)
                                        change.consume()
                                    }
                                }
                                // Se è verticale, lascia passare per lo scroll
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    isSwiping = false
                }
            }
    ) {
        // Contenuto scrollabile
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            if (isCompactMode) {
                // Vista compatta: eventi impilati senza timeline
                CompactWeekView(
                    daysOfWeek = daysOfWeek,
                    events = events,
                    onEventClick = onEventClick,
                    onDayZoom = onDayZoom,
                    primaryColor = primaryColor,
                    grayColor = grayColor
                )
            } else {
                // Vista normale con timeline zoomabile
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

        // Indicatore zoom (opzionale, per debug - rimuovere in produzione)
        /*
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${(zoomLevel * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        */
    }
}

// ============================================================================
// COMPACT WEEK VIEW (per zoom minimo)
// ============================================================================

@Composable
private fun CompactWeekView(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    onEventClick: (CourseEvent) -> Unit,
    onDayZoom: (LocalDate) -> Unit,
    primaryColor: Color,
    grayColor: Color
) {
    val density = LocalDensity.current

    // Raggruppa eventi per giorno
    val eventsByDay = remember(events, daysOfWeek) {
        daysOfWeek.associateWith { date ->
            events.filter { it.startTime.toLocalDate() == date }
                .sortedBy { it.startTime }
        }
    }

    // Calcola l'altezza massima necessaria
    val maxEventsInDay = eventsByDay.values.maxOfOrNull { it.size } ?: 0
    val compactHeight = (maxEventsInDay * (CalendarUtils.COMPACT_EVENT_HEIGHT.value + 2)).dp + 8.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = compactHeight)
            .padding(start = CalendarUtils.TIME_COLUMN_WIDTH)
    ) {
        daysOfWeek.forEachIndexed { index, date ->
            val dayEvents = eventsByDay[date] ?: emptyList()
            val isToday = date == LocalDate.now()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(
                        if (index < 6) Modifier.padding(end = 1.dp) else Modifier
                    )
            ) {
                // Header del giorno in modalità compatta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isToday) primaryColor.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) primaryColor else grayColor
                    )
                }

                // Eventi compatti
                dayEvents.forEach { event ->
                    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)

                    val hasOverlap = dayEvents.any { other ->
                        other.id != event.id &&
                                (event.startTime.isBefore(other.endTime) &&
                                        event.endTime.isAfter(other.startTime))
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

// ============================================================================
// GRID BACKGROUND
// ============================================================================

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
        // Colonna orario
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

        // Area griglia con linea tratteggiata
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

// ============================================================================
// EVENTS OVERLAY
// ============================================================================

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
                val dayEvents = events
                    .filter { it.startTime.toLocalDate() == date }
                    .sortedBy { it.startTime }

                dayEvents.forEach { event ->
                    val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                    val visualDuration = maxOf(durationMinutes, 15L)
                    val eventHeightDp = (visualDuration / 60f * scaledHourSlotHeight.value).dp

                    val hasOverlap = dayEvents.any { other ->
                        other.id != event.id &&
                                (event.startTime.isBefore(other.endTime) &&
                                        event.endTime.isAfter(other.startTime))
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

        // Raggruppa i measurable per giorno
        val measurablesByDay = measurables.groupBy {
            (it.layoutId as String).split("_")[0].toInt()
        }

        measurablesByDay.forEach { (dayIndex, dayMeasurables) ->
            // Crea info di layout per ogni evento del giorno
            val dayEventInfos = dayMeasurables.mapNotNull { measurable ->
                val eventId = (measurable.layoutId as String).split("_")[1].toLong()
                val event = events.firstOrNull { it.id == eventId } ?: return@mapNotNull null

                val eventStartMinutes = (event.startTime.hour - startHour) * 60 + event.startTime.minute
                val topPx = (eventStartMinutes / 60f * scaledHourSlotHeight.toPx()).toInt()

                val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                val visualDuration = maxOf(durationMinutes, 15L)
                val bottomPx = topPx + (visualDuration / 60f * scaledHourSlotHeight.toPx()).toInt()

                EventLayoutInfo(measurable, topPx, bottomPx)
            }.sortedBy { it.topPx }

            // Assegna colonne per gestire sovrapposizioni
            assignColumns(dayEventInfos)

            // Misura e posiziona gli eventi
            val overlapOffsetPx = with(density) { CalendarUtils.OVERLAP_OFFSET.toPx() }

            dayEventInfos.forEach { info ->
                val eventWidth = (dayWidthPx - overlapOffsetPx * 2 - 4).toInt()
                    .coerceAtLeast(1)

                val placeable = info.measurable.measure(
                    constraints.copy(
                        minWidth = eventWidth,
                        maxWidth = eventWidth,
                        minHeight = 0,
                        maxHeight = (info.bottomPx - info.topPx).coerceAtLeast(1)
                    )
                )

                val xPos = (dayIndex * dayWidthPx +
                        info.columnIndex * overlapOffsetPx +
                        with(density) { 1.dp.toPx() }).toInt()

                placeables.add(Triple(placeable, xPos, info.topPx))
            }
        }

        layout(constraints.maxWidth, maxLayoutHeight) {
            placeables.forEach { (placeable, x, y) ->
                placeable.place(x, y)
            }
        }
    }
}

private data class EventLayoutInfo(
    val measurable: Measurable,
    val topPx: Int,
    val bottomPx: Int,
    var columnIndex: Int = 0
)

private fun assignColumns(events: List<EventLayoutInfo>) {
    if (events.isEmpty()) return

    events.forEach { current ->
        // Trova eventi che si sovrappongono e sono già stati posizionati
        val overlapping = events.filter { other ->
            other !== current &&
                    other.columnIndex >= 0 &&
                    current.topPx < other.bottomPx &&
                    current.bottomPx > other.topPx
        }

        // Trova la prima colonna libera
        val usedColumns = overlapping.map { it.columnIndex }.toSet()
        var column = 0
        while (column in usedColumns) {
            column++
        }
        current.columnIndex = column
    }
}

// ============================================================================
// EVENT BOX
// ============================================================================

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
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

// ============================================================================
// CURRENT TIME INDICATOR
// ============================================================================

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

    // Mostra solo se l'ora corrente è nell'intervallo visibile
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

            // Linea orizzontale
            drawLine(
                color = primaryColor,
                start = Offset(0f, lineY),
                end = Offset(size.width, lineY),
                strokeWidth = 1.5.dp.toPx()
            )

            // Pallino sul giorno corrente
            if (daysOfWeek.contains(today)) {
                val dayIndex = daysOfWeek.indexOf(today)
                val dayWidth = size.width / CalendarUtils.TOTAL_DAYS
                val circleX = (dayIndex * dayWidth) + (dayWidth / 2)

                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(circleX, lineY)
                )
            }
        }
    }
}

// ============================================================================
// LOADING STATE
// ============================================================================

@Composable
private fun WeekGridLoadingState(primaryColor: Color) {
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