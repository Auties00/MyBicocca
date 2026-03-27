package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.ui.component.calendar.CalendarConfig
import it.attendance100.mybicocca.ui.component.calendar.EventStatus
import it.attendance100.mybicocca.ui.theme.EventInProgressColor

/**
 * Barra colorata laterale per le card evento.
 * Pulsa se l'evento è in corso.
 */
@Composable
fun EventColorBar(
    color: Color,
    status: EventStatus,
    width: Dp = CalendarConfig.Dimensions.COLOR_BAR_WIDTH,
    cornerRadius: Dp = CalendarConfig.Dimensions.EVENT_CARD_CORNER_RADIUS,
    modifier: Modifier = Modifier
) {
    val barColor = if (status == EventStatus.IN_PROGRESS) EventInProgressColor else color
    
    val infiniteTransition = rememberInfiniteTransition(label = "bar_pulse")
    val animatedAlpha by if (status == EventStatus.IN_PROGRESS) {
        infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween(1000, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }
    
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .clip(
                RoundedCornerShape(
                    topStart = cornerRadius,
                    bottomStart = cornerRadius
                )
            )
            .background(barColor.copy(alpha = animatedAlpha))
    )
}

/**
 * Chip con icona e testo per info evento (orario, durata, location, prof).
 */
@Composable
fun EventInfoChip(
    icon: ImageVector,
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
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
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Indicatore visivo dello stato dell'evento.
 * - IN_PROGRESS: pallino pulsante verde
 * - ENDED/UPCOMING: icona tipo evento
 */
@Composable
fun EventStatusIndicator(
    event: CalendarEvent,
    eventStatus: EventStatus,
    eventColor: Color,
    modifier: Modifier = Modifier
) {
    when (eventStatus) {
        EventStatus.IN_PROGRESS -> {
            val infiniteTransition = rememberInfiniteTransition(label = "in_progress")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    tween(800, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse
                ),
                label = "scale"
            )
            Box(
                modifier = modifier
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

        EventStatus.CANCELLED, EventStatus.ENDED, EventStatus.UPCOMING -> {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = eventColor.copy(alpha = 0.15f),
                modifier = modifier.size(28.dp)
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

/**
 * Restituisce l'icona appropriata per il tipo di evento.
 */
fun getEventTypeIcon(eventType: EventType): ImageVector = when (eventType) {
    EventType.LECTURE -> Icons.Outlined.School
    EventType.LAB -> Icons.Outlined.Science
    EventType.EXAM -> Icons.AutoMirrored.Outlined.Assignment
    EventType.OTHER -> Icons.Outlined.Event
}

/**
 * Linea tratteggiata condivisa per le griglie orarie.
 * Usata da DayTimelineView e WeekGridView.
 */
@Composable
fun DashedHourLine(
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
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f),
            strokeWidth = 1.dp.toPx()
        )
    }
}
