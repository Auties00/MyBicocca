package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.ui.screen.calendar.ext.colorsFor
import it.attendance100.mybicocca.ui.screen.calendar.ext.compactLabel
import it.attendance100.mybicocca.ui.screen.calendar.ext.formatTimeRange
import it.attendance100.mybicocca.ui.screen.calendar.ext.locationLine
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime

/** Content tiers an event card steps through as its frame shrinks, from full detail down to a bare dot. */
enum class EventCardDensity { Full, Compact, Icon, Mini, Dot }

/**
 * Timeline event card that adapts its content to the frame it is given: density is picked
 * from the measured size, letting the same composable serve the day and week event layers
 * where pixel dimensions come straight from event duration and lane width.
 * [forcedDensity] bypasses the measurement for hosts that know their tier.
 */
@Composable
fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    forcedDensity: EventCardDensity? = null,
) {
    BoxWithConstraints(modifier = modifier) {
        val density = forcedDensity ?: pickDensity(maxWidth.value, maxHeight.value)
        EventCardCore(event = event, density = density, onClick = onClick, modifier = Modifier.fillMaxSize())
    }
}

private fun pickDensity(widthDp: Float, heightDp: Float): EventCardDensity = when {
    heightDp < 14f -> EventCardDensity.Dot
    heightDp < 22f -> EventCardDensity.Mini
    widthDp < 50f -> EventCardDensity.Icon
    heightDp < 40f -> EventCardDensity.Compact
    else -> EventCardDensity.Full
}

/**
 * Card chrome shared by every density: a container tinted with the event's swatch behind
 * a 3dp leading accent stripe. Cancelled events dim, gain a dashed accent border and
 * strike their text through.
 */
@Composable
private fun EventCardCore(
    event: CalendarEvent,
    density: EventCardDensity,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val now by rememberCurrentTime()
    val colors = event.colorsFor(now)
    val cancelled = event.status == EventStatus.CANCELLED
    val cornerRadius = if (density == EventCardDensity.Full) 8.dp else 6.dp
    val containerAlpha = if (cancelled) 0.55f else 1f
    val accentAlpha = if (cancelled) 0.45f else 1f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.container.copy(alpha = containerAlpha))
            .then(
                if (cancelled) {
                    Modifier.drawBehind {
                        drawDashedBorder(colors.accent.copy(alpha = 0.6f), cornerRadius.toPx())
                    }
                } else Modifier,
            )
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxSize()
                .background(colors.accent.copy(alpha = accentAlpha)),
        )

        when (density) {
            EventCardDensity.Dot -> DotContent(colors.accent, accentAlpha)
            EventCardDensity.Mini -> MiniContent(event, colors.onContainer, cancelled)
            EventCardDensity.Icon -> IconContent(event, colors.onContainer, cancelled)
            EventCardDensity.Compact -> CompactContent(event, colors.onContainer, cancelled)
            EventCardDensity.Full -> FullContent(event, colors.onContainer, cancelled, colors.accent)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDashedBorder(color: Color, radius: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(1f, 1f),
        size = Size(size.width - 2f, size.height - 2f),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = 1.5f.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))),
    )
}

@Composable
private fun DotContent(accent: Color, alpha: Float) {
    Box(
        modifier = Modifier.fillMaxSize().padding(start = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(accent.copy(alpha = alpha))
                .padding(4.dp),
        )
    }
}

@Composable
private fun MiniContent(event: CalendarEvent, textColor: Color, cancelled: Boolean) {
    Text(
        text = "${event.compactLabel()} · ${event.locationLine().ifBlank { event.title.take(8) }}",
        modifier = Modifier.padding(start = 7.dp, end = 4.dp).fillMaxSize(),
        color = textColor,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
    )
}

@Composable
private fun IconContent(event: CalendarEvent, textColor: Color, cancelled: Boolean) {
    Column(
        modifier = Modifier.padding(start = 7.dp, end = 4.dp, top = 4.dp, bottom = 4.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = event.compactLabel(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
        )
        Text(
            text = "%02d:%02d".format(event.start.hour, event.start.minute),
            color = textColor.copy(alpha = 0.75f),
            fontSize = 9.sp,
        )
    }
}

@Composable
private fun CompactContent(event: CalendarEvent, textColor: Color, cancelled: Boolean) {
    Column(
        modifier = Modifier.padding(start = 9.dp, end = 6.dp, top = 6.dp, bottom = 6.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = event.formatTimeRange(),
            color = textColor.copy(alpha = 0.78f),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = event.title,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
        )
    }
}

@Composable
private fun FullContent(event: CalendarEvent, textColor: Color, cancelled: Boolean, accent: Color) {
    Column(
        modifier = Modifier.padding(start = 11.dp, end = 8.dp, top = 8.dp, bottom = 8.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = event.formatTimeRange(),
            color = textColor.copy(alpha = 0.78f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )
        Column {
            Text(
                text = event.title,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
            )
            Text(
                text = event.activityLabelShort(),
                color = textColor.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.weight(1f))
        val location = event.locationLine()
        if (location.isNotBlank()) {
            Text(
                text = location,
                color = textColor.copy(alpha = 0.85f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun CalendarEvent.activityLabelShort(): String = when (this) {
    is CalendarEvent.Lesson -> "Lezione"
    is CalendarEvent.Exam -> {
        val type = examTypeLabel?.takeIf { it.isNotBlank() }
        if (type != null) "Esame · $type" else "Esame"
    }
    is CalendarEvent.AssignmentDeadline -> "Scadenza compito"
    is CalendarEvent.Appointment -> "Appuntamento"
    is CalendarEvent.LibraryReservation -> "Biblioteca"
}
