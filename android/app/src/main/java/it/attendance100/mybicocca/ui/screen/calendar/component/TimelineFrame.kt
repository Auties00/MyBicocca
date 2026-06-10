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

/** Base vertical scale at zoom 1: one dp per minute, so an hour spans 60dp. */
internal val TimelineMinuteHeightDefault = 1.0.dp
internal val TimelineWindowStart = TIMELINE_WINDOW_START_MINUTE
internal val TimelineWindowEnd = TIMELINE_WINDOW_END_MINUTE

internal const val TIMELINE_ZOOM_MIN = 0.55f
internal const val TIMELINE_ZOOM_MAX = 1.5f
internal const val TIMELINE_ZOOM_DEFAULT = 1.0f

private const val ODD_HOUR_ALPHA_START = 0.6f
private const val ODD_HOUR_ALPHA_END = 0.85f

/**
 * Alpha for odd-hour labels and grid lines: they fade in linearly as the minute height
 * grows through the [ODD_HOUR_ALPHA_START]..[ODD_HOUR_ALPHA_END] dp range (roughly 36 to
 * 51 dp per hour) — hidden when the timeline is zoomed out far enough that hourly
 * markings would crowd, fully visible once there is room for all of them.
 */
internal fun oddHourAlphaFor(minuteHeight: Dp): Float =
    ((minuteHeight.value - ODD_HOUR_ALPHA_START) / (ODD_HOUR_ALPHA_END - ODD_HOUR_ALPHA_START))
        .coerceIn(0f, 1f)

internal fun minuteHeightFor(zoom: Float): Dp =
    (TimelineMinuteHeightDefault.value * zoom).dp

/** Total grid height of a timeline [window] (exact start/end minutes) at the given scale. */
internal fun timelineHeightFor(
    minuteHeight: Dp,
    window: IntRange = TimelineWindowStart..TimelineWindowEnd,
): Dp = ((window.last - window.first) * minuteHeight.value).dp

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

/**
 * Hour-label gutter beside a timeline: one "HH" label per hour of [window], each vertically
 * centered on its grid line, with odd hours sharing the zoom-dependent fade of the grid.
 * The window must be the same one the adjacent events layer renders with, so the labels
 * land exactly on its grid lines.
 */
@Composable
internal fun HourGutterColumn(
    modifier: Modifier = Modifier,
    minuteHeight: Dp = TimelineMinuteHeightDefault,
    window: IntRange = TimelineWindowStart..TimelineWindowEnd,
) {
    val scheme = MaterialTheme.colorScheme
    val pxPerMinute = with(LocalDensity.current) { minuteHeight.toPx() }
    val totalHeight = timelineHeightFor(minuteHeight, window)
    val oddAlpha = oddHourAlphaFor(minuteHeight)
    Layout(
        modifier = modifier.width(TimelineGutterWidth),
        content = {
            val firstHour = window.first / 60
            val lastHour = window.last / 60
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
                val y = ((hour * 60 - window.first) * pxPerMinute).toInt() - placeable.height / 2
                placeable.place(
                    x = (width - placeable.width) / 2,
                    y = y.coerceAtLeast(0),
                )
            }
        }
    }
}

/**
 * Current-time marker: a horizontal line across the timeline with a filled dot anchored at
 * its leading edge, positioned against [windowStartMinute] — the same window start the
 * caller's event grid uses, so the line always sits on the true current minute.
 */
internal fun DrawScope.drawNowIndicator(
    nowMinute: Int,
    windowStartMinute: Int,
    pxPerMinute: Float,
    color: Color,
) {
    val y = (nowMinute - windowStartMinute) * pxPerMinute
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
