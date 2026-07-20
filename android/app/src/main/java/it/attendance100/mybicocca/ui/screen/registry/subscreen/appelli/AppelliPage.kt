package it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli

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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.rememberHapticManager
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.ui.component.button.RetryButton
import it.attendance100.mybicocca.ui.component.feedback.friendlyMessage
import it.attendance100.mybicocca.ui.component.feedback.rememberMinDurationLoading
import it.attendance100.mybicocca.ui.component.modal.SheetLoadingIndicator
import it.attendance100.mybicocca.ui.component.modal.SheetMessage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetBodyGestureBarrier
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.component.BookedExamCard
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.ext.displayTitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.BookedEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.BookedFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.CancelActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.DocDownloadState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.state.groupByFilter
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appelli.subscreen.bookedExamDetail.BookedExamDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookableExamsViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.BookingSheetViewModel
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.CallPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.ConfirmPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.ExamCalendarPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.headerSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.rootSubtitle
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingActionState
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.BookingSheetStep
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.state.groupByCourse
import it.attendance100.mybicocca.ui.screen.registry.subscreen.booking.title
import it.attendance100.mybicocca.ui.screen.registry.subscreen.taxes.openPdfDocument
import it.attendance100.mybicocca.ui.theme.LocalIsOnline
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

/**
 * Appelli: the unified exams modal folding the student's prenotazioni and the Prenota esame
 * flow into one sheet. The root lists only the active prenotazioni, with a Prenota footer
 * that pushes the booking sub-flow — bookable calendar -> appello -> conferma — as deeper
 * pages of the same sheet; the booking detail, the cancel confirmation and the outcome
 * result page are further pages of the same pager, all beneath one pinned morphing header.
 * Body swipes scroll page content rather than the sheet: the header and the drag handle are
 * the only swipe-to-dismiss surfaces, and system back walks the pager up one level before
 * dismissing.
 *
 * BottomSheetSceneStrategy owns the sheet container; this composable owns the multi-level
 * page state machine. Three shell-scoped ViewModels feed it: [viewModel] for the booked
 * list (cancel, slip download), [bookableViewModel] for the bookable calendar — hit only
 * once the user actually enters the booking flow — and [sheetViewModel] for the booking
 * action. Because the booked VM outlives the sheet, a re-open renders the cached snapshot
 * instantly while a refresh runs; the root covers first-load failure (retry page),
 * minimum-duration loading, and the empty state. The detail page re-derives its booking
 * from the live list by identity key, so a refresh updates it in place and an optimistic
 * cancel drops the pager back to the list rather than rendering a stale snapshot. A pending
 * deep-link focus (from a libretto course) opens straight into the booking calendar.
 *
 * While a booking call is in flight the sheet locks via LocalSheetDismissControl — swipe,
 * scrim tap and back go inert so it can't be torn down mid-submit (the result page needs a
 * living sheet). A confirmed booking refreshes both lists and lands back on the root with a
 * success result page; a failed booking returns to the calendar with the error carried by
 * the result page.
 */
