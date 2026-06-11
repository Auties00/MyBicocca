package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

private const val Y_MIN = 18f

/** Top of the scale: one step above 30 so 30 e lode plots above a plain 30. */
private const val Y_MAX = 31f

private val tooltipDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private data class GradePoint(
    val value: Float,
    val name: String,
    val date: String,
    val gradeText: String,
)

/**
 * Card-wrapped line chart of passed, graded exam results in chronological order on an
 * 18..30L scale: a primary-colored line over a vertical gradient fill, a dot on every
 * grade, and labeled reference grid lines at 18, 24, and 30L. Fewer than two points
 * renders an explanatory placeholder instead of a chart. Tapping near a point's
 * horizontal position selects it, drawing a donut highlight and anchoring a popup tooltip
 * with the exam name, date, and grade; taps on empty chart area select nothing, the
 * popup's outside-tap dismisses the tooltip, and the selection resets whenever the
 * dataset changes.
 */
@Composable
fun GradeTrendChart(
    rows: List<TranscriptRow>,
    modifier: Modifier = Modifier,
) {
    val points = remember(rows) {
        rows.asSequence()
            .filter { it.state == TranscriptRowState.Passed && it.grade != null && it.examDate != null }
            .sortedBy { it.examDate }
            .map {
                GradePoint(
                    value = if (it.cumLaude) Y_MAX else it.grade!!.toFloat(),
                    name = it.activityName,
                    date = it.examDate!!.format(tooltipDateFormatter),
                    gradeText = if (it.cumLaude) "30 e lode" else it.grade!!.toString(),
                )
            }
            .toList()
    }

    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (points.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.profile_trend_min_exams),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val lineColor = MaterialTheme.colorScheme.primary
                val fillColor = lineColor.copy(alpha = 0.18f)
                val gridColor = MaterialTheme.colorScheme.outlineVariant
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                val cardColor = MaterialTheme.colorScheme.surfaceContainerHigh
                val measurer = rememberTextMeasurer()
                val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

                val density = LocalDensity.current
                var chartSize by remember { mutableStateOf(IntSize.Zero) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                ) {
                    Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .onSizeChanged { chartSize = it }
                            .pointerInput(points) {
                                detectTapGestures { tap ->
                                    val leftPad = 30.dp.toPx()
                                    val chartWidth = size.width - leftPad
                                    fun xAt(index: Int): Float =
                                        if (points.size == 1) leftPad + chartWidth / 2f
                                        else leftPad + chartWidth * index / (points.size - 1)

                                    val nearest =
                                        points.indices.minByOrNull { abs(tap.x - xAt(it)) }
                                    selectedIndex =
                                        nearest?.takeIf { abs(tap.x - xAt(it)) <= 24.dp.toPx() }
                                }
                            },
                    ) {
                        val leftPad = 30.dp.toPx()
                        val vPad = 10.dp.toPx()
                        val chartWidth = size.width - leftPad
                        val chartHeight = size.height - vPad * 2

                        fun yFor(value: Float): Float =
                            vPad + chartHeight * (1f - (value - Y_MIN) / (Y_MAX - Y_MIN))
                        fun xFor(index: Int): Float =
                            if (points.size == 1) leftPad + chartWidth / 2f
                            else leftPad + chartWidth * index / (points.size - 1)

                        listOf(18f to "18", 24f to "24", Y_MAX to "30L").forEach { (value, label) ->
                            val y = yFor(value)
                            drawLine(
                                color = gridColor,
                                start = Offset(leftPad, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                            )
                            val measured = measurer.measure(label, labelStyle)
                            drawText(measured, topLeft = Offset(0f, y - measured.size.height / 2f))
                        }

                        val linePath = Path()
                        val fillPath = Path()
                        val baseline = yFor(Y_MIN)
                        points.forEachIndexed { index, point ->
                            val x = xFor(index)
                            val y = yFor(point.value)
                            if (index == 0) {
                                linePath.moveTo(x, y)
                                fillPath.moveTo(x, baseline)
                                fillPath.lineTo(x, y)
                            } else {
                                linePath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }
                        fillPath.lineTo(xFor(points.size - 1), baseline)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(fillColor, Color.Transparent),
                                startY = vPad,
                                endY = baseline,
                            ),
                        )
                        drawPath(
                            path = linePath,
                            color = lineColor,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                        points.forEachIndexed { index, point ->
                            drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = Offset(xFor(index), yFor(point.value)))
                        }

                        selectedIndex?.let { index ->
                            val px = xFor(index)
                            val py = yFor(points[index].value)
                            drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(px, py))
                            drawCircle(color = cardColor, radius = 3.dp.toPx(), center = Offset(px, py))
                        }
                    }

                    val index = selectedIndex
                    if (index != null && index in points.indices && chartSize != IntSize.Zero) {
                        val point = points[index]
                        val leftPad = with(density) { 30.dp.toPx() }
                        val vPad = with(density) { 10.dp.toPx() }
                        val chartWidth = chartSize.width - leftPad
                        val chartHeight = chartSize.height - vPad * 2
                        val pointX = if (points.size == 1) leftPad + chartWidth / 2f
                        else leftPad + chartWidth * index / (points.size - 1)
                        val pointY = vPad + chartHeight * (1f - (point.value - Y_MIN) / (Y_MAX - Y_MIN))
                        val gapPx = with(density) { 8.dp.roundToPx() }
                        val positionProvider = remember(pointX, pointY, chartSize, gapPx) {
                            GradeTooltipPositionProvider(
                                pointX = pointX.roundToInt(),
                                pointY = pointY.roundToInt(),
                                anchorWidth = chartSize.width,
                                gap = gapPx,
                            )
                        }

                        Popup(
                            popupPositionProvider = positionProvider,
                            onDismissRequest = { selectedIndex = null },
                            properties = PopupProperties(focusable = true),
                        ) {
                            GradeTooltip(
                                name = point.name,
                                date = point.date,
                                gradeText = point.gradeText,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Positions the grade tooltip centered above the selected point, clamped within the chart,
 * and flipped below the point when there is no room above. Works off the chart's window
 * origin plus the point's local pixel coordinates, since the zero-size popup anchor only
 * yields the chart's top-left.
 */
private class GradeTooltipPositionProvider(
    private val pointX: Int,
    private val pointY: Int,
    private val anchorWidth: Int,
    private val gap: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val originX = anchorBounds.left
        val originY = anchorBounds.top
        val maxX = (originX + anchorWidth - popupContentSize.width).coerceAtLeast(originX)
        val x = (originX + pointX - popupContentSize.width / 2).coerceIn(originX, maxX)
        val above = originY + pointY - gap - popupContentSize.height
        val y = if (above >= originY) above else originY + pointY + gap
        return IntOffset(x, y)
    }
}

@Composable
private fun GradeTooltip(name: String, date: String, gradeText: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.profile_grade_label, gradeText),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
