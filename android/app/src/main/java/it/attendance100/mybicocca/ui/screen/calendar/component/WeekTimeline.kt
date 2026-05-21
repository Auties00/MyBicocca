package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.screen.calendar.ext.rememberCurrentTime
import it.attendance100.mybicocca.ui.screen.calendar.ext.visibleWeekDays
import it.attendance100.mybicocca.ui.screen.calendar.state.layoutDay
import java.time.LocalDate

@Composable
fun WeekEventsLayer(
    weekStart: LocalDate,
    selectedDay: LocalDate?,
    eventsByDay: Map<LocalDate, List<CalendarEvent>>,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val days = remember(weekStart) { visibleWeekDays(weekStart) }
    val now by rememberCurrentTime()
    val today = now.toLocalDate()
    val pxPerMinute = with(LocalDensity.current) { TimelineMinuteHeight.toPx() }
    val perDay = remember(days, eventsByDay) {
        days.associateWith { layoutDay(eventsByDay[it].orEmpty()) }
    }
    val gridColor = scheme.outline.copy(alpha = 0.5f)
    val nowColor = scheme.primary

    Layout(
        modifier = modifier
            .fillMaxWidth()
            .height(TimelineHeight)
            .drawBehind {
                val width = size.width
                val firstLineMinute = ((TimelineWindowStart + 59) / 60) * 60
                var minute = firstLineMinute
                while (minute <= TimelineWindowEnd) {
                    val y = (minute - TimelineWindowStart) * pxPerMinute
                    if (((minute / 60) % TimelineHourLabelStep) == 0) {
                        drawLine(
                            color = gridColor,
                            strokeWidth = 1f,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                        )
                    }
                    minute += 60
                }
                if (today in days) {
                    val nm = now.toLocalTime().let { it.hour * 60 + it.minute }
                    if (nm in TimelineWindowStart..TimelineWindowEnd) {
                        drawNowIndicator(nm, pxPerMinute, nowColor)
                    }
                }
            },
        content = {
            days.forEachIndexed { idx, day ->
                val tint: Color = if (day == selectedDay) {
                    scheme.primaryContainer.copy(alpha = 0.33f)
                } else {
                    Color.Transparent
                }
                if (tint != Color.Transparent) {
                    Box(
                        modifier = Modifier
                            .background(tint)
                            .layoutId(WeekColumnTag(idx)),
                    )
                }
            }
            days.forEachIndexed { idx, day ->
                val layout = perDay[day] ?: return@forEachIndexed
                layout.items.forEach { item ->
                    EventCard(
                        event = item.event,
                        onClick = { onEventClick(item.event) },
                        modifier = Modifier.layoutId(
                            WeekEventTag(idx, item.startMinute, item.endMinute, item.laneStart, item.laneWidth),
                        ),
                    )
                }
            }
        },
    ) { measurables, constraints ->
        val totalWidth = constraints.maxWidth
        val columnWidth = totalWidth / days.size
        val height = ((TimelineWindowEnd - TimelineWindowStart) * pxPerMinute).toInt()

        data class Positioned(val placeable: androidx.compose.ui.layout.Placeable, val x: Int, val y: Int)
        val positioned = mutableListOf<Positioned>()

        measurables.forEach { m ->
            when (val tag = m.layoutId) {
                is WeekColumnTag -> {
                    val p = m.measure(Constraints.fixed(columnWidth.coerceAtLeast(1), height))
                    positioned += Positioned(p, tag.dayIndex * columnWidth, 0)
                }
                is WeekEventTag -> {
                    val cardWidth = (columnWidth * tag.fracWidth).toInt() - 2
                    val cardHeight = ((tag.endMin - tag.startMin) * pxPerMinute).toInt() - 2
                    val p = m.measure(
                        Constraints.fixed(
                            width = cardWidth.coerceAtLeast(6),
                            height = cardHeight.coerceAtLeast(8),
                        ),
                    )
                    val x = tag.dayIndex * columnWidth + (columnWidth * tag.fracStart).toInt() + 1
                    val y = ((tag.startMin - TimelineWindowStart) * pxPerMinute).toInt() + 1
                    positioned += Positioned(p, x, y)
                }
            }
        }

        layout(totalWidth, height) {
            positioned.forEach { it.placeable.place(it.x, it.y) }
        }
    }
}

private data class WeekColumnTag(val dayIndex: Int)
private data class WeekEventTag(
    val dayIndex: Int,
    val startMin: Int,
    val endMin: Int,
    val fracStart: Float,
    val fracWidth: Float,
)