@Composable
fun AppelliPage(
    bookableViewModel: BookableExamsViewModel,
    viewModel: BookedExamsViewModel,
    sheetViewModel: BookingSheetViewModel = hiltViewModel(
        checkNotNull(
            LocalViewModelStoreOwner.current
        ) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        }, null
    ),
) {
    val strAppelliPdfOpenFailed = stringResource(R.string.appelli_pdf_open_failed)
    val strAppelliBookingCancelled = stringResource(R.string.appelli_booking_cancelled)
    val strAppelliBookingFailed = stringResource(R.string.appelli_booking_failed)
    val strAppelliBookingConfirmed = stringResource(R.string.appelli_booking_confirmed)
    val strAppelliCancellationFailed = stringResource(R.string.appelli_cancellation_failed)

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

    var detailKey by remember { mutableStateOf<String?>(null) }
    var confirmingCancel by remember { mutableStateOf(false) }
    var booking by remember { mutableStateOf(false) }
    var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
    val detailBooking = detailKey?.let { key -> bookings.firstOrNull { it.identityKey() == key } }

    LaunchedEffect(pendingFocus) { if (pendingFocus != null) booking = true }
    LaunchedEffect(booking) { if (booking) bookableViewModel.refresh() }

    // Opening a booking's detail lazily fetches its live numIscritti (see loadTotalBookings).
    val callTotals by viewModel.callTotals.collectAsStateWithLifecycle()
    LaunchedEffect(detailBooking?.key) { detailBooking?.let(viewModel::loadTotalBookings) }

    val page = when {
        outcome != null -> AppelliPage.Result
        detailBooking != null && confirmingCancel -> AppelliPage.ConfirmCancel
        detailBooking != null -> AppelliPage.Detail
        target != null && step == BookingSheetStep.Confirm -> AppelliPage.BookingConfirm
        target != null -> AppelliPage.BookingCall
        booking -> AppelliPage.BookingCalendar
        else -> AppelliPage.Root
    }

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
                        outcome =
                            SheetOutcome.Success(strAppelliBookingCancelled)
                    }

                    is BookedEvent.CancellationFailed ->
                        outcome = SheetOutcome.Error(
                            strAppelliCancellationFailed,
                            event.cause
                        )

                    is BookedEvent.OpenPdf ->
                        runCatching { openPdfDocument(context, event.bytes, event.fileName) }
                            .onFailure {
                                outcome = SheetOutcome.Error(
                                    strAppelliPdfOpenFailed,
                                    it
                                )
                            }

                    is BookedEvent.ShowMessage -> outcome = SheetOutcome.Info(event.message)
                }
            }
        }

        LaunchedEffect(sheetViewModel) {
            sheetViewModel.events.collectLatest { event ->
                when (event) {
                    BookingSheetEvent.BookedSuccessfully -> {
                        sheetViewModel.close()
                        booking = false
                        viewModel.refresh()
                        bookableViewModel.refresh()
                        outcome =
                            SheetOutcome.Success(strAppelliBookingConfirmed)
                    }

                    is BookingSheetEvent.BookingFailed -> {
                        sheetViewModel.close()
                        outcome = SheetOutcome.Error(
                            strAppelliBookingFailed,
                            event.cause
                        )
                    }
                }
            }
        }

        val seekableState =
            remember { androidx.compose.animation.core.SeekableTransitionState(page) }
        val transition = androidx.compose.animation.core.rememberTransition(
            seekableState,
            label = "appelli_pages"
        )

        LaunchedEffect(page) {
            if (seekableState.targetState != page) {
                seekableState.animateTo(page)
            }
        }

        androidx.activity.compose.PredictiveBackHandler(enabled = page != AppelliPage.Root) { progress ->
            try {
                val fallback = when (page) {
                    AppelliPage.Result -> if (detailBooking != null && confirmingCancel) AppelliPage.ConfirmCancel else if (detailBooking != null) AppelliPage.Detail else if (target != null && step == BookingSheetStep.Confirm) AppelliPage.BookingConfirm else if (target != null) AppelliPage.BookingCall else if (booking) AppelliPage.BookingCalendar else AppelliPage.Root
                    AppelliPage.ConfirmCancel -> AppelliPage.Detail
                    AppelliPage.Detail -> AppelliPage.Root
                    AppelliPage.BookingConfirm -> if (!submitting) AppelliPage.BookingCall else page
                    AppelliPage.BookingCall -> AppelliPage.BookingCalendar
                    AppelliPage.BookingCalendar -> AppelliPage.Root
                    AppelliPage.Root -> page
                }
                progress.collect { event ->
                    seekableState.seekTo(event.progress, targetState = fallback)
                }
                seekableState.animateTo(fallback)
                when (page) {
                    AppelliPage.Result -> outcome = null
                    AppelliPage.ConfirmCancel -> confirmingCancel = false
                    AppelliPage.Detail -> detailKey = null
                    AppelliPage.BookingConfirm -> if (!submitting) sheetViewModel.goBackToInfo()
                    AppelliPage.BookingCall -> sheetViewModel.close()
                    AppelliPage.BookingCalendar -> booking = false
                    AppelliPage.Root -> Unit
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                seekableState.animateTo(page)
            }
        }

        Column {
            SheetPagerHeader(
                depth = page.depth,
                title = when (page) {
                    AppelliPage.Root -> stringResource(R.string.appelli_title)
                    AppelliPage.Detail -> detailBooking?.displayTitle() ?: ""
                    AppelliPage.ConfirmCancel -> stringResource(R.string.appelli_cancel_confirmation_title)
                    AppelliPage.BookingCalendar -> stringResource(R.string.appelli_book_exam)
                    AppelliPage.BookingCall -> target?.call?.title() ?: ""
                    AppelliPage.BookingConfirm -> stringResource(R.string.appelli_confirm_booking)
                    AppelliPage.Result -> ""
                },
                subtitle = when (page) {
                    AppelliPage.Root -> if (loaded) activeSummary(active.size) else null
                    AppelliPage.Detail -> stringResource(R.string.appelli_booking_details)
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
            transition.AnimatedContent(
                modifier = Modifier.sheetBodyGestureBarrier(),
                transitionSpec = {
                    sheetPageTransform(forward = targetState.depth >= initialState.depth)
                },
                contentKey = { it.key },
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
                            // Fresh lazy fetch wins over the total persisted on the row.
                            totalBookings = callTotals[booked.key] ?: booked.totalBookings,
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

                    AppelliPage.BookingCall -> target?.let {
                        CallPage(
                            target = it,
                            onBook = sheetViewModel::goToConfirm
                        )
                    }

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

/** Pages of the appelli pager; [depth] orders the forward/backward page transition. */
private enum class AppelliPage(val depth: Int, val key: String) {
    Root(0, "root"),
    Detail(1, "detail"),
    ConfirmCancel(2, "confirm_cancel"),
    BookingCalendar(1, "booking_calendar"),
    Result(1, "result"),
    BookingCall(2, "booking_call"),
    BookingConfirm(3, "booking_confirm"),
}

/**
 * Root page: the active prenotazioni list with the Prenota footer pinned at the bottom. The
 * footer is the only way into the booking sub-flow, available the moment the list settles
 * without a hard failure — including when the list is empty, so a first booking can be
 * made. First-load failures render an error page with retry; loading holds a
 * minimum-duration indicator so quick fetches don't flash it.
 */
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
    val showLoading = rememberMinDurationLoading(loading = !loaded)
    val settled = loaded && !showLoading
    val haptic = rememberHapticManager()

    val motion = MaterialTheme.motionScheme
    val sizeSpec = remember(motion) { motion.defaultSpatialSpec<IntSize>() }

    Column(
        modifier = Modifier
            .testTag(AppelliTestTags.ROOT)
            .fillMaxWidth()
            .animateContentSize(animationSpec = sizeSpec),
    ) {
        when {
            failure != null && !loaded -> SheetMessage(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.appelli_load_failed),
                body = stringResource(failure.cause.friendlyMessage()),
                action = { RetryButton(onClick = { haptic.tap(); onRetry() }) },
                modifier = Modifier.testTag(AppelliTestTags.STATE_ERROR),
            )

            !settled -> SheetLoadingIndicator(
                label = stringResource(R.string.appelli_loading_bookings),
                modifier = Modifier.testTag(AppelliTestTags.STATE_LOADING),
            )

            active.isEmpty() -> SheetMessage(
                icon = Icons.Outlined.EventAvailable,
                title = stringResource(R.string.appelli_no_exams),
                body = stringResource(R.string.appelli_no_exams_body),
                modifier = Modifier.testTag(AppelliTestTags.STATE_EMPTY),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .testTag(AppelliTestTags.STATE_CONTENT)
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(items = active, key = { it.identityKey() }) { booking ->
                    BookedExamCard(
                        booking = booking,
                        onClick = { haptic.tap(); onOpenDetail(booking) },
                        modifier = Modifier
                            .testTag(AppelliTestTags.item(booking.identityKey()))
                            .animateItem(),
                    )
                }
            }
        }

        if (settled && failure == null) {
            PrenotaFooterButton(
                onClick = { haptic.tap(); onPrenota() },
                modifier = Modifier
                    .testTag(AppelliTestTags.PRENOTA_BUTTON)
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
            )
        }
    }
}

/** Footer CTA in the shared sheet idiom (cf. the biblioteca footer): brand fill, leading icon. */
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
        Icon(
            Icons.Outlined.EditCalendar,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(R.string.appelli_book),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Cancel-booking confirmation in the sheet's own page language: body copy and the connected
 * action pair — the safe Annulla trails wide on the brand fill (the recommended way out),
 * while the destructive Conferma leads smaller on the neutral tonal. Confirming returns to
 * the detail page, whose Cancella button carries the in-flight spinner; success pops the
 * pager to the root list.
 */
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
    val haptic = rememberHapticManager()

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.appelli_cancel_body, bookingTitle),
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
                onClick = { haptic.tap(); onConfirm() },
                enabled = LocalIsOnline.current,
                modifier = Modifier
                    .testTag(AppelliTestTags.CANCEL_CONFIRM)
                    .weight(1f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = scheme.surfaceContainerHighest,
                    contentColor = scheme.onSurface,
                ),
            ) {
                Text(stringResource(R.string.common_confirm), fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { haptic.tap(); onKeep() },
                modifier = Modifier
                    .testTag(AppelliTestTags.CANCEL_KEEP)
                    .weight(1.4f)
                    .height(56.dp),
                shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandBg,
                    contentColor = brandFg,
                ),
            ) {
                Text(stringResource(R.string.common_cancel), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun activeSummary(count: Int): String = when (count) {
    0 -> "Nessun esame in programma"
    1 -> "1 esame in programma"
    else -> "$count esami in programma"
}
