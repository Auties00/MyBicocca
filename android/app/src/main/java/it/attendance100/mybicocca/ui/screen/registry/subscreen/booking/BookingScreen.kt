package it.attendance100.mybicocca.ui.screen.registry.subscreen.booking

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.BookingSectionTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.BookingSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.CourseCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.UrgentTile
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.accentForCourse
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.groupByCourse
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.imminent
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
fun BookingScreen(
    viewModel: BookingViewModel = hiltViewModel(),
    sheetViewModel: BookingSheetViewModel = hiltViewModel(),
) {
    val data by viewModel.examCalls.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    val sheetTarget by sheetViewModel.target.collectAsStateWithLifecycle()
    val sheetDetail by sheetViewModel.detail.collectAsStateWithLifecycle()
    val sheetSync by sheetViewModel.syncStatus.collectAsStateWithLifecycle()
    val sheetStep by sheetViewModel.step.collectAsStateWithLifecycle()
    val sheetAction by sheetViewModel.bookingAction.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(sheetViewModel) {
        sheetViewModel.events.collectLatest { event ->
            when (event) {
                BookingSheetEvent.BookedSuccessfully -> {
                    sheetViewModel.close()
                    scope.launch { snackbar.showInfo("Prenotazione confermata") }
                    viewModel.refresh()
                }
                is BookingSheetEvent.BookingFailed -> {
                    scope.launch { snackbar.showError("Prenotazione non riuscita", event.cause) }
                }
            }
        }
    }

    val today = remember { LocalDate.now() }
    val pullState = rememberPullToRefreshState()
    // Pull-to-refresh indicator stays only for the dismiss-animation window on a user
    // pull; cold loads use the shapes LoadingIndicator instead.
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
                val calls = snapshot.value
                val failure = syncStatus as? SyncStatus.Failed
                when {
                    failure != null && calls.isEmpty() -> RefreshableEmpty {
                        ErrorEmptyState(cause = failure.cause, onRetry = viewModel::refresh)
                    }
                    calls.isEmpty() -> RefreshableEmpty {
                        EmptyState(
                            icon = Icons.Outlined.CalendarToday,
                            title = "Nessun appello disponibile",
                            body = "Al momento non risultano appelli prenotabili per la tua carriera.",
                        )
                    }
                    else -> BookingContent(
                        calls = calls,
                        today = today,
                        onOpen = sheetViewModel::open,
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
}

@Composable
private fun BookingContent(
    calls: List<ExamCall>,
    today: LocalDate,
    onOpen: (ExamCall) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val imminentCalls = remember(calls, today) { calls.imminent(today) }
    val groups = remember(calls) { calls.groupByCourse() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (imminentCalls.isNotEmpty()) {
            item(key = "imminent_section") {
                ImminentSection(
                    calls = imminentCalls,
                    today = today,
                    scheme = scheme,
                    onOpen = onOpen,
                )
            }
        }

        groups.forEach { group ->
            item(key = "course_${group.courseKey}") {
                val groupAccent = accentForCourse(courseKey = group.courseKey, scheme = scheme)
                CourseCard(
                    group = group,
                    accent = groupAccent,
                    today = today,
                    onOpen = onOpen,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ImminentSection(
    calls: List<ExamCall>,
    today: LocalDate,
    scheme: ColorScheme,
    onOpen: (ExamCall) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BookingSectionTitle(
            text = "Imminenti",
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            calls.forEach { call ->
                val callAccent = accentForCourse(
                    courseKey = call.activityCode
                        ?: call.activityDescription
                        ?: call.key.activityId.toString(),
                    scheme = scheme,
                )
                UrgentTile(
                    call = call,
                    accent = callAccent,
                    today = today,
                    onClick = { onOpen(call) },
                )
            }
        }
    }
}

@Composable
private fun RefreshableEmpty(content: @Composable () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Box(Modifier.fillParentMaxSize()) {
                content()
            }
        }
    }
}

@Composable
private fun ErrorEmptyState(
    cause: Throwable,
    onRetry: () -> Unit,
) {
    EmptyState(
        icon = Icons.Outlined.CloudOff,
        title = "Caricamento non riuscito",
        body = cause.friendlyMessage(),
        action = {
            FilledTonalButton(onClick = onRetry) { Text("Riprova") }
        },
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
