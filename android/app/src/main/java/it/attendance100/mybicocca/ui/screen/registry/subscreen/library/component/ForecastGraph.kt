package it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.domain.model.library.LibraryForecastPoint
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.occupancyColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val HourFormat = DateTimeFormatter.ofPattern("HH")

// Today's hourly occupancy curve as a row of bars, each colored by how busy that hour is. The
// bar nearest "now" is emphasized so the current moment stands out.
@Composable
fun ForecastGraph(
    points: List<LibraryForecastPoint>,
    modifier: Modifier = Modifier,
    now: LocalTime = LocalTime.now(),
) {
    if (points.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    val maxValue = (points.maxOf { it.occupancyPercentage }).coerceAtLeast(1)
    // The bar covering the current hour, to highlight "now".
    val currentIndex = points.indexOfLast { it.time <= now }.takeIf { it >= 0 }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            points.forEachIndexed { index, point ->
                val fraction = (point.occupancyPercentage.toFloat() / maxValue).coerceIn(0.04f, 1f)
                val isNow = index == currentIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(
                                occupancyColor(point.occupancyPercentage)
                                    .copy(alpha = if (isNow) 1f else 0.55f),
                            ),
                    )
                }
            }
        }
        // Hour ticks under every other bar to keep the axis readable.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            points.forEachIndexed { index, point ->
                Text(
                    text = if (index % 2 == 0) point.time.format(HourFormat) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
