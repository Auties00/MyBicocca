package it.attendance100.mybicocca.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.utils.*

/**
 * Dialog che mostra eventi sovrapposti in una lista espandibile.
 * Cliccando su un evento si espande inline con tutti i dettagli e funzionalità.
 * Permette di selezionare quale evento visualizzare nella timeline principale.
 */
@Composable
fun OverlappingEventsDialog(
    events: List<CourseEvent>,
    selectedEventId: Long?,
    onEventSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color,
    backgroundColor: Color,
    textColor: Color,
    grayColor: Color
) {
    var expandedEventId by remember { mutableStateOf<Long?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = backgroundColor,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                OverlappingEventsDialogHeader(
                    eventsCount = events.size,
                    onDismiss = onDismiss,
                    primaryColor = primaryColor,
                    textColor = textColor,
                    grayColor = grayColor
                )

                HorizontalDivider(
                    color = grayColor.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                // Lista eventi
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    // Ordina eventi per startTime
                    val sortedEvents = remember(events) { events.sortedBy { it.startTime } }
                    
                    sortedEvents.forEachIndexed { index, event ->
                        val isExpanded = expandedEventId == event.id
                        val isSelected = selectedEventId == event.id

                        ExpandableEventCardWithDetails(
                            event = event,
                            isExpanded = isExpanded,
                            isSelected = isSelected,
                            onToggle = {
                                expandedEventId = if (isExpanded) null else event.id
                            },
                            onSelect = { onEventSelected(event.id) },
                            primaryColor = primaryColor,
                            backgroundColor = backgroundColor,
                            textColor = textColor,
                            grayColor = grayColor
                        )

                        if (index < sortedEvents.lastIndex) {
                            HorizontalDivider(
                                color = grayColor.copy(alpha = 0.1f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlappingEventsDialogHeader(
    eventsCount: Int,
    onDismiss: () -> Unit,
    primaryColor: Color,
    textColor: Color,
    grayColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.overlapping_events_title),
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Visualizza i dettagli e scegli quale mostrare",
                color = grayColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun ExpandableEventCardWithDetails(
    event: CourseEvent,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onSelect: () -> Unit,
    primaryColor: Color,
    backgroundColor: Color,
    textColor: Color,
    grayColor: Color
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isSelected -> eventColor.copy(alpha = 0.12f)
            isExpanded -> eventColor.copy(alpha = 0.05f)
            else -> Color.Transparent
        },
        border = if (isSelected) BorderStroke(2.dp, eventColor) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header compatto (sempre visibile)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radio button per selezione
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = eventColor,
                            unselectedColor = grayColor
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.courseName,
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = eventColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Visibile",
                                        color = eventColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${event.startTime.format(CalendarUtils.timeFormatter)} - ${event.endTime.format(CalendarUtils.timeFormatter)}",
                            color = grayColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = grayColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Contenuto espanso completo
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(color = grayColor.copy(alpha = 0.2f))

                    // Dettagli evento
                    EventDetailsSection(
                        event = event,
                        textColor = textColor,
                        grayColor = grayColor
                    )

                    // Pulsanti azioni
                    EventActionsSection(
                        event = event,
                        eventColor = eventColor,
                        context = context,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun EventDetailsSection(
    event: CourseEvent,
    textColor: Color,
    grayColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Durata
        EventDetailRow(
            icon = Icons.Outlined.Timer,
            label = stringResource(R.string.event_duration),
            value = CalendarUtils.formatDuration(event.startTime, event.endTime),
            textColor = textColor,
            grayColor = grayColor
        )

        // Location
        val location = CalendarUtils.formatEventLocation(event.room, event.building)
        if (location != null) {
            EventDetailRow(
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.event_location),
                value = location,
                textColor = textColor,
                grayColor = grayColor
            )
        }

        // Professore
        event.professor?.let { professor ->
            EventDetailRow(
                icon = Icons.Outlined.Person,
                label = stringResource(R.string.event_professor),
                value = professor,
                textColor = textColor,
                grayColor = grayColor
            )
        }

        // Cancellato
        if (event.isCancelled) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.event_cancelled),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun EventActionsSection(
    event: CourseEvent,
    eventColor: Color,
    context: android.content.Context,
    textColor: Color
) {
    // Solo eLearning
    event.courseCode?.let {
        EventActionButton(
            text = "Apri e-Learning",
            icon = Icons.Outlined.School,
            color = eventColor,
            onClick = { 
                // TODO: Implementare apertura e-Learning
            }
        )
    }
}

@Composable
private fun EventDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    textColor: Color,
    grayColor: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = grayColor,
            modifier = Modifier.size(20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                color = grayColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EventActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
