package it.attendance100.mybicocca.ui.screen.registry.subscreen.bookableExams

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
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
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingTarget
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.accentForCourse
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.groupByCourse
import kotlinx.coroutines.CancellationException
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    var isClosing by remember { mutableStateOf(false) }

    // Reset the booking VM so reopening starts on the list, then let the host close.
    val dismiss = {
        sheetViewModel.close()
        onDismiss()
    }

    // Fixed sheet height so switching list <-> detail doesn't resize the sheet.
    val sheetHeight = LocalConfiguration.current.screenHeightDp.dp * 0.67f // sessantasette
    val sheetTargetState = androidx.compose.runtime.rememberUpdatedState(sheetTarget)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newState ->
            !(sheetTargetState.value != null && newState == SheetValue.Hidden)
        }
    )

    // Retains the list's scroll position and expanded cards across the detail trip. The holder lives
    // outside the AnimatedContent, so the list's saveable state survives while its slot is removed.
    val listStateHolder = rememberSaveableStateHolder()

    // Drives the list <-> detail slide AND the close slide.
    val stepState = remember { SeekableTransitionState<BookingTarget?>(null) }
    val stepTransition = rememberTransition(stepState, label = "bookableStep")

    // Expressive spring motion for the list <-> detail step change.
    val motion = MaterialTheme.motionScheme
    androidx.compose.runtime.LaunchedEffect(sheetTarget) {
        if (stepState.targetState != sheetTarget) {
            stepState.animateTo(sheetTarget, motion.slowSpatialSpec())
        }
    }

    val closeSeekableState = remember { SeekableTransitionState(true) }
    val closeTransition = rememberTransition(closeSeekableState, label = "closeTransition")

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSubmitting) {
                if (sheetTarget != null) {
                    sheetViewModel.close()
                } else {
                    dismiss()
                }
            }
        },
        contentWindowInsets = { WindowInsets(0) },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
    ) {
        // Back from the detail step scrubs it back down to the list rather than closing the sheet.
        PredictiveBackHandler(enabled = sheetTarget != null && !isClosing && !isSubmitting) { progress ->
            try {
                progress.collect { backEvent ->
                    stepState.seekTo(backEvent.progress, targetState = null)
                }
                sheetViewModel.close()
                stepState.animateTo(null)
            } catch (_: CancellationException) {
                stepState.animateTo(sheetTarget)
            }
        }

        // Back from the list scrubs it down to closed.
        PredictiveBackHandler(enabled = sheetTarget == null && !isClosing && !isSubmitting && sheetState.isVisible) { progress ->
            try {
                progress.collect { backEvent ->
                    closeSeekableState.seekTo(backEvent.progress, targetState = false)
                }
                isClosing = true
                closeSeekableState.animateTo(false)
                dismiss()
            } catch (_: CancellationException) {
                closeSeekableState.animateTo(true)
            }
        }

        closeTransition.AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = fadeIn(motion.defaultEffectsSpec()),
                    initialContentExit = fadeOut(motion.defaultEffectsSpec()),
                    sizeTransform = SizeTransform(clip = true) { _, _ ->
                        motion.defaultSpatialSpec()
                    },
                )
            },
            contentKey = { it }
        ) { isVisible ->
            if (isVisible) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp
                            )
                        )
                ) {
                    stepTransition.AnimatedContent(
                        modifier = Modifier.fillMaxWidth(),
                        transitionSpec = {
                            val isForward = targetState != null
                            val enter = slideInVertically(motion.defaultSpatialSpec()) { h ->
                                if (isForward) h else -h
                            } + fadeIn(motion.defaultEffectsSpec(), initialAlpha = 0.2f)

                            val exit = slideOutVertically(motion.defaultSpatialSpec()) { h ->
                                if (isForward) -h else h
                            } + fadeOut(motion.defaultEffectsSpec(), targetAlpha = 0.2f)

                            ContentTransform(
                                targetContentEnter = enter,
                                initialContentExit = exit,
                                sizeTransform = SizeTransform(clip = true) { _, _ ->
                                    motion.defaultSpatialSpec()
                                },
                            )
                        },
                        contentAlignment = Alignment.BottomCenter,
                        contentKey = { it },
                    ) { bookingDetail ->
                        if (bookingDetail != null) {
                            BookingSheetContent(
                                target = bookingDetail,
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
                        // The list keeps its tall height; the detail (below) sizes to its own content.
                        else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(sheetHeight)
                            ) {
                                listStateHolder.SaveableStateProvider("bookable-list") {
                                    BookableListStep(
                                        callsData = callsData,
                                        syncStatus = callsSync,
                                        bookedKeys = bookedKeys,
                                        today = today,
                                        onOpen = sheetViewModel::open,
                                        onRetry = bookableViewModel::refresh,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val loadedGroups = (callsData as? Loadable.Loaded)?.let { snapshot ->
        remember(snapshot, bookedKeys) {
            snapshot.value.filterNot { it.key in bookedKeys }.groupByCourse()
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
        ) {
            Text(
                text = "Prenota un esame",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = scheme.onSurface,
            )
            loadedGroups?.sumOf { it.calls.size }?.takeIf { it > 0 }?.let { count ->
                Text(
                    text = if (count == 1) "1 appello disponibile" else "$count appelli disponibili",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        val bodyModifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        when (callsData) {
            Loadable.NotYetLoaded -> Box(bodyModifier, contentAlignment = Alignment.Center) {
                when (val status = syncStatus) {
                    is SyncStatus.Failed -> SheetError(cause = status.cause, onRetry = onRetry)
                    else -> LoadingIndicator(modifier = Modifier.size(56.dp))
                }
            }

            is Loadable.Loaded -> {
                val groups = loadedGroups.orEmpty()
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
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 28.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(items = groups, key = { it.courseKey }) { group ->
                            val groupAccent =
                                accentForCourse(courseKey = group.courseKey, scheme = scheme)
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
