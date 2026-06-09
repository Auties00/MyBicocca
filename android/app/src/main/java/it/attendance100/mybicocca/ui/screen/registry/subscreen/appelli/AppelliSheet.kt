package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.PredictiveModalBottomSheet
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.component.BookedExamCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.ext.displayTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.BookedFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.CancelActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.DocDownloadState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booked.state.groupByFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.bookedExamDetail.BookedExamDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookingSheetViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.CallPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.ConfirmPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.ExamCalendarPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.headerSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.rootSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.title
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetStep
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.groupByCourse
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

// Appelli: the unified exams modal that folds Prenotazioni and Prenota esame into one sheet.
// The root lists the active prenotazioni (passate are no longer surfaced here) with a Prenota
// footer that pushes the booking sub-flow — bookable calendar -> appello -> conferma — as
// deeper pages of the SAME sheet, instead of a second modal. Three shell-scoped VMs feed it:
// the booked list (cancel, slip), the bookable calendar, and the booking action flow.
// Appelli as a single sheet entry: BottomSheetSceneStrategy owns the container; this keeps its
// own multi-level state machine (prenotazioni -> detail / cancel-confirm, plus the booking
// sub-flow) and morphing header. While a booking call is in flight it locks the sheet via the
// LocalSheetDismissControl so a swipe / scrim tap can't tear it down mid-submit.
@Composable
fun AppelliPage(
    bookableViewModel: BookableExamsViewModel,
    viewModel: BookedExamsViewModel,
    sheetViewModel: BookingSheetViewModel = hiltViewModel(),
) {
    val bookingsData by viewModel.bookings.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val cancelAction by viewModel.cancelAction.collectAsStateWithLifecycle()
    val docDownload by viewModel.docDownload.collectAsStateWithLifecycle()

    val callsData by bookableViewModel.examCalls.collectAsStateWithLifecycle()
    val callsSync by bookableViewModel.syncStatus.collectAsStateWithLifecycle()
    val pendingFocus by bookableViewModel.pendingFocusCourseKey.collectAsStateWithLifecycle()

    val target by sheetViewModel.target.collectAsStateWithLifecycle()
    val step by sheetViewModel.step.collectAsStateWithLifecycle()
    val action by sheetViewModel.bookingAction.collectAsStateWithLifecycle()
    val submitting = action is BookingActionState.InProgress

    // The booked VM outlives the sheet (shell-scoped): the first open is loaded by its own
    // career collector; a re-open shows the cached snapshot instantly while this refreshes.
    LaunchedEffect(viewModel) {
        if (viewModel.bookings.value is Loadable.Loaded) viewModel.refresh()
    }

    val loaded = bookingsData is Loadable.Loaded
    val bookings = bookingsData.valueOrNull().orEmpty()
    val today = remember { LocalDate.now() }
    val active = remember(bookings, today) {
        bookings.groupByFilter(today)[BookedFilter.Active].orEmpty()
    }

    val callsLoaded = callsData is Loadable.Loaded
    val callGroups = (callsData as? Loadable.Loaded)?.let { snapshot ->
        remember(snapshot) { snapshot.value.groupByCourse() }
    }

    // The detail booking is re-derived from the live list by identityKey: a refresh updates it
    // in place and an optimistic cancel drops the page back to the list, never a stale snapshot.
    var detailKey by remember { mutableStateOf<String?>(null) }
    var confirmingCancel by remember { mutableStateOf(false) }
    // True once the user steps into the booking sub-flow (the bookable calendar onward).
    var booking by remember { mutableStateOf(false) }
    // The outcome of a cancellation/booking/download, shown as a dedicated result page.
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    val detailBooking = detailKey?.let { key -> bookings.firstOrNull { it.identityKey() == key } }

    // A deep-link focus (from a libretto course) opens straight into the booking calendar.
    LaunchedEffect(pendingFocus) { if (pendingFocus != null) booking = true }
    // Only hit the bookable endpoint once the user actually enters the booking flow.
    LaunchedEffect(booking) { if (booking) bookableViewModel.refresh() }

    val page = when {
        outcome != null -> AppelliPage.Result
        detailBooking != null && confirmingCancel -> AppelliPage.ConfirmCancel
        detailBooking != null -> AppelliPage.Detail
        target != null && step == BookingSheetStep.Confirm -> AppelliPage.BookingConfirm
        target != null -> AppelliPage.BookingCall
        booking -> AppelliPage.BookingCalendar
        else -> AppelliPage.Root
    }

    // While a booking call is in flight the sheet stays up: no swipe / scrim tap dismisses it
    // (the outcome result page needs a living sheet). The back gesture goes inert via the
    // BackHandler below.
    val control = LocalSheetDismissControl.current
    SideEffect {
        control?.gesturesEnabled = !submitting
        control?.confirmDismiss = { !submitting }
    }

    run {
        val context = LocalContext.current

        LaunchedEffect(viewModel) {
            viewModel.events.collectLatest { event ->
                when (event) {
                    BookedEvent.CancellationSucceeded -> {
                        confirmingCancel = false
                        detailKey = null
                        outcome = SheetOutcome.Success("Prenotazione annullata")
                    }
                    is BookedEvent.CancellationFailed ->
                        outcome = SheetOutcome.Error("Annullamento non riuscito", event.cause)
                    is BookedEvent.OpenPdf ->
                        runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                            .onFailure { outcome = SheetOutcome.Error("Impossibile aprire il PDF", it) }
                    is BookedEvent.ShowMessage -> outcome = SheetOutcome.Info(event.message)
                }
            }
        }

        LaunchedEffect(sheetViewModel) {
            sheetViewModel.events.collectLatest { event ->
                when (event) {
                    // A confirmed booking walks back to the active list with the fresh booking
                    // in place — the sheet stays open, unlike the old standalone Prenota modal.
                    BookingSheetEvent.BookedSuccessfully -> {
                        sheetViewModel.close()
                        booking = false
                        viewModel.refresh()
                        bookableViewModel.refresh()
                        outcome = SheetOutcome.Success("Prenotazione confermata")
                    }
                    // Failure pops back to the calendar: the sheet stays up for a retry and its
                    // own result page carries the error.
                    is BookingSheetEvent.BookingFailed -> {
                        sheetViewModel.close()
                        outcome = SheetOutcome.Error("Prenotazione non riuscita", event.cause)
                    }
                }
            }
        }

        // System back walks the pager up one level before dismissing the sheet; it goes inert
        // while the booking call is in flight.
        BackHandler(enabled = page != AppelliPage.Root) {
            when (page) {
                AppelliPage.Result -> outcome = null
                AppelliPage.ConfirmCancel -> confirmingCancel = false
                AppelliPage.Detail -> detailKey = null
                AppelliPage.BookingConfirm -> if (!submitting) sheetViewModel.goBackToInfo()
                AppelliPage.BookingCall -> sheetViewModel.close()
                AppelliPage.BookingCalendar -> booking = false
                AppelliPage.Root -> Unit
            }
        }

        Column {
            SheetPagerHeader(
                depth = page.depth,
                title = when (page) {
                    AppelliPage.Root -> "Appelli"
                    AppelliPage.Detail -> detailBooking?.displayTitle() ?: ""
                    AppelliPage.ConfirmCancel -> "Annullare la prenotazione?"
                    AppelliPage.BookingCalendar -> "Prenota esame"
                    AppelliPage.BookingCall -> target?.call?.title() ?: ""
                    AppelliPage.BookingConfirm -> "Conferma prenotazione"
                    AppelliPage.Result -> ""
                },
                subtitle = when (page) {
                    AppelliPage.Root -> if (loaded) activeSummary(active.size) else null
                    AppelliPage.Detail -> "Dettagli appello"
                    AppelliPage.ConfirmCancel -> detailBooking?.displayTitle()
                    AppelliPage.BookingCalendar -> callGroups?.let { rootSubtitle(it) }
                    AppelliPage.BookingCall -> target?.call?.headerSubtitle()
                    AppelliPage.BookingConfirm -> target?.call?.title()
                    AppelliPage.Result -> null
                },
                onBack = when (page) {
                    AppelliPage.Root -> null
                    AppelliPage.Detail -> ({ detailKey = null })
                    AppelliPage.ConfirmCancel -> ({ confirmingCancel = false })
                    AppelliPage.BookingCalendar -> ({ booking = false })
                    AppelliPage.BookingCall -> ({ sheetViewModel.close() })
                    AppelliPage.BookingConfirm -> if (submitting) null else ({ sheetViewModel.goBackToInfo() })
                    AppelliPage.Result -> null
                },
            )
            AnimatedContent(
                targetState = page,
                // Swipes on the pages scroll their content, never the sheet: the header above
                // (and the drag handle) is the only swipe-to-dismiss surface.
                modifier = Modifier.sheetBodyGestureBarrier(),
                transitionSpec = {
                    sheetPageTransform(forward = targetState.depth >= initialState.depth)
                },
                contentKey = { it.key },
                label = "appelli_pages",
            ) { current ->
                when (current) {
                    AppelliPage.Root -> ActiveBody(
                        loaded = loaded,
                        active = active,
                        syncStatus = syncStatus,
                        onRetry = viewModel::refresh,
                        onOpenDetail = { detailKey = it.identityKey() },
                        onPrenota = { booking = true },
                    )

                    AppelliPage.Detail -> detailBooking?.let { booked ->
                        val downloading = (docDownload as? DocDownloadState.InProgress)
                            ?.takeIf { it.bookingKey == booked.identityKey() }
                            ?.document
                        BookedExamDetailPage(
                            booking = booked,
                            today = today,
                            isCancelling = (cancelAction as? CancelActionState.InProgress)
                                ?.key == booked.identityKey(),
                            downloadingDocument = downloading,
                            onRequestCancel = { confirmingCancel = true },
                            onDownloadSlip = viewModel::downloadBookingSlip,
                        )
                    }

                    AppelliPage.ConfirmCancel -> detailBooking?.let { booked ->
                        CancelConfirmPage(
                            bookingTitle = booked.displayTitle(),
                            onKeep = { confirmingCancel = false },
                            onConfirm = {
                                // Back to the detail, whose Cancella button carries the in-flight
                                // spinner; success pops the pager to the list.
                                confirmingCancel = false
                                viewModel.cancel(booked)
                            },
                        )
                    }

                    AppelliPage.BookingCalendar -> ExamCalendarPage(
                        loaded = callsLoaded,
                        groups = callGroups,
                        syncStatus = callsSync,
                        pendingFocus = pendingFocus,
                        onConsumeFocus = bookableViewModel::consumeFocus,
                        onRetry = bookableViewModel::refresh,
                        onOpenCall = sheetViewModel::open,
                    )

                    AppelliPage.BookingCall -> target?.let { CallPage(target = it, onBook = sheetViewModel::goToConfirm) }

                    AppelliPage.BookingConfirm -> ConfirmPage(
                        submitting = submitting,
                        onConfirm = sheetViewModel::confirmBooking,
                    )

                    AppelliPage.Result -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }
                }
            }
        }
    }
}

