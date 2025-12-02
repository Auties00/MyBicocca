package it.attendance100.mybicocca.screens.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
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

// CONSTANTS

/**
 * Costanti condivise tra CalendarScreen e WeekGridView per garantire allineamento.
 */
object CalendarLayoutConstants {
    val TIME_COLUMN_WIDTH = 50.dp
}

private object WeekGridConstants {
    const val EVENT_HORIZONTAL_PADDING = 2
    const val TOTAL_DAYS = 7

    // Offset per l'effetto "cascata" degli eventi sovrapposti
    val OVERLAP_OFFSET = 4.dp
}

// MAIN COMPONENT - WEEK GRID VIEW

@Composable
fun WeekGridView(
    displayedWeekStart: LocalDate,
    events: List<CourseEvent>,
    isLoading: Boolean,
    grayColor: Color,
    primaryColor: Color,
    onEventClick: (CourseEvent) -> Unit
) {
    // Calcola i giorni della settimana da visualizzare
    val daysOfWeek = remember(displayedWeekStart) {
        (0..6).map { displayedWeekStart.plusDays(it.toLong()) }
    }

    // Filtra solo gli eventi che cadono in questa settimana
    val weekEvents = remember(events, daysOfWeek) {
        events.filter { event ->
            val eventDate = event.startTime.toLocalDate()
            !eventDate.isBefore(daysOfWeek.first()) && !eventDate.isAfter(daysOfWeek.last())
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

    // Stato per l'aggiornamento dell'ora corrente
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    // Timer per aggiornare la linea rossa ogni minuto
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
        // 1. Sfondo: Griglia con linee tratteggiate
        WeekGridBackground(
            startHour = CalendarUtils.WEEK_START_HOUR,
            endHour = CalendarUtils.WEEK_END_HOUR,
            grayColor = grayColor
        )

        // 2. Overlay: Eventi posizionati "a cascata"
        WeekEventsOverlay(
            daysOfWeek = daysOfWeek,
            events = events,
            startHour = CalendarUtils.WEEK_START_HOUR,
            onEventClick = onEventClick,
            primaryColor = primaryColor
        )

        // 3. Overlay: Linea dell'ora corrente
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
        // Colonna Orario
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
                    .offset(y = (-8).dp) // Centratura visiva rispetto alla linea
            )
        }

        // Linea orizzontale tratteggiata
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
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f), // Tratteggio 10px on, 10px off
            strokeWidth = 1.dp.toPx()
        )
    }
}

// EVENTS OVERLAY CON LOGICA "A CASCATA"

