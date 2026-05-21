package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.calendar.state.TIMELINE_WINDOW_END_MINUTE
import it.attendance100.mybicocca.ui.screen.calendar.state.TIMELINE_WINDOW_START_MINUTE

internal val TimelineGutterWidth = 38.dp
internal val TimelineMinuteHeight = 1.0.dp        // 60dp per hour
internal const val TimelineHourLabelStep = 2
internal val TimelineWindowStart = TIMELINE_WINDOW_START_MINUTE
internal val TimelineWindowEnd = TIMELINE_WINDOW_END_MINUTE
internal val TimelineHeight =
    ((TimelineWindowEnd - TimelineWindowStart) * TimelineMinuteHeight.value).dp

@Composable
internal fun HourGutterColumn(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val pxPerMinute = with(LocalDensity.current) { TimelineMinuteHeight.toPx() }
    Layout(
        modifier = modifier.width(TimelineGutterWidth),
        content = {
            val firstHour = TimelineWindowStart / 60
            val lastHour = TimelineWindowEnd / 60
            for (h in firstHour..lastHour) {
                if (h % TimelineHourLabelStep != 0) continue
                Text(
                    text = "%02d".format(h % 24),
                    color = scheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.layoutId(h),
                )
            }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        layout(width, TimelineHeight.toPx().toInt()) {
            measurables.forEach { m ->
                val hour = m.layoutId as Int
                val placeable = m.measure(Constraints())
                val y = ((hour * 60 - TimelineWindowStart) * pxPerMinute).toInt() - placeable.height / 2
                placeable.place(
                    x = (width - placeable.width) / 2,
                    y = y.coerceAtLeast(0),
                )
            }
        }
    }
}

internal fun DrawScope.drawNowIndicator(
    nowMinute: Int,
    pxPerMinute: Float,
    color: Color,
) {
    val y = (nowMinute - TimelineWindowStart) * pxPerMinute
    val thumbRadius = 4.dp.toPx()
    drawLine(
        color = color,
        strokeWidth = 2f,
        start = Offset(thumbRadius, y),
        end = Offset(size.width, y),
    )
    drawCircle(
        color = color,
        radius = thumbRadius,
        center = Offset(thumbRadius, y),
    )
}
