package it.attendance100.mybicocca.ui.screen.registry.subscreen.exams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component.BookedExamCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.CancelActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookingSheetViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.BookingSectionTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.BookingSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.CourseCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.accentForCourse
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.groupByCourse
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
fun ExamsScreen(
    modifier: Modifier = Modifier,
    viewModel: ExamsViewModel = hiltViewModel(),
    sheetViewModel: BookingSheetViewModel = hiltViewModel(),
) {
    val callsData by viewModel.examCalls.collectAsStateWithLifecycle()
    val bookingsData by viewModel.bookings.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val cancelAction by viewModel.cancelAction.collectAsStateWithLifecycle()

    val sheetTarget by sheetViewModel.target.collectAsStateWithLifecycle()
    val sheetDetail by sheetViewModel.detail.collectAsStateWithLifecycle()
    val sheetSync by sheetViewModel.syncStatus.collectAsStateWithLifecycle()
    val sheetStep by sheetViewModel.step.collectAsStateWithLifecycle()
    val sheetAction by sheetViewModel.bookingAction.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()
    // Confirmation state is intentionally NOT saveable — letting it lapse across process
    // death is safer than restoring a destructive action prompt.
    var confirming by remember { mutableStateOf<BookedExam?>(null) }

    LaunchedEffect(sheetViewModel) {
        sheetViewModel.events.collectLatest { event ->
            when (event) {
                BookingSheetEvent.BookedSuccessfully -> {
                    sheetViewModel.close()
                    scope.launch { snackbar.showInfo("Prenotazione confermata") }
                    // The booked exam should move from "prenotabili" to "prenotati".
                    viewModel.refresh()
                }
                is BookingSheetEvent.BookingFailed -> {
                    scope.launch { snackbar.showError("Prenotazione non riuscita", event.cause) }
                }
            }
        }
    }

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

    val today = remember { LocalDate.now() }
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
        modifier = modifier.fillMaxSize(),
    ) {
        val calls = (callsData as? Loadable.Loaded)?.value
        val booked = (bookingsData as? Loadable.Loaded)?.value
        when {
            // Wait for both sources before rendering, so sections don't pop in separately.
            calls == null || booked == null -> when (val status = syncStatus) {
                is SyncStatus.Failed -> RefreshableEmpty {
                    ErrorEmptyState(cause = status.cause, onRetry = viewModel::refresh)
                }
                else -> RefreshableEmpty {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator(modifier = Modifier.size(72.dp))
                    }
                }
            }
            else -> {
                val bookedKeys = remember(booked) { booked.map { it.key }.toSet() }
                val bookable = remember(calls, bookedKeys) { calls.filterNot { it.key in bookedKeys } }
                val failure = syncStatus as? SyncStatus.Failed
                // Only surface a full-screen error when there is genuinely nothing to show;
                // otherwise render both sections, each with its own empty note if needed.
                if (failure != null && booked.isEmpty() && bookable.isEmpty()) {
                    RefreshableEmpty {
                        ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                    }
                } else {
                    ExamsContent(
                        booked = booked,
                        bookable = bookable,
                        today = today,
                        cancelAction = cancelAction,
                        onOpen = sheetViewModel::open,
                        onCancel = { confirming = it },
                    )
                }
            }
        }
    }

    val currentTarget = sheetTarget
    if (currentTarget != null) {
        BookingSheet(
            target = currentTarget,
            detail = sheetDetail,
            syncStatus = sheetSync,
            step = sheetStep,
            bookingAction = sheetAction,
            onRefresh = sheetViewModel::refresh,
            onGoToConfirm = sheetViewModel::goToConfirm,
            onGoBackToInfo = sheetViewModel::goBackToInfo,
            onConfirm = sheetViewModel::confirmBooking,
            onDismiss = sheetViewModel::close,
        )
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
private fun ExamsContent(
    booked: List<BookedExam>,
    bookable: List<ExamCall>,
    today: LocalDate,
    cancelAction: CancelActionState,
    onOpen: (ExamCall) -> Unit,
    onCancel: (BookedExam) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val groups = remember(bookable) { bookable.groupByCourse() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(key = "bookable_section") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                BookingSectionTitle(text = "Esami prenotabili")
                if (groups.isEmpty()) {
                    EmptySectionNote("Non ci sono appelli prenotabili al momento.")
                } else {
                    groups.forEach { group ->
                        val groupAccent = accentForCourse(courseKey = group.courseKey, scheme = scheme)
                        CourseCard(
                            group = group,
                            accent = groupAccent,
                            today = today,
                            onOpen = onOpen,
                        )
                    }
                }
            }
        }

        item(key = "booked_section") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BookingSectionTitle(text = "Esami prenotati")
                if (booked.isEmpty()) {
                    EmptySectionNote("Non hai prenotato nessun esame.")
                } else {
                    booked.forEach { booking ->
                        val cancelling = (cancelAction as? CancelActionState.InProgress)
                            ?.key == booking.identityKey()
                        BookedExamCard(
                            booking = booking,
                            isCancelling = cancelling,
                            onCancel = { onCancel(booking) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySectionNote(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
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
