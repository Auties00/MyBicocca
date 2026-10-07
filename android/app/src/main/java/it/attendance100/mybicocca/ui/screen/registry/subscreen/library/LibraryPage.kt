package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.os.currentLocale
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.domain.model.library.isBookableAt
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.navigation.DisposableEffectOnPop
import it.attendance100.mybicocca.ui.navigation.scene.LocalSheetDismissControl
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

private val RecapDateFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEE d MMM", currentLocale())
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * The whole Biblioteca experience — Affluences seat booking for the university libraries — as a
 * single bottom-sheet entry. The sheet container is owned by the navigation layer
 * (ModalSceneStrategy); this composable keeps its own VM-driven in-sheet [SheetPager] with a
 * morphing pinned header. Bookings are server-synced (Room-cached); the user logs in by validating
 * their institutional email.
 *
 * The pager renders the ViewModel's back stack (home, then login / reservation detail / the
 * libraries list, then library detail and the booking wizard: zones, date & time, seats, confirm,
 * done), with two sheet-local overlays stacked on top: a [SheetResultPage] for one-shot operation
 * outcomes and a [SheetConfirmPage] guarding reservation cancellation.
 *
 * Behavior:
 * - System back dismisses the overlays first, then pops the stack; it is blocked on the Done page
 *   (the booking is committed) and while a submission is in flight, which also locks sheet
 *   dismissal.
 * - Closing the sheet (its entry leaving the back stack) rewinds the pager and forgets the
 *   booking session.
 * - The email-sent login event surfaces no result page: the login page advances to its
 *   check-your-email state through the login phase stream.
 * - A reservation-detail page pops itself when a cancellation or sync drops its reservation.
 * - "Verifica presenza" opens a full-screen QR scanner with a manual-code fallback.
 */
