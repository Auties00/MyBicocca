package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.ui.component.feedback.EmptyState
import it.attendance100.mybicocca.ui.component.feedback.LocalAppSnackbarController
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookingSheetViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.BookingSheetContent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingCourseGroup
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.groupByCourse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// "Prenota esame" sub-page — the bookable exam calls grouped by course, rendered in the
// StudyPlan visual idiom (connected segmented tile groups). Tapping an appello
// opens the booking detail/confirm modal (BookingSheetContent), whose result is collected
// here so the sheet can close + the list refresh on both success and failure.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookableExamsScreen(
    bookableViewModel: BookableExamsViewModel,
    sheetViewModel: BookingSheetViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val callsData by bookableViewModel.examCalls.collectAsStateWithLifecycle()
    val callsSync by bookableViewModel.syncStatus.collectAsStateWithLifecycle()
    val pendingFocus by bookableViewModel.pendingFocusCourseKey.collectAsStateWithLifecycle()

    val sheetTarget by sheetViewModel.target.collectAsStateWithLifecycle()
    val sheetDetail by sheetViewModel.detail.collectAsStateWithLifecycle()
    val sheetSync by sheetViewModel.syncStatus.collectAsStateWithLifecycle()
    val sheetStep by sheetViewModel.step.collectAsStateWithLifecycle()
    val sheetAction by sheetViewModel.bookingAction.collectAsStateWithLifecycle()

    val snackbar = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    // Booking outcome is handled here (not in the modal) so the sheet can close on both
    // success and failure while the snackbar/refresh still run from a living scope.
    LaunchedEffect(sheetViewModel) {
        sheetViewModel.events.collectLatest { event ->
            sheetViewModel.close()
            when (event) {
                BookingSheetEvent.BookedSuccessfully -> {
                    scope.launch { snackbar.showInfo("Prenotazione confermata") }
                    bookableViewModel.refresh()
                }
                is BookingSheetEvent.BookingFailed -> scope.launch {
                    snackbar.showError("Prenotazione non riuscita", event.cause)
                }
            }
        }
    }

    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()
    var pullIndicatorVisible by remember { mutableStateOf(false) }

    val loadedGroups = (callsData as? Loadable.Loaded)?.let { snapshot ->
        remember(snapshot) { snapshot.value.groupByCourse() }
    }

    // Deep-link from the libretto course sheet: once loaded, animate-scroll to the matching
    // course group's header item, then consume the request (whether or not it was found, so we
    // don't keep re-scrolling).
    LaunchedEffect(pendingFocus, loadedGroups) {
        val groups = loadedGroups ?: return@LaunchedEffect
        val key = pendingFocus ?: return@LaunchedEffect
        val index = groups.indexOfFirst { it.courseKey == key }
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
        bookableViewModel.consumeFocus()
    }

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = pullIndicatorVisible,
            onRefresh = {
                pullIndicatorVisible = true
                bookableViewModel.refresh()
                scope.launch {
                    delay(PULL_INDICATOR_DISMISS_DELAY_MS)
                    pullIndicatorVisible = false
                }
            },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val snapshot = callsData) {
                Loadable.NotYetLoaded -> when (val status = callsSync) {
                    is SyncStatus.Failed -> RefreshableEmpty {
                        ErrorEmptyState(cause = status.cause, onRetry = bookableViewModel::refresh)
                    }

                    else -> RefreshableEmpty {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LoadingIndicator(modifier = Modifier.size(72.dp))
                        }
                    }
                }

                is Loadable.Loaded -> {
                    val groups = loadedGroups.orEmpty()
                    val failure = callsSync as? SyncStatus.Failed
                    when {
                        failure != null && groups.isEmpty() -> RefreshableEmpty {
                            ErrorEmptyState(cause = failure.cause, onRetry = bookableViewModel::refresh)
                        }

                        groups.isEmpty() -> RefreshableEmpty {
                            EmptyState(
                                icon = Icons.Outlined.EventAvailable,
                                title = "Nessun appello disponibile",
                                body = "Non ci sono appelli prenotabili al momento.",
                            )
                        }

                        else -> {
                            val today = remember { LocalDate.now() }
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                groups.forEach { group ->
                                    item(key = group.courseKey) {
                                        CourseGroupSection(
                                            group = group,
                                            today = today,
                                            onOpen = sheetViewModel::open,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (sheetTarget != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val isSubmitting = sheetAction is BookingActionState.InProgress
        val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.67f
        ModalBottomSheet(
            onDismissRequest = { if (!isSubmitting) sheetViewModel.close() },
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0) },
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        ) {
            BookingSheetContent(
                target = sheetTarget!!,
                detail = sheetDetail,
                syncStatus = sheetSync,
                step = sheetStep,
                bookingAction = sheetAction,
                onBackToList = sheetViewModel::close,
                onRefresh = sheetViewModel::refresh,
                onGoToConfirm = sheetViewModel::goToConfirm,
                onGoBackToInfo = sheetViewModel::goBackToInfo,
                onConfirm = sheetViewModel::confirmBooking,
                maxHeight = sheetHeight,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// One course rendered like the StudyPlan year sections: a header tile and connected
// appello sub-tiles with 2.dp seams.
@Composable
private fun CourseGroupSection(
    group: BookingCourseGroup,
    today: LocalDate,
    onOpen: (ExamCall) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        YearHeaderTile(
            title = group.courseTitle,
            subtitle = if (group.calls.size == 1) "1 appello" else "${group.calls.size} appelli",
        )
        group.calls.forEachIndexed { index, call ->
            AppelloTile(
                call = call,
                today = today,
                isLast = index == group.calls.lastIndex,
                onClick = { onOpen(call) },
            )
        }
    }
}

@Composable
private fun YearHeaderTile(
    title: String,
    subtitle: String,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp,
        ),
    ) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AppelloTile(
    call: ExamCall,
    today: LocalDate,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val large = 20.dp
    val small = 4.dp
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = scheme.surfaceContainer,
        contentColor = scheme.onSurface,
        shape = RoundedCornerShape(
            topStart = small,
            topEnd = small,
            bottomStart = if (isLast) large else small,
            bottomEnd = if (isLast) large else small,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DateBox(date = call.callDate)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = call.callDate?.format(LongDateFormat)
                        ?.replaceFirstChar { it.titlecase(Locale.ITALIAN) }
                        ?: "Data da definire",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = buildList {
                    call.callTime?.let { add("ore ${it.format(TimeFormat)}") }
                    call.callDescription?.takeIf { it.isNotBlank() }?.let { add(it) }
                        ?: call.stateDescription?.takeIf { it.isNotBlank() }?.let { add(it) }
                }.joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                EnrollmentWindowLabel(
                    opens = call.enrollmentWindow.opensAt,
                    closes = call.enrollmentWindow.closesAt,
                    today = today,
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun DateBox(date: LocalDate?) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(scheme.primaryContainer, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        if (date == null) {
            Text(
                text = "—",
                color = scheme.onPrimaryContainer,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${date.dayOfMonth}",
                    color = scheme.onPrimaryContainer,
                    fontSize = 16.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = date.format(MonthFormat).uppercase(Locale.ITALIAN),
                    color = scheme.onPrimaryContainer.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

// Compact enrollment-window hint mirroring the modal's window chip semantics.
@Composable
private fun EnrollmentWindowLabel(
    opens: LocalDate?,
    closes: LocalDate?,
    today: LocalDate,
) {
    val scheme = MaterialTheme.colorScheme
    val label = remember(opens, closes, today) { windowLabel(opens, closes, today) } ?: return
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = scheme.onSurfaceVariant,
        maxLines = 1,
    )
}

private fun windowLabel(opens: LocalDate?, closes: LocalDate?, today: LocalDate): String? {
    if (opens == null && closes == null) return null
    if (closes != null && closes.isBefore(today)) return "Iscrizioni chiuse"
    if (opens != null && opens.isAfter(today)) {
        return "Iscrizioni dal ${opens.format(WindowFormat)}"
    }
    if (closes != null) return "Iscrizioni fino al ${closes.format(WindowFormat)}"
    return null
}

// Wraps full-screen empty/loading/error content in a scrollable container so the
// pull-to-refresh gesture still works when there's nothing else to scroll.
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

private val LongDateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN)
private val MonthFormat = DateTimeFormatter.ofPattern("MMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val WindowFormat = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ITALIAN)

private const val PULL_INDICATOR_DISMISS_DELAY_MS = 350L