private enum class AppelliPage(val depth: Int, val key: String) {
    Root(0, "root"),
    Detail(1, "detail"),
    ConfirmCancel(2, "confirm_cancel"),
    BookingCalendar(1, "booking_calendar"),
    Result(1, "result"),
    BookingCall(2, "booking_call"),
    BookingConfirm(3, "booking_confirm"),
}

// The active prenotazioni list with the Prenota footer pinned at the bottom. The footer is
// the only way into the booking sub-flow, available the moment the list settles without a
// hard failure — including when the list is empty, so a first booking can be made.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ActiveBody(
    loaded: Boolean,
    active: List<BookedExam>,
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    onOpenDetail: (BookedExam) -> Unit,
    onPrenota: () -> Unit,
) {
    val failure = syncStatus as? SyncStatus.Failed
    // Hold the loading state for a beat so quick fetches don't flash it.
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && !loaded -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = "Caricamento non riuscito",
                body = failure.cause.friendlyMessage(),
                action = { RetryButton(onClick = onRetry) },
            )

            !settled -> SheetLoadingIndicator(label = "Caricamento prenotazioni…")

            active.isEmpty() -> SheetMessage(
                icon = Icons.Outlined.EventAvailable,
                title = "Nessun esame in programma",
                body = "Quando prenoti un appello lo trovi qui. Premi Prenota per iniziare.",
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = active, key = { it.identityKey() }) { booking ->
                    BookedExamCard(
                        booking = booking,
                        onClick = { onOpenDetail(booking) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }

        if (settled && failure == null) {
            PrenotaFooterButton(
                onClick = onPrenota,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
            )
        }
    }
}

