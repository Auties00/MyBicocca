package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.domain.model.library.isBookableAt
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component.QrScannerScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.ConfirmPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.DateTimePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.HomePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibrariesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryDonePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryReservationDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LoginPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.SeatsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.ZonesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryPage
import java.time.format.DateTimeFormatter
import java.util.Locale

private val RecapDateFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * The whole Biblioteca experience — Affluences seat booking for the university libraries — as a
 * single bottom-sheet entry. The sheet container is owned by the navigation layer
 * (BottomSheetSceneStrategy); this composable keeps its own VM-driven in-sheet pager and morphing
 * [SheetPagerHeader]. Bookings are server-synced (Room-cached); the user logs in by validating
 * their institutional email.
 *
 * The pager renders the ViewModel's back stack (home, then login / reservation detail / the
 * libraries list, then library detail and the booking wizard: zones, date & time, seats, confirm,
 * done), with two sheet-local overlays stacked on top: a [SheetResultPage] for one-shot operation
 * outcomes and a [SheetConfirmPage] guarding reservation cancellation.
 *
 * Behavior:
 * - System back dismisses the overlays first, then pops the stack; it is blocked on the Done page
 *   (the booking is committed) and while a submission is in flight.
 * - The email-sent login event surfaces no result page: the login page advances to its
 *   check-your-email state through the login phase stream.
 * - A reservation-detail page pops itself when a cancellation or sync drops its reservation.
 * - "Verifica presenza" opens a full-screen QR scanner with a manual-code fallback.
 */
