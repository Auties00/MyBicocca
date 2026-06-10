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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.domain.model.library.isBookableAt
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component.QrScannerScreen
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.ConfirmPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.DateTimePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.HomePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryDonePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibrariesPage
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

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    LibraryEvent.ReservationCancelled -> outcome = SheetOutcome.Success("Prenotazione annullata.")
                    is LibraryEvent.CancelFailed -> outcome = SheetOutcome.Error("Annullamento non riuscito", event.cause)
                    is LibraryEvent.BookingFailed -> outcome = SheetOutcome.Error("Prenotazione non riuscita", event.cause)
                    is LibraryEvent.LoginEmailSent -> Unit
                    is LibraryEvent.LoginRequestFailed -> outcome = SheetOutcome.Error("Invio non riuscito", event.cause)
                    is LibraryEvent.LoginFailed -> outcome = SheetOutcome.Error("Accesso non riuscito", event.cause)
                    is LibraryEvent.SyncFailed -> outcome = SheetOutcome.Error("Sincronizzazione non riuscita", event.cause)
                    LibraryEvent.PresenceVerified -> outcome = SheetOutcome.Success("Presenza registrata.")
                    LibraryEvent.PresenceInvalidCode ->
                        outcome = SheetOutcome.Error("Codice non valido", body = "Controlla il QR o il codice e riprova.")
                    is LibraryEvent.PresenceFailed -> outcome = SheetOutcome.Error("Verifica non riuscita", event.cause)
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
        BackHandler(
            enabled = display !is LibraryDisplay.Page || (depth > 0 && !submitting && !onDonePage),
        ) {
            when (display) {
                LibraryDisplay.Outcome -> outcome = null
                LibraryDisplay.ConfirmCancel -> pendingCancel = null
                is LibraryDisplay.Page -> viewModel.back()
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
                    LibraryDisplay.ConfirmCancel -> "Cancellare la prenotazione?"
                    is LibraryDisplay.Page -> when (val current = display.page) {
                        LibraryPage.Home -> "Biblioteca"
                        LibraryPage.Login -> "Accedi"
                        LibraryPage.Libraries -> "Biblioteche"
                        is LibraryPage.ReservationDetail -> detailReservation?.libraryName ?: "Prenotazione"
                        is LibraryPage.LibraryDetail ->
                            libraryList.firstOrNull { it.id == current.libraryId }?.name ?: "Biblioteca"
                        LibraryPage.Zones -> bookingLibrary?.name ?: "Prenota"
                        LibraryPage.DateTime -> selectedZone?.name ?: "Prenota"
                        LibraryPage.Seats -> "Scegli il posto"
                        LibraryPage.Confirm -> "Conferma"
                        LibraryPage.Done -> "Confermato"
                    }
                },
                subtitle = when (display) {
                    LibraryDisplay.Outcome -> null
                    LibraryDisplay.ConfirmCancel -> pendingCancel?.libraryName
                    is LibraryDisplay.Page -> when (val current = display.page) {
                        LibraryPage.Home ->
                            if (linkedEmail == null) "Accedi e prenota un posto"
                            else if (reservations !is Loadable.Loaded) null
                            else if (reservationList.isEmpty()) "Nessuna prenotazione"
                            else if (reservationList.size == 1) "1 prenotazione"
                            else "${reservationList.size} prenotazioni"
                        LibraryPage.Login -> "Verifica la tua email"
                        LibraryPage.Libraries -> "Scegli dove prenotare"
                        is LibraryPage.ReservationDetail -> "Dettagli prenotazione"
                        is LibraryPage.LibraryDetail ->
                            libraryList.firstOrNull { it.id == current.libraryId }?.secondaryName
                        LibraryPage.Zones -> "Scegli una zona"
                        LibraryPage.DateTime -> "Scegli giorno e orario"
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

            AnimatedContent(
                targetState = display,
                transitionSpec = {
                    sheetPageTransform(forward = displayDepth(targetState) >= displayDepth(initialState))
                },
                contentKey = { displayKey(it) },
                label = "library_sheet_pages",
            ) { shown ->
                when (shown) {
                    LibraryDisplay.Outcome -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }

                    LibraryDisplay.ConfirmCancel -> pendingCancel?.let { reservation ->
                        SheetConfirmPage(
                            body = "Stai per cancellare il posto in ${reservation.libraryName}. " +
                                "Tornerà prenotabile da altri studenti.",
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
                hint = "Inquadra il QR in biblioteca",
                manualTitle = "Inserisci codice",
                manualDescription = "Inserisci il codice di validazione mostrato in biblioteca.",
                manualLabel = "Codice di validazione",
                manualConfirm = "Verifica",
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
