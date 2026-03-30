package it.attendance100.mybicocca.ui.component.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.data.model.campus.RoomOccupationEvent
import java.time.format.DateTimeFormatter

private const val TIMELINE_START_HOUR = 8
private const val TIMELINE_END_HOUR = 21
private const val TOTAL_HOURS = TIMELINE_END_HOUR - TIMELINE_START_HOUR

@Composable
fun OccupationTimeline(
    events: List<RoomOccupationEvent>,
    modifier: Modifier = Modifier,
) {
    val grouped = remember(events) {
        events.groupBy { it.roomName }.entries.sortedBy { it.key }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Hour labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            for (hour in TIMELINE_START_HOUR..TIMELINE_END_HOUR step 2) {
                Text(
                    text = "${hour}:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(
                        (2f / TOTAL_HOURS * 300).dp
                    ),
                )
            }
        }

        // Room rows
        grouped.take(8).forEach { (roomName, roomEvents) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(80.dp),
                )
                Spacer(Modifier.width(8.dp))

                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp),
                ) {
                    // Track background
                    drawRoundRect(
                        color = trackColor,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        size = size,
                    )

                    // Event blocks
                    roomEvents.forEach { event ->
                        val startHour = event.startDateTime.hour + event.startDateTime.minute / 60f
                        val endHour = event.endDateTime.hour + event.endDateTime.minute / 60f

                        val startFraction =
                            ((startHour - TIMELINE_START_HOUR) / TOTAL_HOURS).coerceIn(0f, 1f)
                        val endFraction =
                            ((endHour - TIMELINE_START_HOUR) / TOTAL_HOURS).coerceIn(0f, 1f)

                        if (endFraction > startFraction) {
                            drawRoundRect(
                                color = primaryColor,
                                topLeft = Offset(startFraction * size.width, 0f),
                                size = Size(
                                    (endFraction - startFraction) * size.width,
                                    size.height,
                                ),
                                cornerRadius = CornerRadius(4.dp.toPx()),
                            )
                        }
                    }
                }
            }
        }

        if (grouped.size > 8) {
            Text(
                text = "+${grouped.size - 8} altre aule",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
