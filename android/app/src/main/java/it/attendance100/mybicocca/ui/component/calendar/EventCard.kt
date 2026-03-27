package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import it.attendance100.mybicocca.ui.component.calendar.EventStatus
import it.attendance100.mybicocca.ui.component.calendar.resolveStatus
import it.attendance100.mybicocca.ui.theme.EventInProgressColor

/**
 * Varianti di visualizzazione per EventCard.
 */
enum class EventCardVariant {
    /** Stile compatto per MonthGridView - usa ListItem */
    COMPACT,
    /** Stile timeline per DayTimelineView e SearchListScreen - colonna orario separata */
    TIMELINE,
    /** Stile lista per SwipeableCardStack - supporta selezione */
    LIST
}

/**
 * Componente unificato per visualizzare eventi calendario.
 * Supporta diversi layout tramite il parametro [variant].
 *
 * @param event L'evento da visualizzare
 * @param variant Lo stile di visualizzazione
 * @param onClick Callback per il click (opzionale)
 * @param modifier Modifier da applicare al componente
 * @param isSelected Se true, mostra lo stato selezionato (solo per LIST variant)
 * @param showStatusIndicator Se mostrare l'indicatore di stato (solo per TIMELINE variant)
 * @param showLocation Se mostrare la location
 * @param trailingContent Contenuto personalizzato a destra (per COMPACT e TIMELINE variant)
 */
@Composable
fun EventCard(
    event: CalendarEvent,
    variant: EventCardVariant,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showStatusIndicator: Boolean = true,
    showLocation: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val eventColor = CalendarUtils.getEventColor(event.eventType, colorScheme.primary)
    val eventStatus = remember(event) { event.resolveStatus() }

    when (variant) {
        EventCardVariant.COMPACT -> CompactEventCardLayout(
            event = event,
            eventColor = eventColor,
            eventStatus = eventStatus,
            onClick = onClick,
            modifier = modifier,
            trailingContent = trailingContent
        )
        EventCardVariant.TIMELINE -> TimelineEventCardLayout(
            event = event,
            eventColor = eventColor,
            eventStatus = eventStatus,
            onClick = onClick,
            modifier = modifier,
            showLocation = showLocation,
            showStatusIndicator = showStatusIndicator,
            trailingContent = trailingContent
        )
        EventCardVariant.LIST -> ListEventCardLayout(
            event = event,
            eventColor = eventColor,
            isSelected = isSelected,
            onClick = onClick,
            modifier = modifier
        )
    }
}

// COMPACT VARIANT - Per MonthGridView (sostituisce EventPreviewItem)

@Composable
private fun CompactEventCardLayout(
    event: CalendarEvent,
    eventColor: Color,
    eventStatus: EventStatus,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeFormatter = CalendarUtils.timeFormatter

    ListItem(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(eventColor.copy(alpha = 0.1f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        leadingContent = {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(eventColor)
            )
        },
        headlineContent = {
            Text(
                text = event.title,
                color = colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${event.startDateTime.format(timeFormatter)} - ${event.endDateTime.format(timeFormatter)}",
                color = colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        },
        trailingContent = trailingContent ?: {
            if (event.location != null) {
                Badge(
                    containerColor = colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                    contentColor = colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

// TIMELINE VARIANT - Per DayTimelineView (sostituisce CompactEventCard)

@Composable
private fun TimelineEventCardLayout(
    event: CalendarEvent,
    eventColor: Color,
    eventStatus: EventStatus,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    showLocation: Boolean = true,
    showStatusIndicator: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeFormatter = CalendarUtils.timeFormatter

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = eventColor.copy(alpha = 0.1f),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra colorata
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(eventColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Orario in colonna
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = event.startDateTime.format(timeFormatter),
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = event.endDateTime.format(timeFormatter),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info evento
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (showLocation) {
                    CalendarUtils.formatEventLocation(event.location, event.buildingCode)?.let { location ->
                        Text(
                            text = location,
                            color = colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Trailing content: custom or status indicator
            when {
                trailingContent != null -> trailingContent()
                showStatusIndicator -> TimelineStatusIndicator(
                    eventStatus = eventStatus,
                    eventColor = eventColor,
                    eventType = event.eventType
                )
            }
        }
    }
}

@Composable
private fun TimelineStatusIndicator(
    eventStatus: EventStatus,
    eventColor: Color,
    eventType: EventType
) {
    when (eventStatus) {
        EventStatus.IN_PROGRESS -> {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(EventInProgressColor)
            )
        }
        EventStatus.ENDED, EventStatus.CANCELLED -> {
            Icon(
                Icons.Outlined.Check,
                contentDescription = null,
                tint = EventInProgressColor,
                modifier = Modifier.size(20.dp)
            )
        }
        EventStatus.UPCOMING -> {
            Icon(
                getEventTypeIcon(eventType),
                contentDescription = null,
                tint = eventColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// LIST VARIANT - Per SwipeableCardStack (sostituisce ExpandedEventsList item)
@Composable
private fun ListEventCardLayout(
    event: CalendarEvent,
    eventColor: Color,
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val timeFormatter = CalendarUtils.timeFormatter

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                eventColor.copy(alpha = 0.1f)
            else
                colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium,
        border = if (isSelected)
            BorderStroke(1.5.dp, eventColor.copy(alpha = 0.5f))
        else
            BorderStroke(0.5.dp, colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(eventColor)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = event.title,
                    color = colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${event.startDateTime.format(timeFormatter)} - ${event.endDateTime.format(timeFormatter)}",
                    color = colorScheme.outline,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

