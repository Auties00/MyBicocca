package it.attendance100.mybicocca.ui.screen.calendar.component

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.ui.screen.calendar.state.TIMELINE_WINDOW_END_MINUTE
import it.attendance100.mybicocca.ui.screen.calendar.state.TIMELINE_WINDOW_START_MINUTE
import kotlin.math.abs

internal val TimelineGutterWidth = 38.dp
internal val TimelineMinuteHeightDefault = 1.0.dp        // 60dp per hour
internal val TimelineWindowStart = TIMELINE_WINDOW_START_MINUTE
internal val TimelineWindowEnd = TIMELINE_WINDOW_END_MINUTE

internal const val TIMELINE_ZOOM_MIN = 0.55f
internal const val TIMELINE_ZOOM_MAX = 1.5f
internal const val TIMELINE_ZOOM_DEFAULT = 1.0f

// Odd-hour labels and grid lines fade in as zoom increases through this range.
// Below START → α = 0 (hidden); above END → α = 1 (fully visible).
private const val ODD_HOUR_ALPHA_START = 0.6f   // zoom ≈ 0.6 → 36 dp/hr
private const val ODD_HOUR_ALPHA_END = 0.85f  // zoom ≈ 0.85 → 51 dp/hr

internal fun oddHourAlphaFor(minuteHeight: Dp): Float =
    ((minuteHeight.value - ODD_HOUR_ALPHA_START) / (ODD_HOUR_ALPHA_END - ODD_HOUR_ALPHA_START))
        .coerceIn(0f, 1f)

internal fun minuteHeightFor(zoom: Float): Dp =
    (TimelineMinuteHeightDefault.value * zoom).dp

internal fun timelineHeightFor(minuteHeight: Dp): Dp =
    ((TimelineWindowEnd - TimelineWindowStart) * minuteHeight.value).dp

/**
 * Detects a vertical two-finger pinch and calls [onZoom] with the new zoom level,
 * the viewport-relative focal Y coordinate, and the per-frame scale factor.
 */
internal fun Modifier.verticalPinchZoom(
    currentZoom: () -> Float,
    onZoom: (newZoom: Float, focalY: Float, factor: Float) -> Unit,
): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var prevVerticalSpan = 0f
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val down = event.changes.filter { it.pressed }
            if (down.size >= 2) {
                val p0 = down[0].position
                val p1 = down[1].position
                val currentSpan = abs(p1.y - p0.y)
                val focalY = (p0.y + p1.y) / 2f
                if (prevVerticalSpan > 1f) {
                    val factor = currentSpan / prevVerticalSpan
                    val newZoom =
                        (currentZoom() * factor).coerceIn(TIMELINE_ZOOM_MIN, TIMELINE_ZOOM_MAX)
                    onZoom(newZoom, focalY, factor)
                }
                prevVerticalSpan = currentSpan
                event.changes.forEach { it.consume() }
            } else {
                prevVerticalSpan = 0f
            }
        } while (event.changes.any { it.pressed })
    }
}

@Composable
internal fun HourGutterColumn(
    modifier: Modifier = Modifier,
    minuteHeight: Dp = TimelineMinuteHeightDefault,
) {
    val scheme = MaterialTheme.colorScheme
    val pxPerMinute = with(LocalDensity.current) { minuteHeight.toPx() }
    val totalHeight = timelineHeightFor(minuteHeight)
    val oddAlpha = oddHourAlphaFor(minuteHeight)
    Layout(
        modifier = modifier.width(TimelineGutterWidth),
        content = {
            val firstHour = TimelineWindowStart / 60
            val lastHour = TimelineWindowEnd / 60
            for (h in firstHour..lastHour) {
                Text(
                    text = "%02d".format(h % 24),
                    color = scheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = if (h % 2 != 0) {
                        Modifier
                            .layoutId(h)
                            .graphicsLayer { alpha = oddAlpha }
                    } else {
                        Modifier.layoutId(h)
                    },
                )
            }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        layout(width, totalHeight.toPx().toInt()) {
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
