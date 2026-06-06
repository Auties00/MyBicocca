package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component.BookedExamCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.CancelActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.DocDownloadState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExamDetail.BookedExamDetailSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookedExamsScreen(
    bookedViewModel: BookedExamsViewModel,
    modifier: Modifier = Modifier,
) {
    val bookingsData by bookedViewModel.bookings.collectAsStateWithLifecycle()
    val bookedSync by bookedViewModel.syncStatus.collectAsStateWithLifecycle()
    val cancelAction by bookedViewModel.cancelAction.collectAsStateWithLifecycle()
    val docDownload by bookedViewModel.docDownload.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Tapping a card opens its detail sheet; identified by identityKey so the sheet
    // survives refreshes and disappears with the booking once a cancellation lands.
    var detailKey by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(bookedViewModel) {
        bookedViewModel.events.collectLatest { event ->
            when (event) {
                BookedEvent.CancellationSucceeded -> {
                    detailKey = null
                    scope.launch { snackbar.showInfo("Prenotazione annullata") }
                }
                is BookedEvent.CancellationFailed -> scope.launch {
                    snackbar.showError("Annullamento non riuscito", event.cause)
                }
                is BookedEvent.OpenPdf -> scope.launch {
                    runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                        .onFailure { snackbar.showError("Impossibile aprire il PDF", it) }
                }
                is BookedEvent.ShowMessage -> scope.launch { snackbar.showInfo(event.message) }
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    var pullIndicatorVisible by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = pullIndicatorVisible,
            onRefresh = {
                pullIndicatorVisible = true
                bookedViewModel.pullToRefresh()
                scope.launch {
                    delay(PULL_INDICATOR_DISMISS_DELAY_MS)
                    pullIndicatorVisible = false
                }
            },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val snapshot = bookingsData) {
                Loadable.NotYetLoaded -> when (val status = bookedSync) {
                    is SyncStatus.Failed -> RefreshableEmpty {
                        ErrorEmptyState(cause = status.cause, onRetry = bookedViewModel::refresh)
                    }
                    else -> RefreshableEmpty {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LoadingIndicator(modifier = Modifier.size(72.dp))
                        }
                    }
                }
                is Loadable.Loaded -> {
                    val booked = snapshot.value
                    val failure = bookedSync as? SyncStatus.Failed
                    when {
                        // Failed sync but we have cached data
                        failure != null && booked.isEmpty() -> RefreshableEmpty {
                            ErrorEmptyState(cause = failure.cause, onRetry = bookedViewModel::refresh)
                        }
                        // Sync successful but no exams booked
                        booked.isEmpty() -> RefreshableEmpty {
                            EmptyState(
                                icon = Icons.Outlined.EventAvailable,
                                title = "Nessun esame prenotato",
                                body = "Quando prenoti un appello lo trovi qui. Prenotane uno dalla sezione Prenota esame.",
                            )
                        }
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(items = booked, key = { it.identityKey() }) { booking ->
                                BookedExamCard(
                                    booking = booking,
                                    onClick = { detailKey = booking.identityKey() },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // The sheet's booking is re-derived from the live list: a refresh updates it in
    // place, and the optimistic removal after a cancellation dismisses it.
    val detailBooking = detailKey?.let { key ->
        (bookingsData as? Loadable.Loaded)?.value?.firstOrNull { it.identityKey() == key }
    }
    if (detailBooking != null) {
        val today = remember { LocalDate.now() }
        BookedExamDetailSheet(
            booking = detailBooking,
            today = today,
            isCancelling = (cancelAction as? CancelActionState.InProgress)?.key == detailBooking.identityKey(),
            downloadingDocument = (docDownload as? DocDownloadState.InProgress)
                ?.takeIf { it.bookingKey == detailBooking.identityKey() }
                ?.document,
            onCancel = bookedViewModel::cancel,
            onDownloadSlip = bookedViewModel::downloadBookingSlip,
            onDownloadCertificate = bookedViewModel::downloadPresenceCertificate,
            onDismiss = { detailKey = null },
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
    else -> "Si è verificato un errore imprevisto"
}

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
