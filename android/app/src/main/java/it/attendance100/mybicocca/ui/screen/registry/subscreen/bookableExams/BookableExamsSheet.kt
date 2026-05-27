package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.domain.model.exam.ExamCall
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookingSheetViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.BookingSheetContent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.component.CourseCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.accentForCourse
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.groupByCourse
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.LocalDate

// "Prenota un esame" modal — a single fixed-height sheet that swaps between the
// bookable list (scrollable, with its own header) and the booking detail/confirm
// flow. Fixing the height to the detail step keeps the sheet from resizing when
// drilling in. `bookedKeys` hides already-booked exams. Confirming keeps the sheet
// open with a spinner; the host (which outlives this composable) collects the
// booking result and closes the sheet + shows a snackbar, on success and failure.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookableExamsSheet(
    bookableViewModel: BookableExamsViewModel,
    sheetViewModel: BookingSheetViewModel,
    bookedKeys: Set<ExamCallKey>,
    onDismiss: () -> Unit,
) {
    val callsData by bookableViewModel.examCalls.collectAsStateWithLifecycle()
    val callsSync by bookableViewModel.syncStatus.collectAsStateWithLifecycle()

    val sheetTarget by sheetViewModel.target.collectAsStateWithLifecycle()
    val sheetDetail by sheetViewModel.detail.collectAsStateWithLifecycle()
    val sheetSync by sheetViewModel.syncStatus.collectAsStateWithLifecycle()
    val sheetStep by sheetViewModel.step.collectAsStateWithLifecycle()
    val sheetAction by sheetViewModel.bookingAction.collectAsStateWithLifecycle()

    val today = remember { LocalDate.now() }
    val isSubmitting = sheetAction is BookingActionState.InProgress

    // Reset the booking VM so reopening starts on the list, then let the host close.
    val dismiss = {
        sheetViewModel.close()
        onDismiss()
    }

    // Fixed sheet height so switching list <-> detail doesn't resize the sheet.
    val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.8f
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) dismiss() },
        sheetState = sheetState,
    ) {
        // Back from the detail step returns to the list rather than closing the sheet.
        BackHandler(enabled = sheetTarget != null && !isSubmitting) {
            sheetViewModel.close()
        }
        AnimatedContent(
            targetState = sheetTarget,
            transitionSpec = {
                fadeIn(animationSpec = effectsSpec)
                    .togetherWith(fadeOut(animationSpec = effectsSpec))
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight),
            label = "bookableStep",
        ) { target ->
            if (target == null) {
                BookableListStep(
                    callsData = callsData,
                    syncStatus = callsSync,
                    bookedKeys = bookedKeys,
                    today = today,
                    onOpen = sheetViewModel::open,
                    onRetry = bookableViewModel::refresh,
                )
            } else {
                BookingSheetContent(
                    target = target,
                    detail = sheetDetail,
                    syncStatus = sheetSync,
                    step = sheetStep,
                    bookingAction = sheetAction,
                    onBackToList = sheetViewModel::close,
                    onRefresh = sheetViewModel::refresh,
                    onGoToConfirm = sheetViewModel::goToConfirm,
                    onGoBackToInfo = sheetViewModel::goBackToInfo,
                    onConfirm = sheetViewModel::confirmBooking,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun BookableListStep(
    callsData: Loadable<List<ExamCall>>,
    syncStatus: SyncStatus,
    bookedKeys: Set<ExamCallKey>,
    today: LocalDate,
    onOpen: (ExamCall) -> Unit,
    onRetry: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Prenota un esame",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
        )
        val bodyModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        when (val snapshot = callsData) {
            Loadable.NotYetLoaded -> Box(bodyModifier, contentAlignment = Alignment.Center) {
                when (val status = syncStatus) {
                    is SyncStatus.Failed -> SheetError(cause = status.cause, onRetry = onRetry)
                    else -> CircularProgressIndicator(modifier = Modifier.size(40.dp))
                }
            }
            is Loadable.Loaded -> {
                val groups = remember(snapshot, bookedKeys) {
                    snapshot.value.filterNot { it.key in bookedKeys }.groupByCourse()
                }
                if (groups.isEmpty()) {
                    Box(
                        modifier = bodyModifier.padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Non ci sono appelli prenotabili al momento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = bodyModifier,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(items = groups, key = { it.courseKey }) { group ->
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
        }
    }
}

@Composable
private fun SheetError(cause: Throwable, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = cause.friendlyMessage(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(onClick = onRetry) { Text("Riprova") }
    }
}

private fun Throwable.friendlyMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "Rete non disponibile. Controlla la connessione e riprova."
    is SocketTimeoutException -> "Timeout di rete. Riprova tra un momento."
    is IOException -> "Errore di rete. Riprova tra un momento."
    else -> "Si è verificato un errore imprevisto. Riprova."
}
