package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime
import it.attendance100.mybicocca.ui.screen.calendar.state.layoutDay
import java.time.LocalDate

/**
 * Event-card layer of the day timeline: a custom layout that turns each laid-out event's
 * minute range and lane fractions into a fixed card frame, one minute mapping to
 * [minuteHeight]. Hourly grid lines draw behind the cards, odd hours fading out at low
 * zoom, and the now indicator appears while [selectedDay] is today and the current minute
 * sits inside the layout window. Small pixel gaps are carved between adjacent cards so
 * lanes read as separate, and every card keeps a minimum tappable frame.
 *
 * The vertical window is [window], shared with the hour gutter and every other pager page so
 * adjacent days and the labels all sit on one scale. [layoutDay] receives it and only grows
 * it when this day's events spill past it — with a window pre-fitted to all loaded days they
 * never do.
 */
@Composable
fun DayEventsLayer(
    selectedDay: LocalDate,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
    minuteHeight: Dp = TimelineMinuteHeightDefault,
    window: IntRange = TimelineWindowStart..TimelineWindowEnd,
) {
    val layout = remember(events, window) { layoutDay(events, window) }
    val now by rememberCurrentTime()
    val scheme = MaterialTheme.colorScheme
    val gridColor = scheme.outline.copy(alpha = 0.5f)
    val nowColor = scheme.primary
    val pxPerMinute = with(LocalDensity.current) { minuteHeight.toPx() }

    val nowMinute = if (selectedDay == now.toLocalDate()) {
        now.toLocalTime().let { it.hour * 60 + it.minute }
    } else null
    val oddGridAlpha = oddHourAlphaFor(minuteHeight)

    Layout(
        modifier = modifier
            .height(timelineHeightFor(minuteHeight, window))
            .fillMaxWidth()
            .drawBehind {
                val width = size.width
                val firstLineMinute = ((layout.startMinute + 59) / 60) * 60
                var minute = firstLineMinute
                while (minute <= layout.endMinute) {
                    val y = (minute - layout.startMinute) * pxPerMinute
                    val effectiveAlpha = if ((minute / 60) % 2 != 0) oddGridAlpha else 1f
                    if (effectiveAlpha > 0f) {
                        drawLine(
                            color = gridColor.copy(alpha = gridColor.alpha * effectiveAlpha),
                            strokeWidth = 1f,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                        )
                    }
                    minute += 60
                }
                nowMinute?.let { nm ->
                    if (nm in layout.startMinute..layout.endMinute) {
                        drawNowIndicator(nm, layout.startMinute, pxPerMinute, nowColor)
                    }
                }
            },
        content = {
            layout.items.forEach { item ->
                EventCard(
                    event = item.event,
                    onClick = { onEventClick(item.event) },
                    modifier = Modifier.layoutId(
                        DayEventTag(item.startMinute, item.endMinute, item.laneStart, item.laneWidth),
                    ),
                )
            }
        },
    ) { measurables, constraints ->
        val totalWidth = constraints.maxWidth
        val height = ((layout.endMinute - layout.startMinute) * pxPerMinute).toInt()

        data class Positioned(val placeable: androidx.compose.ui.layout.Placeable, val x: Int, val y: Int)
        val positioned = mutableListOf<Positioned>()

        measurables.forEach { m ->
            val tag = m.layoutId as DayEventTag
            val cardWidth = (totalWidth * tag.fracWidth).toInt() - 4
            val cardHeight = ((tag.endMin - tag.startMin) * pxPerMinute).toInt() - 2
            val placeable = m.measure(
                Constraints.fixed(
                    width = cardWidth.coerceAtLeast(8),
                    height = cardHeight.coerceAtLeast(8),
                ),
            )
            val x = (totalWidth * tag.fracStart).toInt() + 2
            val y = ((tag.startMin - layout.startMinute) * pxPerMinute).toInt() + 1
            positioned += Positioned(placeable, x = x, y = y)
        }

        layout(totalWidth, height) {
            positioned.forEach { it.placeable.place(it.x, it.y) }
        }
    }
}

private data class DayEventTag(val startMin: Int, val endMin: Int, val fracStart: Float, val fracWidth: Float)