@Composable
fun LibraryPage(
    viewModel: LibraryViewModel,
) {
    DisposableEffect(Unit) {
        onDispose { viewModel.resetNavigation() }
    }

    run {
        var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
        var pendingCancel by remember { mutableStateOf<LibraryReservation?>(null) }

        val backStack by viewModel.backStack.collectAsStateWithLifecycle()
        val libraries by viewModel.libraries.collectAsStateWithLifecycle()
        val librariesStatus by viewModel.librariesStatus.collectAsStateWithLifecycle()
        val reservations by viewModel.reservations.collectAsStateWithLifecycle()
        val cancellingId by viewModel.cancellingId.collectAsStateWithLifecycle()
        val linkedEmail by viewModel.linkedEmail.collectAsStateWithLifecycle()
        val institutionalEmail by viewModel.institutionalEmail.collectAsStateWithLifecycle()
        val loginPhase by viewModel.loginPhase.collectAsStateWithLifecycle()
        val loginFeedback by viewModel.loginFeedback.collectAsStateWithLifecycle()

        val liveStatus by viewModel.liveStatus.collectAsStateWithLifecycle()
        val weekHours by viewModel.weekHours.collectAsStateWithLifecycle()
        val detailStatus by viewModel.detailStatus.collectAsStateWithLifecycle()

        val bookingLibrary by viewModel.bookingLibrary.collectAsStateWithLifecycle()
        val zones by viewModel.zones.collectAsStateWithLifecycle()
        val zonesStatus by viewModel.zonesStatus.collectAsStateWithLifecycle()
        val agreements by viewModel.agreements.collectAsStateWithLifecycle()
        val selectedZone by viewModel.selectedZone.collectAsStateWithLifecycle()
        val constraints by viewModel.constraints.collectAsStateWithLifecycle()
        val constraintsStatus by viewModel.constraintsStatus.collectAsStateWithLifecycle()
        val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
        val selectedDuration by viewModel.selectedDuration.collectAsStateWithLifecycle()
        val seats by viewModel.seats.collectAsStateWithLifecycle()
        val seatsStatus by viewModel.seatsStatus.collectAsStateWithLifecycle()
        val availableStartTimes by viewModel.availableStartTimes.collectAsStateWithLifecycle()
        val selectedStartTime by viewModel.selectedStartTime.collectAsStateWithLifecycle()
        val selectedSeat by viewModel.selectedSeat.collectAsStateWithLifecycle()
        val note by viewModel.note.collectAsStateWithLifecycle()
        val consentAccepted by viewModel.consentAccepted.collectAsStateWithLifecycle()
        val submitting by viewModel.submitting.collectAsStateWithLifecycle()

        var showScanner by remember { mutableStateOf(false) }

        val current = backStack.last()
        val depth = backStack.lastIndex
        val reservationList = reservations.valueOrNull().orEmpty()
        val libraryList = libraries.valueOrNull().orEmpty()
        val mandatoryAgreement = remember(agreements) { agreements.firstOrNull { it.mandatory } }
        val email = institutionalEmail.orEmpty()

        val context = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    LibraryEvent.ReservationCancelled -> outcome =
                        SheetOutcome.Success(context.getString(R.string.library_reservation_cancelled))

                    is LibraryEvent.CancelFailed -> outcome = SheetOutcome.Error(
                        context.getString(R.string.library_cancellation_failed),
                        event.cause
                    )

                    is LibraryEvent.BookingFailed -> outcome = SheetOutcome.Error(
                        context.getString(R.string.library_booking_failed),
                        event.cause
                    )
                    is LibraryEvent.LoginEmailSent -> Unit
                    is LibraryEvent.LoginRequestFailed -> outcome = SheetOutcome.Error(
                        context.getString(R.string.library_send_failed),
                        event.cause
                    )

                    is LibraryEvent.LoginFailed -> outcome = SheetOutcome.Error(
                        context.getString(R.string.library_login_failed),
                        event.cause
                    )

                    is LibraryEvent.SyncFailed -> outcome = SheetOutcome.Error(
                        context.getString(R.string.library_sync_failed),
                        event.cause
                    )

                    LibraryEvent.PresenceVerified -> outcome =
                        SheetOutcome.Success(context.getString(R.string.library_presence_verified))
                    LibraryEvent.PresenceInvalidCode ->
                        outcome = SheetOutcome.Error(
                            context.getString(R.string.library_invalid_code),
                            body = context.getString(R.string.library_invalid_code_body)
                        )

                    is LibraryEvent.PresenceFailed -> outcome = SheetOutcome.Error(
                        context.getString(R.string.library_presence_failed),
                        event.cause
                    )
                }
            }
        }

        val detailReservation: LibraryReservation? = (current as? LibraryPage.ReservationDetail)
            ?.let { page -> reservationList.firstOrNull { it.reservationId == page.reservationId } }
        LaunchedEffect(current, reservationList) {
            if (current is LibraryPage.ReservationDetail && detailReservation == null) viewModel.back()
        }

        val onDonePage = current is LibraryPage.Done
        val display: LibraryDisplay = when {
            outcome != null -> LibraryDisplay.Outcome
            pendingCancel != null -> LibraryDisplay.ConfirmCancel
            else -> LibraryDisplay.Page(current)
        }

        BackHandler(enabled = onDonePage && display is LibraryDisplay.Page) { }

        val seekableState =
            remember { androidx.compose.animation.core.SeekableTransitionState(display) }
        val transition = androidx.compose.animation.core.rememberTransition(
            seekableState,
            label = "library_sheet_pages"
        )

        LaunchedEffect(display) {
            if (seekableState.targetState != display) {
                seekableState.animateTo(display)
            }
        }

        androidx.activity.compose.PredictiveBackHandler(
            enabled = display !is LibraryDisplay.Page || (depth > 0 && !submitting && !onDonePage),
        ) { progress ->
            try {
                val fallback = when (display) {
                    LibraryDisplay.Outcome -> if (pendingCancel != null) LibraryDisplay.ConfirmCancel else LibraryDisplay.Page(
                        current
                    )

                    LibraryDisplay.ConfirmCancel -> LibraryDisplay.Page(current)
                    is LibraryDisplay.Page -> if (depth > 0) LibraryDisplay.Page(backStack[depth - 1]) else display
                }
                progress.collect { event ->
                    seekableState.seekTo(event.progress, targetState = fallback)
                }
                seekableState.animateTo(fallback)
                when (display) {
                    LibraryDisplay.Outcome -> outcome = null
                    LibraryDisplay.ConfirmCancel -> pendingCancel = null
                    is LibraryDisplay.Page -> viewModel.back()
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                seekableState.animateTo(display)
            }
        }

        val seatsAtTime = remember(seats, selectedStartTime) {
            val time = selectedStartTime
            if (time == null) emptyList() else seats.valueOrNull().orEmpty().filter { it.isBookableAt(time) }
        }
        val slotRecap = remember(selectedDate, selectedStartTime) {
            listOfNotNull(
                selectedDate?.format(RecapDateFormat)?.replaceFirstChar { it.titlecase(Locale.ITALIAN) },
                selectedStartTime?.format(TimeFormat)?.let { "ore $it" },
            ).joinToString(" · ").ifBlank { null }
        }

        Column(modifier = Modifier.testTag(LibraryTestTags.ROOT)) {
            SheetPagerHeader(
                depth = displayDepth(display),
                title = when (display) {
                    LibraryDisplay.Outcome -> ""
                    LibraryDisplay.ConfirmCancel -> stringResource(R.string.library_cancel_confirmation)
                    is LibraryDisplay.Page -> when (val current = display.page) {
                        LibraryPage.Home -> stringResource(R.string.library_title)
                        LibraryPage.Login -> stringResource(R.string.library_login)
                        LibraryPage.Libraries -> stringResource(R.string.library_libraries)
                        is LibraryPage.ReservationDetail -> detailReservation?.libraryName
                            ?: stringResource(R.string.library_reservation)
                        is LibraryPage.LibraryDetail ->
                            libraryList.firstOrNull { it.id == current.libraryId }?.name
                                ?: stringResource(R.string.library_title)

                        LibraryPage.Zones -> bookingLibrary?.name
                            ?: stringResource(R.string.library_book)

                        LibraryPage.DateTime -> selectedZone?.name
                            ?: stringResource(R.string.library_book)

                        LibraryPage.Seats -> stringResource(R.string.library_choose_seat)
                        LibraryPage.Confirm -> stringResource(R.string.common_confirm)
                        LibraryPage.Done -> stringResource(R.string.library_confirmed)
                    }
                },
                subtitle = when (display) {
                    LibraryDisplay.Outcome -> null
                    LibraryDisplay.ConfirmCancel -> pendingCancel?.libraryName
                    is LibraryDisplay.Page -> when (val current = display.page) {
                        LibraryPage.Home ->
                            if (linkedEmail == null) stringResource(R.string.library_login_and_book)
                            else if (reservations !is Loadable.Loaded) null
                            else if (reservationList.isEmpty()) stringResource(R.string.library_no_bookings)
                            else if (reservationList.size == 1) stringResource(R.string.library_one_booking)
                            else stringResource(
                                R.string.library_multiple_bookings,
                                reservationList.size
                            )

                        LibraryPage.Login -> stringResource(R.string.library_verify_email)
                        LibraryPage.Libraries -> stringResource(R.string.library_choose_library)
                        is LibraryPage.ReservationDetail -> stringResource(R.string.library_reservation_details)
                        is LibraryPage.LibraryDetail ->
                            libraryList.firstOrNull { it.id == current.libraryId }?.secondaryName
                        LibraryPage.Zones -> stringResource(R.string.library_choose_zone)
                        LibraryPage.DateTime -> stringResource(R.string.library_choose_datetime)
                        LibraryPage.Seats -> slotRecap
                        LibraryPage.Confirm -> slotRecap
                        LibraryPage.Done -> bookingLibrary?.name
                    }
                },
                onBack = when (display) {
                    LibraryDisplay.Outcome -> null
                    LibraryDisplay.ConfirmCancel -> ({ pendingCancel = null })
                    is LibraryDisplay.Page ->
                        if (depth > 0 && !submitting && !onDonePage) viewModel::back else null
                },
            )

            transition.AnimatedContent(
                transitionSpec = {
                    sheetPageTransform(forward = displayDepth(targetState) >= displayDepth(initialState))
                },
                contentKey = { displayKey(it) },
            ) { shown ->
                when (shown) {
                    LibraryDisplay.Outcome -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }

                    LibraryDisplay.ConfirmCancel -> pendingCancel?.let { reservation ->
                        SheetConfirmPage(
                            body = stringResource(
                                R.string.library_confirm_cancel,
                                reservation.libraryName
                            ),
                            onConfirm = {
                                pendingCancel = null
                                viewModel.cancel(reservation)
                            },
                            onKeep = { pendingCancel = null },
                            confirmIsPrimary = true,
                        )
                    }

                    is LibraryDisplay.Page -> when (val page = shown.page) {
                    LibraryPage.Home -> HomePage(
                        reservations = reservations,
                        loggedIn = linkedEmail != null,
                        onOpenReservation = viewModel::openReservation,
                        onLogin = viewModel::openLogin,
                        onPrenota = viewModel::openLibraries,
                    )

                    LibraryPage.Login -> LoginPage(
                        email = email,
                        phase = loginPhase,
                        feedback = loginFeedback,
                        onSendEmail = viewModel::sendLoginEmail,
                        onVerify = viewModel::verifyLogin,
                        onFeedbackDismiss = viewModel::dismissLoginFeedback,
                    )

                    LibraryPage.Libraries -> LibrariesPage(
                        libraries = libraries,
                        librariesStatus = librariesStatus,
                        onOpenLibrary = viewModel::openLibrary,
                        onRetry = viewModel::refreshLibraries,
                    )

                    is LibraryPage.ReservationDetail -> {
                        val reservation = reservationList.firstOrNull { it.reservationId == page.reservationId }
                        if (reservation != null) {
                            LibraryReservationDetailPage(
                                reservation = reservation,
                                isCancelling = cancellingId == reservation.reservationId,
                                onVerifyPresence = { showScanner = true },
                                onCancel = { pendingCancel = it },
                            )
                        }
                    }

                    is LibraryPage.LibraryDetail -> LibraryDetailPage(
                        library = libraryList.firstOrNull { it.id == page.libraryId },
                        liveStatus = liveStatus,
                        weekHours = weekHours,
                        detailStatus = detailStatus,
                        onPrenota = {
                            libraryList.firstOrNull { it.id == page.libraryId }?.let(viewModel::startBooking)
                        },
                        onRetry = viewModel::retryDetail,
                    )

                    LibraryPage.Zones -> ZonesPage(
                        zones = zones,
                        zonesStatus = zonesStatus,
                        onSelectZone = viewModel::selectZone,
                        onRetry = viewModel::retryZones,
                    )

                    LibraryPage.DateTime -> DateTimePage(
                        constraints = constraints,
                        constraintsStatus = constraintsStatus,
                        selectedDate = selectedDate,
                        selectedDuration = selectedDuration,
                        seats = seats,
                        seatsStatus = seatsStatus,
                        availableStartTimes = availableStartTimes,
                        selectedStartTime = selectedStartTime,
                        enabled = !submitting,
                        onSelectDate = viewModel::selectDate,
                        onSelectDuration = viewModel::selectDuration,
                        onSelectStartTime = viewModel::selectStartTime,
                        onContinue = viewModel::goToSeats,
                        onRetryConstraints = viewModel::retryConstraints,
                        onRetrySeats = viewModel::retrySeats,
                    )

                    LibraryPage.Seats -> SeatsPage(
                        seats = seatsAtTime,
                        zoneColor = selectedZone?.color ?: LibraryZoneColor.Other,
                        onSelectSeat = viewModel::selectSeat,
                        onAutoSelect = viewModel::autoSelectSeat,
                    )

                    LibraryPage.Confirm -> {
                        val seat = selectedSeat
                        val date = selectedDate
                        val start = selectedStartTime
                        val duration = selectedDuration
                        val library = bookingLibrary
                        val zone = selectedZone
                        if (seat != null && date != null && start != null && duration != null && library != null && zone != null) {
                            ConfirmPage(
                                libraryName = library.name,
                                zoneName = zone.name,
                                seat = seat,
                                date = date,
                                startTime = start,
                                durationMinutes = duration,
                                email = email,
                                note = note,
                                onNoteChange = viewModel::setNote,
                                agreement = mandatoryAgreement,
                                consentAccepted = consentAccepted,
                                onConsentChange = viewModel::setConsent,
                                submitting = submitting,
                                onSubmit = viewModel::submit,
                            )
                        }
                    }

                    LibraryPage.Done -> LibraryDonePage(
                        libraryName = bookingLibrary?.name.orEmpty(),
                        zoneName = selectedZone?.name.orEmpty(),
                        seatName = selectedSeat?.shortName.orEmpty(),
                        date = selectedDate,
                        startTime = selectedStartTime,
                        durationMinutes = selectedDuration,
                        onDone = viewModel::finishBooking,
                    )
                    }
                }
            }
        }

        if (showScanner) {
            QrScannerScreen(
                onResult = { code ->
                    showScanner = false
                    viewModel.verifyPresence(code)
                },
                onClose = { showScanner = false },
            )
        }
    }
}

