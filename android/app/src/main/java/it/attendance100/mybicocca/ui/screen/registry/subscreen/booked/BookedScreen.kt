package it.attendance100.mybicocca.ui.screen.registry.subscreen.booked

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component.BookedExamCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.CancelActionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookedScreen(
    viewModel: BookedViewModel = hiltViewModel(),
) {
    val data by viewModel.bookings.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val cancelAction by viewModel.cancelAction.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    // Confirmation state is intentionally NOT saveable — letting it lapse across process
    // death is safer than restoring a destructive action prompt.
    var confirming by remember { mutableStateOf<BookedExam?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                BookedEvent.CancellationSucceeded -> scope.launch {
                    snackbar.showInfo("Prenotazione annullata")
                }
                is BookedEvent.CancellationFailed -> scope.launch {
                    snackbar.showError("Annullamento non riuscito", event.cause)
                }
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    // Pull-to-refresh indicator stays only for the dismiss-animation window on a user
    // pull; cold loads (incl. post-pull invalidation) use the in-page LoadingIndicator.
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
                val bookings = snapshot.value
                val failure = syncStatus as? SyncStatus.Failed
                when {
                    failure != null && bookings.isEmpty() -> RefreshableEmpty {
                        ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                    }
                    bookings.isEmpty() -> RefreshableEmpty {
                        EmptyState(
                            icon = Icons.Outlined.EventAvailable,
                            title = "Nessuna prenotazione attiva",
                            body = "Non risulti iscritto a nessun appello in programma.",
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(items = bookings, key = { it.identityKey() }) { booking ->
                            val cancelling = (cancelAction as? CancelActionState.InProgress)
                                ?.key == booking.identityKey()
                            BookedExamCard(
                                booking = booking,
                                isCancelling = cancelling,
                                onCancel = { confirming = booking },
                            )
                        }
                    }
                }
            }
        }
    }

    val pending = confirming
    if (pending != null) {
        AlertDialog(
            onDismissRequest = { confirming = null },
            title = { Text("Annullare la prenotazione?") },
            text = {
                Text(
                    "Stai per annullare la prenotazione a " +
                        (pending.activityDescription?.takeIf { it.isNotBlank() } ?: "questo appello") +
                        ". L'operazione non può essere ripristinata."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancel(pending)
                        confirming = null
                    },
                ) { Text("Annulla prenotazione") }
            },
            dismissButton = {
                TextButton(onClick = { confirming = null }) { Text("Indietro") }
            },
        )
    }
}

@Composable
private fun RefreshableEmpty(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize()) { content() }
        }
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
