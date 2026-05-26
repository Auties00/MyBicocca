package it.attendance100.mybicocca.ui.screen.registry.subscreen.isee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.tax.IseeDeclaration
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.TaxesViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatEuro
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.taxFriendlyMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IseeScreen(viewModel: TaxesViewModel) {
    val iseeState by viewModel.isee.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = syncStatus is SyncStatus.Refreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val snapshot = iseeState) {
            Loadable.NotYetLoaded -> Centered {
                when (val status = syncStatus) {
                    is SyncStatus.Failed -> EmptyState(
                        icon = Icons.Outlined.CloudOff,
                        title = "Caricamento non riuscito",
                        body = status.cause.taxFriendlyMessage(),
                        action = { FilledTonalButton(onClick = viewModel::refresh) { Text("Riprova") } },
                    )
                    else -> CircularProgressIndicator()
                }
            }

            is Loadable.Loaded -> {
                val declarations = snapshot.value
                    .filter { it.isee != null }
                    .sortedByDescending { it.academicYearEnrollmentId ?: 0L }
                if (declarations.isEmpty()) {
                    Centered {
                        EmptyState(
                            icon = Icons.Outlined.Savings,
                            title = "ISEE non disponibile",
                            body = "Non risultano dichiarazioni ISEE per la tua carriera.",
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = declarations,
                            key = { it.academicYearEnrollmentId ?: it.hashCode().toLong() },
                        ) { IseeCard(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun IseeCard(declaration: IseeDeclaration) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            declaration.academicYearEnrollmentId?.let { year ->
                Text(
                    text = "Anno accademico $year/${(year + 1) % 100}",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = declaration.isee?.let { formatEuro(it) } ?: "—",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            declaration.iseeThreshold?.let { DetailRow("Soglia agevolazioni", formatEuro(it)) }
            declaration.courseDescription?.let { DetailRow("Corso", it) }
            declaration.exemptionDescription?.takeIf { it.isNotBlank() }?.let { DetailRow("Esonero", it) }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { content() }
        }
    }
}