/**
 * What the pager currently shows. The ViewModel owns the booking back stack; the result and
 * cancel-confirmation overlays are sheet-local and stack on top of it.
 */
private sealed interface LibraryDisplay {
    data class Page(val page: LibraryPage) : LibraryDisplay
    data object ConfirmCancel : LibraryDisplay
    data object Outcome : LibraryDisplay
}

private fun displayDepth(display: LibraryDisplay): Int = when (display) {
    is LibraryDisplay.Page -> pageDepth(display.page)
    LibraryDisplay.ConfirmCancel -> 2
    LibraryDisplay.Outcome -> 8
}

private fun displayKey(display: LibraryDisplay): String = when (display) {
    is LibraryDisplay.Page -> display.page.key
    LibraryDisplay.ConfirmCancel -> "confirm_cancel"
    LibraryDisplay.Outcome -> "outcome"
}

private fun pageDepth(page: LibraryPage): Int = when (page) {
    LibraryPage.Home -> 0
    LibraryPage.Login -> 1
    is LibraryPage.ReservationDetail -> 1
    LibraryPage.Libraries -> 1
    is LibraryPage.LibraryDetail -> 2
    LibraryPage.Zones -> 3
    LibraryPage.DateTime -> 4
    LibraryPage.Seats -> 5
    LibraryPage.Confirm -> 6
    LibraryPage.Done -> 7
}