@Composable
private fun WeekEventsOverlay(
    daysOfWeek: List<LocalDate>,
    events: List<CourseEvent>,
    startHour: Int,
    onEventClick: (CourseEvent) -> Unit,
    primaryColor: Color
) {
    val hourSlotHeight = CalendarUtils.HOUR_SLOT_HEIGHT
    val density = LocalDensity.current

    Layout(
        content = {
            daysOfWeek.forEachIndexed { dayIndex, date ->
                // Filtra e ordina gli eventi del giorno
                val dayEvents = events.filter { it.startTime.toLocalDate() == date }
                    .sortedBy { it.startTime }

                dayEvents.forEach { event ->
                    val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                    // Altezza minima visiva di 15 minuti per evitare card invisibili
                    val visualDuration = maxOf(durationMinutes, 15L)
                    val eventHeightDp = (visualDuration / 60f * hourSlotHeight.value).dp

                    WeekEventBox(
                        event = event,
                        height = eventHeightDp,
                        onClick = { onEventClick(event) },
                        primaryColor = primaryColor,
                        // ID composto per identificare l'evento nel Layout custom
                        modifier = Modifier.layoutId("${dayIndex}_${event.id}")
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalendarLayoutConstants.TIME_COLUMN_WIDTH)
    ) { measurables, constraints ->
        val dayWidthPx = constraints.maxWidth / WeekGridConstants.TOTAL_DAYS.toFloat()
        val totalHours = CalendarUtils.WEEK_TOTAL_HOURS
        val maxLayoutHeight = (totalHours * hourSlotHeight.toPx()).toInt()

        val placeables = mutableListOf<Triple<Placeable, Int, Int>>() // (Placeable, x, y)

        // Raggruppa i measurables per giorno (indice 0..6)
        val measurablesByDay = measurables.groupBy {
            (it.layoutId as String).split("_")[0].toInt()
        }

        measurablesByDay.forEach { (dayIndex, dayMeasurables) ->
            // Calcola le coordinate verticali per ogni evento del giorno
            val dayEventInfos = dayMeasurables.mapNotNull { measurable ->
                val eventId = (measurable.layoutId as String).split("_")[1].toLong()
                val event = events.firstOrNull { it.id == eventId } ?: return@mapNotNull null

                val startMinutes = event.startTime.hour * 60 + event.startTime.minute
                val startOffsetMinutes = startMinutes - (startHour * 60)

                val topPx = (startOffsetMinutes / 60f * hourSlotHeight.toPx()).toInt()
                val durationMinutes = ChronoUnit.MINUTES.between(event.startTime, event.endTime)
                val heightPx = (maxOf(durationMinutes, 15L) / 60f * hourSlotHeight.toPx()).toInt()

                EventLayoutInfo(measurable, topPx, topPx + heightPx)
            }.sortedBy { it.topPx } // Ordina per orario di inizio

            // Algoritmo "Greedy" per assegnare colonne (livelli di indentazione)
            val columns = mutableListOf<MutableList<EventLayoutInfo>>()

            dayEventInfos.forEach { info ->
                // Cerca la prima colonna libera dove l'evento non si sovrappone
                var placed = false
                for (column in columns) {
                    val lastInColumn = column.last()
                    // Se l'evento inizia dopo che l'ultimo della colonna è finito -> nessun overlap
                    if (info.topPx >= lastInColumn.bottomPx) {
                        column.add(info)
                        info.columnIndex = columns.indexOf(column)
                        placed = true
                        break
                    }
                }

                // Se non c'è posto, crea una nuova colonna (sovrapposizione)
                if (!placed) {
                    val newCol = mutableListOf(info)
                    columns.add(newCol)
                    info.columnIndex = columns.size - 1
                }
            }

            // Posiziona gli eventi calcolando offset e larghezza
            dayEventInfos.forEach { info ->
                val colIndex = info.columnIndex
                // Offset verso destra basato sull'indice di colonna (effetto cascata)
                val overlapOffsetPx = with(density) { WeekGridConstants.OVERLAP_OFFSET.toPx() } * colIndex

                // La larghezza si riduce per ogni livello di indentazione per rimanere nella colonna del giorno
                val widthPx = (dayWidthPx - overlapOffsetPx - with(density) { 2.dp.toPx() }).coerceAtLeast(10f)

                val placeable = info.measurable.measure(
                    constraints.copy(
                        minWidth = 0,
                        maxWidth = widthPx.toInt(),
                        minHeight = 0
                    )
                )

                val xPos = (dayIndex * dayWidthPx + overlapOffsetPx + with(density) { 1.dp.toPx() }).toInt()

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

// Classe di supporto per il layout
private data class EventLayoutInfo(
    val measurable: Measurable,
    val topPx: Int,
    val bottomPx: Int,
    var columnIndex: Int = 0
)

// EVENT BOX OTTIMIZZATA

@Composable
private fun WeekEventBox(
    event: CourseEvent,
    height: Dp,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)

    // Determina quali dettagli mostrare in base all'altezza
    val showText = height >= 14.dp
    val showDetails = height >= 35.dp // Mostra aula solo se c'è spazio

    Surface(
        modifier = modifier
            .height(height)
            .padding(bottom = 1.dp), // Separatore bianco sottile tra eventi contigui
        shape = RoundedCornerShape(6.dp),
        color = eventColor.copy(alpha = 0.95f), // Leggera trasparenza
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), // Bordo per stacco visivo
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        if (showText) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 3.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.Top // Testo sempre in alto
            ) {
                // Nome corso
                Text(
                    text = event.courseName,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (showDetails) 2 else 1,
                    lineHeight = 10.sp,
                    overflow = TextOverflow.Ellipsis
                )

                // Dettagli (Aula)
                if (showDetails) {
                    event.room?.let { room ->
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = room,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 8.sp,
                            maxLines = 1,
                            lineHeight = 9.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// INDICATORE ORA CORRENTE (LINEA ROSSA)
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

    // Mostra l'indicatore solo se l'orario corrente è nel range visualizzato (es. 7-22)
    val currentHour = currentTime.hour
    if (currentHour >= startHour && currentHour < CalendarUtils.WEEK_END_HOUR) {

        // Calcolo posizione Y (uguale per tutti i giorni)
        val totalMinutes = (currentHour - startHour) * 60 + currentTime.minute
        val topOffsetDp = (totalMinutes / 60f * hourSlotHeight.value).dp

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalMinutes.dp) // Altezza fittizia per il layout, usiamo offset nel draw
                .padding(start = CalendarLayoutConstants.TIME_COLUMN_WIDTH)
        ) {
            val lineY = with(density) { topOffsetDp.toPx() }

            // 1. Linea rossa su TUTTA la larghezza
            drawLine(
                color = primaryColor,
                start = Offset(0f, lineY), // Inizia da sinistra (dopo la colonna orari)
                end = Offset(size.width, lineY), // Finisce a destra
                strokeWidth = 1.dp.toPx() // Leggermente più sottile se è lunga
            )

            // 2. Pallino solo sul giorno corrente (se visibile nella settimana)
            if (daysOfWeek.contains(today)) {
                val dayIndex = daysOfWeek.indexOf(today)
                val dayWidth = size.width / WeekGridConstants.TOTAL_DAYS
                // Calcola il centro della colonna del giorno corrente o l'inizio
                val circleX = (dayIndex * dayWidth) // Mettiamo il pallino all'inizio della colonna del giorno

                drawCircle(
                    color = primaryColor,
                    radius = 3.dp.toPx(),
                    center = Offset(circleX, lineY)
                )
            }
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
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp,
            color = primaryColor
        )
    }
}