// Footer CTA in the shared sheet idiom (cf. the biblioteca footer): brand fill, leading icon.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrenotaFooterButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (dark) scheme.primaryContainer else scheme.primary,
            contentColor = if (dark) scheme.onPrimaryContainer else scheme.onPrimary,
        ),
    ) {
        Icon(Icons.Outlined.EditCalendar, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Prenota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

// Confirm page in the sheet's own language, replacing the old AlertDialog: body copy and the
// connected action pair — the safe Annulla trails wide on the brand fill (the recommended way
// out), while the destructive Conferma leads smaller on the neutral tonal.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CancelConfirmPage(
    bookingTitle: String,
    onKeep: () -> Unit,
    onConfirm: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val brandBg = if (dark) scheme.primaryContainer else scheme.primary
    val brandFg = if (dark) scheme.onPrimaryContainer else scheme.onPrimary

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Stai per annullare la prenotazione a $bookingTitle. " +
                    "Potrai prenotarti di nuovo finché le iscrizioni restano aperte.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilledTonalButton(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Text("Conferma", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onKeep,
                modifier = Modifier
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
                ),
            ) {
                Text("Annulla", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun activeSummary(count: Int): String = when (count) {
    0 -> "Nessun esame in programma"
    1 -> "1 esame in programma"
    else -> "$count esami in programma"
}
