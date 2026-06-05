package it.attendance100.mybicocca.ui.screen.registry.subscreen.refunds

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.tax.Refund
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.formatEuro
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RefundDateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RefundsScreen(
    viewModel: RefundsViewModel = hiltViewModel(),
) {
    val data by viewModel.refunds.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val pullState = rememberPullToRefreshState()
    var pullIndicatorVisible by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = pullIndicatorVisible,
        onRefresh = {
            pullIndicatorVisible = true
            viewModel.pullToRefresh()
            scope.launch {
                delay(PULL_INDICATOR_DISMISS_DELAY_MS)
                pullIndicatorVisible = false
            }
        },
        state = pullState,
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val snapshot = data) {
            Loadable.NotYetLoaded -> when (val status = syncStatus) {
                is SyncStatus.Failed -> RefreshableEmpty {
                    ErrorEmptyState(cause = status.cause, onRetry = viewModel::refresh)
                }

                else -> RefreshableEmpty {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator(modifier = Modifier.size(72.dp))
                    }
                }
            }

            is Loadable.Loaded -> {
                val items = snapshot.value
                val failure = syncStatus as? SyncStatus.Failed
                when {
                    failure != null && items.isEmpty() -> RefreshableEmpty {
                        ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                    }

                    items.isEmpty() -> RefreshableEmpty {
                        EmptyState(
                            icon = Icons.Outlined.CurrencyExchange,
                            title = "Nessun rimborso",
                            body = "Non risultano rimborsi sulla tua carriera.",
                        )
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = items,
                            key = { it.invoiceId ?: it.hashCode().toLong() },
                        ) { refund -> RefundCard(refund) }
                    }
                }
            }
        }
    }
}

@Composable
private fun RefundCard(refund: Refund) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    refund.academicYear?.let { year ->
                        Text(
                            text = "Anno accademico $year/${(year + 1) % 100}",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = refund.amount?.let { formatEuro(it) } ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                    )
                }
                RefundStatusChip(refunded = refund.refunded)
            }
            refund.reasonCode?.let { DetailRow("Causale", it) }
            refund.mandateNumber?.let { DetailRow("Mandato", it) }
            refund.creditDate?.let { DetailRow("Accredito", it.format(RefundDateFormat)) }
            refund.paymentDate?.let { DetailRow("Pagamento", it.format(RefundDateFormat)) }
            refund.note?.let { DetailRow("Nota", it) }
        }
    }
}

@Composable
private fun RefundStatusChip(refunded: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val container = if (refunded) scheme.primary else scheme.surfaceContainerHighest
    val content = if (refunded) scheme.onPrimary else scheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .background(container, RoundedCornerShape(percent = 50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = if (refunded) "Rimborsato" else "In lavorazione",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
        )
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
private fun RefreshableEmpty(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { Box(Modifier.fillParentMaxSize()) { content() } }
    }
}

@Composable
private fun ErrorEmptyState(cause: Throwable, onRetry: () -> Unit) {
    EmptyState(
        icon = Icons.Outlined.CloudOff,
        title = "Caricamento non riuscito",
        body = cause.friendlyMessage(),
        action = { FilledTonalButton(onClick = onRetry) { Text("Riprova") } },
    )
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto. Riprova."
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
