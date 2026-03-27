package it.attendance100.mybicocca.ui.component.calendar.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.component.calendar.CalendarUtils
import it.attendance100.mybicocca.ui.component.calendar.OverlapCalculator
import it.attendance100.mybicocca.ui.component.calendar.OverlapGroup

/**
 * Event card for single events in the week timeline.
 */
@Composable
internal fun WeekEventCard(
    event: CalendarEvent,
    height: Dp,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val showText = height >= 16.dp
    val showLocation = height >= 40.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = MaterialTheme.shapes.small,
        color = eventColor,
        onClick = onClick
    ) {
        if (showText) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 3.dp)
            ) {
                Text(
                    text = event.title,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (showLocation) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )

                val locationText = CalendarUtils.formatEventLocation(event.location, event.buildingCode)
                if (showLocation && !locationText.isNullOrBlank()) {
                    Text(
                        text = locationText,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Merged event block for overlapping events.
 * Displays first event name with badge showing total count.
 */
@Composable
internal fun MergedEventBlock(
    group: OverlapGroup,
    height: Dp,
    onClick: () -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val firstEvent = OverlapCalculator.getEarliestEvent(group.events)
    val eventColor = CalendarUtils.getEventColor(firstEvent.eventType, primaryColor)
    val timeSpan = OverlapCalculator.formatGroupTimeSpan(group)
    val eventCount = group.events.size
    val showText = height >= 16.dp
    val showTimeSpan = height >= 40.dp

    // Badge takes ~18dp height, so offset title below it when height allows
    val badgeHeight = 18.dp
    val hasSpaceForBadgeAndText = height >= 36.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = MaterialTheme.shapes.small,
        color = eventColor,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Badge with event count - always at top end
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp),
                containerColor = Color.White,
                contentColor = eventColor
            ) {
                Text(
                    text = "$eventCount",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showText) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 4.dp,
                            end = 4.dp,
                            // Push text below badge when there's space
                            top = if (hasSpaceForBadgeAndText) badgeHeight else 3.dp,
                            bottom = 3.dp
                        )
                ) {
                    Text(
                        text = firstEvent.title,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = if (showTimeSpan) 2 else 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (showTimeSpan) {
                        Text(
                            text = timeSpan,
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact event card for zoomed-out view.
 */
@Composable
internal fun CompactEventCard(
    event: CalendarEvent,
    height: Dp,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val eventColor = CalendarUtils.getEventColor(event.eventType, primaryColor)
    val locationText = CalendarUtils.formatEventLocation(event.location, event.buildingCode)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        shape = MaterialTheme.shapes.small,
        color = eventColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = event.title,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = if (height > 60.dp) 3 else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (height > 40.dp && !locationText.isNullOrBlank()) {
                Text(
                    text = locationText,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
