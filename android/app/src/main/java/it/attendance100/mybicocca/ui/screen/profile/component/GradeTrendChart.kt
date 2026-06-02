package it.attendance100.mybicocca.ui.screen.profile.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRowState

// Grades run 18..30, with 30L (cum laude) one step above 30 as the top of the scale.
private const val Y_MIN = 18f
private const val Y_MAX = 31f

private data class GradePoint(val value: Float)

// Line chart of accepted (passed, graded) exam grades in chronological order, on a 0..30L scale.
@Composable
fun GradeTrendChart(
    rows: List<TranscriptRow>,
    modifier: Modifier = Modifier,
) {
    val points = remember(rows) {
        rows.asSequence()
            .filter { it.state == TranscriptRowState.Passed && it.grade != null && it.examDate != null }
            .sortedBy { it.examDate }
            .map { GradePoint(if (it.cumLaude) Y_MAX else it.grade!!.toFloat()) }
            .toList()
    }

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
                        text = "Andamento disponibile dopo almeno due esami superati",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val lineColor = MaterialTheme.colorScheme.primary
                val fillColor = lineColor.copy(alpha = 0.18f)
                val gridColor = MaterialTheme.colorScheme.outlineVariant
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                val measurer = rememberTextMeasurer()
                val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
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

                    // Reference lines + Y labels across the 18..30L scale.
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
                }
            }
        }
    }
}
