package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component.BookedExamCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookingSheetViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookedExamsScreen(
    bookedViewModel: BookedExamsViewModel,
    bookableViewModel: BookableExamsViewModel,
    onOpenExam: (courseOfStudyId: Long, activityId: Long, callId: Int) -> Unit,
    modifier: Modifier = Modifier,
    sheetViewModel: BookingSheetViewModel = hiltViewModel(),
) {
    val bookingsData by bookedViewModel.bookings.collectAsStateWithLifecycle()
    val bookedSync by bookedViewModel.syncStatus.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    var showBookable by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(bookedViewModel) {
        bookedViewModel.events.collectLatest { event ->
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

    // Booking outcome is handled here (not in the modal) so the sheet can close on both
    // success and failure while the snackbar/refresh still run from a living scope.
    LaunchedEffect(sheetViewModel) {
        sheetViewModel.events.collectLatest { event ->
            showBookable = false
            sheetViewModel.close()
            when (event) {
                BookingSheetEvent.BookedSuccessfully -> {
                    scope.launch { snackbar.showInfo("Prenotazione confermata") }
                    bookedViewModel.refresh()
                    bookableViewModel.refresh()
                }
                is BookingSheetEvent.BookingFailed -> scope.launch {
                    snackbar.showError("Prenotazione non riuscita", event.cause)
                }
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
                                body = "Quando prenoti un appello lo trovi qui. Usa il pulsante in basso per prenotarne uno.",
                            )
                        }
                        // Display list of booked exams
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item(key = "header_title") {
                                Text(
                                    text = "Iscrizioni Appelli",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                            }
                            items(items = booked, key = { it.identityKey() }) { booking ->
                                BookedExamCard(
                                    booking = booking,
                                    onClick = {
                                        onOpenExam(
                                            booking.key.courseOfStudyId,
                                            booking.key.activityId,
                                            booking.key.callId
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (bookingsData is Loadable.Loaded) {
            ExtendedFloatingActionButton(
                onClick = { showBookable = true },
                icon = { Icon(Icons.Outlined.EditCalendar, contentDescription = null) },
                text = { Text("Prenota un esame") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            )
        }
    }

    if (showBookable) {
        val bookedKeys = remember(bookingsData) {
            (bookingsData as? Loadable.Loaded)?.value?.map { it.key }?.toSet() ?: emptySet()
        }
        BookableExamsSheet(
            bookableViewModel = bookableViewModel,
            sheetViewModel = sheetViewModel,
            bookedKeys = bookedKeys,
            onDismiss = { showBookable = false },
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
