package it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.state.ChartPoint

@Composable
fun GradesChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    // Stable signature key — rebuild the model only when the underlying data set actually
    // changes. Recompositions for unrelated reasons (theme tweaks, parent state) skip work.
    val signature = remember(points) { points.signature() }
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(signature) {
        if (points.isEmpty()) {
            modelProducer.runTransaction { lineSeries { series(0f) } }
        } else {
            modelProducer.runTransaction { lineSeries { series(points.map { it.y }) } }
        }
    }

    val yMin = remember(signature) { points.minOfOrNull { it.y }?.let { (it.toInt() - 1).coerceAtLeast(17).toFloat() } ?: 18f }
    val yMax = remember(signature) { points.maxOfOrNull { it.y }?.coerceAtLeast(31f) ?: 31f }

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    CartesianChartHost(
        modifier = modifier.fillMaxWidth().height(220.dp),
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(primary)),
                    ),
                ),
                rangeProvider = CartesianLayerRangeProvider.fixed(minY = yMin.toDouble(), maxY = yMax.toDouble()),
            ),
            endAxis = VerticalAxis.rememberEnd(
                label = rememberAxisLabelComponent(color = onSurface),
                valueFormatter = { _, value, _ ->
                    if (value.toInt() == 31) "30L" else value.toInt().toString()
                },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(label = null),
        ),
        modelProducer = modelProducer,
        zoomState = rememberVicoZoomState(zoomEnabled = false, initialZoom = Zoom.fixed(1f)),
        scrollState = rememberVicoScrollState(),
    )
}

private fun List<ChartPoint>.signature(): Int {
    // Cheap fingerprint: size + first/last id. Avoids re-running expensive equality checks.
    if (isEmpty()) return 0
    return size * 31 + first().rowId.hashCode() + last().rowId.hashCode()
}