@Composable
fun LibraryPage(
    viewModel: LibraryViewModel,
) {
    val strLibrarySyncFailed = stringResource(R.string.library_sync_failed)
    val strLibraryPresenceVerified = stringResource(R.string.library_presence_verified)
    val strLibraryBookingFailed = stringResource(R.string.library_booking_failed)
    val strLibraryPresenceFailed = stringResource(R.string.library_presence_failed)
    val strLibraryCancellationFailed = stringResource(R.string.library_cancellation_failed)
    val strLibraryReservationCancelled = stringResource(R.string.library_reservation_cancelled)
    val strLibraryInvalidCode = stringResource(R.string.library_invalid_code)
    val strLibraryLoginFailed = stringResource(R.string.library_login_failed)
    val strLibraryInvalidCodeBody = stringResource(R.string.library_invalid_code_body)
    val strLibrarySendFailed = stringResource(R.string.library_send_failed)

    DisposableEffectOnPop { viewModel.resetNavigation() }

    run {
        var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
        var outcomeKind by remember { mutableStateOf(LibraryOutcomeKind.Cancellation) }
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

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    LibraryEvent.ReservationCancelled -> {
                        outcomeKind = LibraryOutcomeKind.Cancellation
                        outcome = SheetOutcome.Success(strLibraryReservationCancelled)
                    }
                    is LibraryEvent.CancelFailed -> {
                        outcomeKind = LibraryOutcomeKind.Cancellation
                        outcome = SheetOutcome.Error(strLibraryCancellationFailed, event.cause)
                    }
                    is LibraryEvent.BookingFailed -> {
                        outcomeKind = LibraryOutcomeKind.Booking
                        outcome = SheetOutcome.Error(strLibraryBookingFailed, event.cause)
                    }
                    is LibraryEvent.LoginEmailSent -> Unit
                    is LibraryEvent.LoginRequestFailed -> {
                        outcomeKind = LibraryOutcomeKind.Login
                        outcome = SheetOutcome.Error(strLibrarySendFailed, event.cause)
                    }
                    is LibraryEvent.LoginFailed -> {
                        outcomeKind = LibraryOutcomeKind.Login
                        outcome = SheetOutcome.Error(strLibraryLoginFailed, event.cause)
                    }
                    is LibraryEvent.SyncFailed -> {
                        outcomeKind = LibraryOutcomeKind.Sync
                        outcome = SheetOutcome.Error(strLibrarySyncFailed, event.cause)
                    }

                    LibraryEvent.PresenceVerified -> {
                        outcomeKind = LibraryOutcomeKind.Presence
                        outcome = SheetOutcome.Success(strLibraryPresenceVerified)
                    }

                    LibraryEvent.PresenceInvalidCode -> {
                        outcomeKind = LibraryOutcomeKind.Presence
                        outcome = SheetOutcome.Error(strLibraryInvalidCode, body = strLibraryInvalidCodeBody)
                    }
                    is LibraryEvent.PresenceFailed -> {
                        outcomeKind = LibraryOutcomeKind.Presence
                        outcome = SheetOutcome.Error(strLibraryPresenceFailed, event.cause)
                    }
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

        // A booking in flight cannot be abandoned half-way: lock swipe/scrim/back dismissal.
        val control = LocalSheetDismissControl.current
        SideEffect {
            control?.gesturesEnabled = !submitting
            control?.confirmDismiss = { !submitting }
        }

        val backTo: LibraryDisplay? = when (display) {
            LibraryDisplay.Outcome ->
                if (pendingCancel != null) LibraryDisplay.ConfirmCancel else LibraryDisplay.Page(current)

            LibraryDisplay.ConfirmCancel -> LibraryDisplay.Page(current)
            is LibraryDisplay.Page ->
                if (depth > 0 && !submitting && !onDonePage) LibraryDisplay.Page(backStack[depth - 1]) else null
        }

        // While the confirmation slides out its reservation is already cleared; keep its header readable.
        val lastCancel = remember { arrayOf<LibraryReservation?>(null) }
        if (pendingCancel != null) lastCancel[0] = pendingCancel
        val shownCancel = pendingCancel ?: lastCancel[0]

        val seatsAtTime = remember(seats, selectedStartTime) {
            val time = selectedStartTime
            if (time == null) emptyList() else seats.valueOrNull().orEmpty().filter { it.isBookableAt(time) }
        }
        val locale = currentLocale()
        val recapDateFormat = RecapDateFormat
        val timeRecap = selectedStartTime?.format(TimeFormat)?.let { stringResource(R.string.registry_time_at, it) }
        val slotRecap = remember(selectedDate, timeRecap, locale, recapDateFormat) {
            listOfNotNull(
                selectedDate?.format(recapDateFormat)?.replaceFirstChar { it.titlecase(locale) },
                timeRecap,
            ).joinToString(" · ").ifBlank { null }
        }

        SheetPager(
            page = display,
            depth = { displayDepth(it) },
            backTo = backTo,
            onBack = {
                when (display) {
                    LibraryDisplay.Outcome -> outcome = null
                    LibraryDisplay.ConfirmCancel -> pendingCancel = null
                    is LibraryDisplay.Page -> viewModel.back()
                }
            },
            modifier = Modifier.testTag(LibraryTestTags.ROOT),
            key = { displayKey(it) },
            header = { target ->
                SheetHeaderSpec(
                    title = when (target) {
                        LibraryDisplay.Outcome -> stringResource(
                            when (outcomeKind) {
                                LibraryOutcomeKind.Cancellation -> R.string.library_result_cancellation_title
                                LibraryOutcomeKind.Booking -> R.string.library_reservation
                                LibraryOutcomeKind.Login -> R.string.library_result_login_title
                                LibraryOutcomeKind.Sync -> R.string.library_result_sync_title
                                LibraryOutcomeKind.Presence -> R.string.library_verify_presence
                            },
                        )
                        LibraryDisplay.ConfirmCancel -> stringResource(R.string.library_cancel_confirmation)
                        is LibraryDisplay.Page -> when (val page = target.page) {
                            LibraryPage.Home -> stringResource(R.string.library_title)
                            LibraryPage.Login -> stringResource(R.string.library_login)
                            LibraryPage.Libraries -> stringResource(R.string.library_libraries)
                            is LibraryPage.ReservationDetail ->
                                reservationList.firstOrNull { it.reservationId == page.reservationId }?.libraryName
                                    ?: stringResource(R.string.library_reservation)
                            is LibraryPage.LibraryDetail ->
                                libraryList.firstOrNull { it.id == page.libraryId }?.name
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
                    subtitle = when (target) {
                        LibraryDisplay.Outcome -> when (outcomeKind) {
                            LibraryOutcomeKind.Cancellation -> shownCancel?.libraryName
                            LibraryOutcomeKind.Booking -> bookingLibrary?.name
                            LibraryOutcomeKind.Login, LibraryOutcomeKind.Sync ->
                                linkedEmail ?: email.ifBlank { null }
                            LibraryOutcomeKind.Presence -> detailReservation?.libraryName
                        } ?: stringResource(R.string.library_title)
                        LibraryDisplay.ConfirmCancel -> shownCancel?.libraryName
                            ?: stringResource(R.string.library_reservation)
                        is LibraryDisplay.Page -> when (val page = target.page) {
                            LibraryPage.Home ->
                                if (linkedEmail == null) stringResource(R.string.library_login_and_book)
                                else if (reservations !is Loadable.Loaded) null
                                else if (reservationList.isEmpty()) stringResource(R.string.library_no_bookings)
                                else pluralStringResource(
                                    R.plurals.library_booking_count,
                                    reservationList.size,
                                    reservationList.size
                                )
                            LibraryPage.Login -> stringResource(R.string.library_verify_email)
                            LibraryPage.Libraries -> stringResource(R.string.library_choose_library)
                            is LibraryPage.ReservationDetail -> stringResource(R.string.library_reservation_details)
                            is LibraryPage.LibraryDetail -> libraryList.firstOrNull { it.id == page.libraryId }
                                ?.secondaryName?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.registry_library_desc)
                            LibraryPage.Zones -> stringResource(R.string.library_choose_zone)
                            LibraryPage.DateTime -> stringResource(R.string.library_choose_datetime)
                            LibraryPage.Seats, LibraryPage.Confirm -> slotRecap
                                ?: selectedZone?.name
                                ?: stringResource(R.string.library_choose_datetime)
                            LibraryPage.Done -> bookingLibrary?.name
                                ?: stringResource(R.string.library_booked)
                        }
                    },
                    showBack = when (target) {
                        LibraryDisplay.Outcome -> false
                        LibraryDisplay.ConfirmCancel -> true
                        is LibraryDisplay.Page -> !submitting && target.page != LibraryPage.Done
                    },
                )
            },
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
                        val reservation =
                            reservationList.firstOrNull { it.reservationId == page.reservationId }
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
                            libraryList.firstOrNull { it.id == page.libraryId }
                                ?.let(viewModel::startBooking)
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

/** The operation an outcome page reports on, naming its header. */
private enum class LibraryOutcomeKind { Cancellation, Booking, Login, Sync, Presence }

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
