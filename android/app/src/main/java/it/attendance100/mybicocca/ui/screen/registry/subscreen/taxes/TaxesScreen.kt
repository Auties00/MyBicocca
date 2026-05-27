package it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.isee.IseeSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.component.TaxInvoiceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxesScreen(
    viewModel: TaxesViewModel,
    onOpenDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invoicesState by viewModel.invoices.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    var showIsee by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = syncStatus is SyncStatus.Refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
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
                    if (invoices.isEmpty()) {
                        Centered {
                            EmptyState(
                                icon = Icons.Outlined.ReceiptLong,
                                title = "Nessuna tassa",
                                body = "Non risultano fatture per la tua carriera.",
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            // Bottom inset clears the ISEE FAB.
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(items = invoices, key = { it.id.value }) { invoice ->
                                TaxInvoiceCard(
                                    invoice = invoice,
                                    onClick = { onOpenDetail(invoice.id.value) },
                                )
                            }
                        }
                    }
                }
            }
        }

        ExtendedFloatingActionButton(
            text = { Text("ISEE") },
            icon = { Icon(Icons.Outlined.Savings, contentDescription = null) },
            onClick = { showIsee = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (showIsee) {
        IseeSheet(viewModel = viewModel, onDismiss = { showIsee = false })
    }
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
