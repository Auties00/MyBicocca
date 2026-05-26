package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.IseeEntryCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.TaxInvoiceCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.TaxSummaryCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.TaxFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.label
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.state.matches

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxesScreen(
    viewModel: TaxesViewModel,
    onOpenDetail: (Long) -> Unit,
    onOpenIsee: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val invoicesState by viewModel.invoices.collectAsStateWithLifecycle()
    val summaryState by viewModel.summary.collectAsStateWithLifecycle()
    val iseeState by viewModel.isee.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf(TaxFilter.ALL) }

    PullToRefreshBox(
        isRefreshing = syncStatus is SyncStatus.Refreshing,
        onRefresh = viewModel::refresh,
        modifier = modifier.fillMaxSize(),
    ) {
        when (val snapshot = invoicesState) {
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
                val invoices = snapshot.value
                val shown = remember(filter, invoices) {
                    invoices.filter { filter.matches(it.status) }
                }
                val latestIsee = remember(iseeState) {
                    iseeState.valueOrNull()
                        ?.filter { it.isee != null }
                        ?.maxByOrNull { it.academicYearEnrollmentId ?: 0L }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    summaryState.valueOrNull()?.let { summary ->
                        item { TaxSummaryCard(summary) }
                    }
                    item { IseeEntryCard(declaration = latestIsee, onClick = onOpenIsee) }
                    item { TaxFilterRow(selected = filter, onSelect = { filter = it }) }

                    when {
                        invoices.isEmpty() -> item { InlineHint("Nessuna tassa registrata.") }
                        shown.isEmpty() -> item { InlineHint("Nessuna tassa per questo filtro.") }
                        else -> items(items = shown, key = { it.id.value }) { invoice ->
                            TaxInvoiceCard(invoice = invoice, onClick = { onOpenDetail(invoice.id.value) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaxFilterRow(selected: TaxFilter, onSelect: (TaxFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TaxFilter.entries.forEach { entry ->
            FilterChip(
                selected = entry == selected,
                onClick = { onSelect(entry) },
                label = { Text(entry.label) },
            )
        }
    }
}

@Composable
private fun InlineHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    )
}

// Centers content while remaining vertically scrollable so pull-to-refresh works.
@Composable
private fun Centered(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { content() }
        }
    }
}
