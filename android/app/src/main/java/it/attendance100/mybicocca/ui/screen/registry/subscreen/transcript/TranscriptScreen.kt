package it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.transcript.TranscriptRow
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.component.ExamCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.component.ExpandableHeader
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.component.GradesChart
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.state.ChartPoint
import it.attendance100.mybicocca.ui.screen.registry.subscreen.transcript.state.TranscriptPartition

@Composable
fun TranscriptScreen(
    careerId: Long,
    modifier: Modifier = Modifier,
    viewModel: TranscriptViewModel = hiltViewModel(),
) {
    val partitionLoadable by viewModel.partition.collectAsStateWithLifecycle()
    val chartLoadable by viewModel.chartSeries.collectAsStateWithLifecycle()

    val partition: TranscriptPartition = partitionLoadable.valueOrNull() ?: TranscriptPartition(emptyList(), emptyList())
    val chartPoints: List<ChartPoint> = chartLoadable.valueOrNull().orEmpty()

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (partitionLoadable !is Loadable.Loaded) {
            item { Text("Caricamento libretto…", style = MaterialTheme.typography.bodyMedium) }
        }

        if (chartPoints.isNotEmpty()) {
            item {
                Text(
                    text = "Grafico Voti",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                GradesChart(points = chartPoints, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
            }
        }

        item {
            ExpandableHeader(
                title = "Esami Sostenuti",
                count = partition.passed.size,
                initiallyExpanded = true,
            ) {
                ExamList(rows = partition.passed)
            }
        }

        item {
            ExpandableHeader(
                title = "Esami in Sospeso",
                count = partition.pending.size,
                initiallyExpanded = false,
            ) {
                ExamList(rows = partition.pending)
            }
        }
    }
}

@Composable
private fun ExamList(rows: List<TranscriptRow>) {
    if (rows.isEmpty()) {
        Text(
            text = "Nessun esame.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        return
    }
    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            // Stable identity prevents recomposition churn when the surrounding list reorders.
            androidx.compose.runtime.key(row.id) {
                ExamCard(row = row)
            }
        }
    }
}
