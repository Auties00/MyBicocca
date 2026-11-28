package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.isBookableAt
import it.attendance100.mybicocca.domain.model.library.isEndedAt
import it.attendance100.mybicocca.domain.model.library.isInProgressAt
import it.attendance100.mybicocca.domain.model.library.isUpcomingSoonAt
import it.attendance100.mybicocca.ui.component.modal.LockSheetWhile
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetHeaderSpec
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPager
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.rememberLastNonNull
import it.attendance100.mybicocca.ui.navigation.DisposableEffectOnPop
import it.attendance100.mybicocca.ui.navigation.ShellCommand
import it.attendance100.mybicocca.ui.navigation.ShellCommands
import it.attendance100.mybicocca.ui.navigation.route.ShellTab
import it.attendance100.mybicocca.ui.screen.registry.subscreen.attendance.subscreen.rilevaPresenza.component.QrScannerScreen
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.AccountPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.AddressChooserSheet
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.BookingsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.ConfirmPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.DateTimePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.HomeHeaderAction
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.HomePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibrariesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryDonePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibraryReservationDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LibrarySharedTransition
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LocalLibrarySharedTransition
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.LoginPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.SeatsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.ServicesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.component.ZonesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.ext.libraryBuildingLine
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryBookingsTab
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.LibraryPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.library.state.depth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val RecapDateFormat: DateTimeFormatter
    @Composable
    @ReadOnlyComposable
    get() = DateTimeFormatter.ofPattern("EEE d MMM", currentLocale())
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * The whole Biblioteca experience — Affluences seat booking for the university libraries — as a
 * single bottom-sheet entry. Public library browsing and seat selection work without a linked
 * account; e-mail verification is requested on Confirm or from the Home header / bookings entry.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
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
    val strLibrarySeatUnavailable = stringResource(R.string.library_seat_unavailable)
    val strLibrarySeatUnavailableBody = stringResource(R.string.library_seat_unavailable_body)

    LaunchedEffect(Unit) {
        viewModel.onSheetOpened()
    }

    DisposableEffectOnPop { viewModel.resetNavigation() }

    run {
        var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
        var outcomeKind by remember { mutableStateOf(LibraryOutcomeKind.Cancellation) }
        var pendingCancel by remember { mutableStateOf<LibraryReservation?>(null) }
        var pendingLogout by remember { mutableStateOf(false) }

        val backStack by viewModel.backStack.collectAsStateWithLifecycle()
        val preferredCampus by viewModel.preferredCampus.collectAsStateWithLifecycle()
        val libraries by viewModel.libraries.collectAsStateWithLifecycle()
        val librariesStatus by viewModel.librariesStatus.collectAsStateWithLifecycle()
        val reservations by viewModel.reservations.collectAsStateWithLifecycle()
        val pastReservations by viewModel.pastReservations.collectAsStateWithLifecycle()
        val bookingsTab by viewModel.bookingsTab.collectAsStateWithLifecycle()
        val nextReservation by viewModel.nextReservation.collectAsStateWithLifecycle()
        val addressChooser by viewModel.addressChooser.collectAsStateWithLifecycle()
        val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
        val cancellingId by viewModel.cancellingId.collectAsStateWithLifecycle()
        val linkedEmail by viewModel.linkedEmail.collectAsStateWithLifecycle()
        val institutionalEmail by viewModel.institutionalEmail.collectAsStateWithLifecycle()
        val bookingEmail by viewModel.bookingEmail.collectAsStateWithLifecycle()
        val loginPhase by viewModel.loginPhase.collectAsStateWithLifecycle()
        val loginFeedback by viewModel.loginFeedback.collectAsStateWithLifecycle()

        val liveStatus by viewModel.liveStatus.collectAsStateWithLifecycle()
        val weekHours by viewModel.weekHours.collectAsStateWithLifecycle()
        val siteServices by viewModel.siteServices.collectAsStateWithLifecycle()
        val crowdingWeek by viewModel.crowdingWeek.collectAsStateWithLifecycle()
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
        val pastReservationList = pastReservations.valueOrNull().orEmpty()
        val allReservations = reservationList + pastReservationList
        val libraryList = libraries.valueOrNull().orEmpty()
        val mandatoryAgreement = remember(agreements) { agreements.firstOrNull { it.mandatory } }
        val email = institutionalEmail.orEmpty()

        val openBuildingOnMap: (String) -> Unit = { buildingCode ->
            viewModel.dismissAddressChooser()
            ShellCommands.send(
                ShellCommand(
                    tab = ShellTab.Map,
                    routes = emptyList(),
                    building = buildingCode,
                ),
            )
        }

        LaunchedEffect(linkedEmail) {
            if (linkedEmail == null) pendingLogout = false
        }

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
                    LibraryEvent.SeatNoLongerAvailable -> {
                        outcomeKind = LibraryOutcomeKind.Booking
                        outcome = SheetOutcome.Error(strLibrarySeatUnavailable, body = strLibrarySeatUnavailableBody)
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
            ?.let { page -> allReservations.firstOrNull { it.reservationId == page.reservationId } }
        LaunchedEffect(current, allReservations) {
            if (current is LibraryPage.ReservationDetail && detailReservation == null) viewModel.back()
        }

        val onDonePage = current is LibraryPage.Done
        val display: LibraryDisplay = when {
            outcome != null -> LibraryDisplay.Outcome
            pendingCancel != null -> LibraryDisplay.ConfirmCancel
            pendingLogout -> LibraryDisplay.ConfirmLogout
            else -> LibraryDisplay.Page(current)
        }
        val isOverlayShowing = display !is LibraryDisplay.Page

        BackHandler(enabled = onDonePage && display is LibraryDisplay.Page) { }

        // A booking in flight cannot be abandoned half-way: lock swipe/scrim/back dismissal.
        LockSheetWhile(submitting)

        val backTo: LibraryDisplay? = when (display) {
            LibraryDisplay.Outcome -> when {
                pendingCancel != null -> LibraryDisplay.ConfirmCancel
                pendingLogout -> LibraryDisplay.ConfirmLogout
                else -> LibraryDisplay.Page(current)
            }
            LibraryDisplay.ConfirmCancel -> LibraryDisplay.Page(current)
            LibraryDisplay.ConfirmLogout -> LibraryDisplay.Page(current)
            is LibraryDisplay.Page ->
                if (depth > 0 && !submitting && !onDonePage) LibraryDisplay.Page(backStack[depth - 1]) else null
        }

        // Keep content readable while overlays or booking steps slide out after state clears.
        val shownOutcome = rememberLastNonNull(outcome)
        val shownCancel = rememberLastNonNull(pendingCancel)
        val shownLinkedEmail = rememberLastNonNull(linkedEmail)
        val shownZone = rememberLastNonNull(selectedZone)
        val shownLibrary = rememberLastNonNull(bookingLibrary)
        val shownDetailReservation = rememberLastNonNull(detailReservation)

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

        // A page keeps its scroll and open rows while another covers it, and drops them once popped.
        val pageStates = rememberSaveableStateHolder()
        val openPageKeys = remember { mutableSetOf<String>() }
        LaunchedEffect(backStack) {
            val keys = backStack.mapTo(mutableSetOf()) { it.key }
            (openPageKeys - keys).forEach(pageStates::removeState)
            openPageKeys.clear()
            openPageKeys.addAll(keys)
        }

        SharedTransitionLayout {
            val sharedScope = this
            SheetPager(
                page = display,
                depth = { displayDepth(it) },
                backTo = backTo,
                onBack = {
                    when (display) {
                        LibraryDisplay.Outcome -> outcome = null
                        LibraryDisplay.ConfirmCancel -> pendingCancel = null
                        LibraryDisplay.ConfirmLogout -> pendingLogout = false
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
                            LibraryDisplay.ConfirmLogout -> stringResource(R.string.library_logout_confirmation)
                            is LibraryDisplay.Page -> when (val page = target.page) {
                                LibraryPage.Home -> stringResource(R.string.library_title)
                                LibraryPage.Libraries -> stringResource(R.string.library_libraries_title)
                                is LibraryPage.Login -> stringResource(R.string.library_login)
                                LibraryPage.Bookings -> stringResource(R.string.library_my_bookings)
                                LibraryPage.Account -> stringResource(R.string.library_account)
                                LibraryPage.Services -> stringResource(R.string.library_services)
                                is LibraryPage.ReservationDetail -> stringResource(R.string.library_reservation_details)
                                is LibraryPage.LibraryDetail ->
                                    libraryList.firstOrNull { it.id == page.libraryId }?.name
                                        ?: stringResource(R.string.library_title)
                                LibraryPage.Zones -> shownLibrary?.name
                                    ?: stringResource(R.string.library_book)
                                LibraryPage.DateTime -> shownZone?.name
                                    ?: stringResource(R.string.library_book)
                                LibraryPage.Seats -> stringResource(R.string.library_choose_seat)
                                LibraryPage.Confirm -> stringResource(R.string.common_confirm)
                                LibraryPage.Done -> stringResource(R.string.library_confirmed)
                            }
                        },
                        subtitle = when (target) {
                            LibraryDisplay.Outcome -> when (outcomeKind) {
                                LibraryOutcomeKind.Cancellation -> shownCancel?.libraryName
                                LibraryOutcomeKind.Booking -> shownLibrary?.name
                                LibraryOutcomeKind.Login, LibraryOutcomeKind.Sync ->
                                    linkedEmail ?: email.ifBlank { null }
                                LibraryOutcomeKind.Presence ->
                                    (detailReservation ?: nextReservation)?.libraryName
                            } ?: stringResource(R.string.library_title)
                            LibraryDisplay.ConfirmCancel -> shownCancel?.libraryName
                                ?: stringResource(R.string.library_reservation)
                            LibraryDisplay.ConfirmLogout -> shownLinkedEmail
                                ?: stringResource(R.string.library_account)
                            is LibraryDisplay.Page -> when (val page = target.page) {
                                LibraryPage.Home -> null
                                LibraryPage.Libraries -> if (preferredCampus != null) {
                                    stringResource(R.string.library_subtitle_your_campus)
                                } else {
                                    stringResource(R.string.library_subtitle_choose_site)
                                }
                                is LibraryPage.Login -> stringResource(R.string.library_verify_email)
                                LibraryPage.Bookings -> when (bookingsTab) {
                                    LibraryBookingsTab.Upcoming -> when {
                                        reservations !is Loadable.Loaded -> null
                                        reservationList.isEmpty() -> stringResource(R.string.library_bookings_none_scheduled)
                                        else -> pluralStringResource(
                                            R.plurals.library_bookings_scheduled_count,
                                            reservationList.size,
                                            reservationList.size,
                                        )
                                    }
                                    LibraryBookingsTab.History -> when {
                                        pastReservations !is Loadable.Loaded -> null
                                        pastReservationList.isEmpty() -> stringResource(R.string.library_no_past_bookings)
                                        else -> pluralStringResource(
                                            R.plurals.library_booking_count,
                                            pastReservationList.size,
                                            pastReservationList.size,
                                        )
                                    }
                                }
                                LibraryPage.Account -> stringResource(R.string.library_title)
                                LibraryPage.Services -> null
                                is LibraryPage.ReservationDetail -> {
                                    val reservation = allReservations.firstOrNull {
                                        it.reservationId == page.reservationId
                                    } ?: shownDetailReservation
                                    reservation?.let { reservationStatusSubtitle(it) }
                                }
                                is LibraryPage.LibraryDetail ->
                                    libraryList.firstOrNull { it.id == page.libraryId }
                                        ?.let { libraryBuildingLine(it) }
                                LibraryPage.Zones -> stringResource(R.string.library_choose_zone)
                                LibraryPage.DateTime -> stringResource(R.string.library_choose_datetime)
                                LibraryPage.Seats, LibraryPage.Confirm -> slotRecap
                                    ?: shownZone?.name
                                    ?: stringResource(R.string.library_choose_datetime)
                                LibraryPage.Done -> shownLibrary?.name
                                    ?: stringResource(R.string.library_booked)
                            }
                        },
                        showBack = when (target) {
                            LibraryDisplay.Outcome -> false
                            LibraryDisplay.ConfirmCancel -> true
                            LibraryDisplay.ConfirmLogout -> true
                            is LibraryDisplay.Page -> !submitting && target.page != LibraryPage.Done
                        },
                        trailing = when (target) {
                            is LibraryDisplay.Page -> when (target.page) {
                                LibraryPage.Home -> {
                                    {
                                        HomeHeaderAction(
                                            linkedEmail = linkedEmail,
                                            onLogin = { viewModel.openLogin(fromBooking = false) },
                                            onOpenAccount = viewModel::openAccount,
                                        )
                                    }
                                }
                                else -> null
                            }
                            else -> null
                        },
                    )
                },
            ) { shown ->
                val sharedTransition = LibrarySharedTransition(
                    sharedScope = sharedScope,
                    animatedContentScope = this,
                    isOverlayShowing = isOverlayShowing,
                )
                CompositionLocalProvider(LocalLibrarySharedTransition provides sharedTransition) {
                    when (shown) {
                        LibraryDisplay.Outcome -> shownOutcome?.let { currentOutcome ->
                            SheetResultPage(outcome = currentOutcome, onDismiss = { outcome = null })
                        }

                        LibraryDisplay.ConfirmCancel -> shownCancel?.let { reservation ->
                            SheetConfirmPage(
                                body = stringResource(
                                    R.string.library_confirm_cancel,
                                    reservation.libraryName,
                                ),
                                onConfirm = {
                                    pendingCancel = null
                                    viewModel.cancel(reservation)
                                },
                                onKeep = { pendingCancel = null },
                                confirmIsPrimary = true,
                            )
                        }

                        LibraryDisplay.ConfirmLogout -> SheetConfirmPage(
                            body = stringResource(R.string.library_confirm_logout_body),
                            confirmLabel = stringResource(R.string.library_logout),
                            onConfirm = {
                                pendingLogout = false
                                viewModel.logoutAccount()
                            },
                            onKeep = { pendingLogout = false },
                            confirmIsPrimary = true,
                        )

                        is LibraryDisplay.Page -> pageStates.SaveableStateProvider(shown.page.key) {
                            when (val page = shown.page) {
                                LibraryPage.Home -> HomePage(
                                    linkedEmail = linkedEmail,
                                    nextReservation = nextReservation,
                                    libraries = libraries,
                                    reservations = reservations,
                                    syncStatus = syncStatus,
                                    onOpenLibraries = viewModel::openLibraries,
                                    onOpenReservation = viewModel::openReservation,
                                    onOpenBookings = viewModel::openBookings,
                                    onOpenServices = viewModel::openServices,
                                )

                                LibraryPage.Libraries -> LibrariesPage(
                                    libraries = libraries,
                                    librariesStatus = librariesStatus,
                                    onOpenLibrary = viewModel::openLibrary,
                                    onRetry = viewModel::refreshLibraries,
                                )

                                is LibraryPage.Login -> LoginPage(
                                    email = email,
                                    phase = loginPhase,
                                    feedback = loginFeedback,
                                    onSendEmail = viewModel::sendLoginEmail,
                                    onResendEmail = viewModel::resendLoginEmail,
                                    onVerify = viewModel::verifyLogin,
                                    onFeedbackDismiss = viewModel::dismissLoginFeedback,
                                    onResume = viewModel::checkValidationSilently,
                                )

                                LibraryPage.Bookings -> BookingsPage(
                                    reservations = reservations,
                                    pastReservations = pastReservations,
                                    selectedTab = bookingsTab,
                                    onSelectTab = viewModel::selectBookingsTab,
                                    onOpenReservation = viewModel::openReservation,
                                    onVerifyPresence = { showScanner = true },
                                    onBook = viewModel::openLibraries,
                                )

                                LibraryPage.Account -> AccountPage(
                                    linkedEmail = shownLinkedEmail.orEmpty(),
                                    onRequestLogout = { pendingLogout = true },
                                )

                                LibraryPage.Services -> ServicesPage()

                                is LibraryPage.ReservationDetail -> {
                                    val reservation = allReservations.firstOrNull {
                                        it.reservationId == page.reservationId
                                    } ?: shownDetailReservation
                                    if (reservation != null) {
                                        LibraryReservationDetailPage(
                                            reservation = reservation,
                                            isCancelling = cancellingId == reservation.reservationId,
                                            onShowOnMap = openBuildingOnMap,
                                            onVerifyPresence = { showScanner = true },
                                            onCancel = { pendingCancel = it },
                                        )
                                    }
                                }

                                is LibraryPage.LibraryDetail -> {
                                    val detailLibrary = libraryList.firstOrNull { it.id == page.libraryId }
                                    LibraryDetailPage(
                                        library = detailLibrary,
                                        liveStatus = liveStatus,
                                        weekHours = weekHours,
                                        siteServices = siteServices,
                                        crowdingWeek = crowdingWeek,
                                        detailStatus = detailStatus,
                                        onOpenAddress = {
                                            detailLibrary?.let(viewModel::openAddressChooser)
                                        },
                                        onPrenota = {
                                            detailLibrary?.let(viewModel::startBooking)
                                        },
                                        onRetry = viewModel::retryDetail,
                                    )
                                }

                                LibraryPage.Zones -> ZonesPage(
                                    zones = zones,
                                    zonesStatus = zonesStatus,
                                    onSelectZone = viewModel::selectZone,
                                    onRetry = viewModel::retryZones,
                                )

                                LibraryPage.DateTime -> DateTimePage(
                                    zone = shownZone,
                                    libraryName = shownLibrary?.name.orEmpty(),
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
                                    zone = shownZone,
                                    libraryName = shownLibrary?.name.orEmpty(),
                                    seats = seatsAtTime,
                                    seatsStatus = seatsStatus,
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
                                            email = bookingEmail.orEmpty(),
                                            loggedIn = linkedEmail != null,
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
                                    libraryName = shownLibrary?.name.orEmpty(),
                                    zoneName = shownZone?.name.orEmpty(),
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
            }
        }

        val activeAddressChooser = addressChooser
        if (activeAddressChooser != null) {
            AddressChooserSheet(
                state = activeAddressChooser,
                onShowOnMap = openBuildingOnMap,
                onDismiss = viewModel::dismissAddressChooser,
            )
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

@Composable
private fun reservationStatusSubtitle(reservation: LibraryReservation): String {
    val now = LocalDateTime.now(AppClock.clock)
    return when {
        reservation.isEndedAt(now) -> stringResource(R.string.library_booking_ended)
        reservation.isInProgressAt(now) -> stringResource(R.string.library_booking_in_progress)
        reservation.isUpcomingSoonAt(now) -> stringResource(R.string.library_booking_starting_soon)
        else -> stringResource(R.string.library_booking_scheduled)
    }
}

/** The operation an outcome page reports on, naming its header. */
private enum class LibraryOutcomeKind { Cancellation, Booking, Login, Sync, Presence }

/**
 * What the pager currently shows. The ViewModel owns the booking back stack; the result and
 * confirmation overlays are sheet-local and stack on top of the page they cover.
 */
private sealed interface LibraryDisplay {
    data class Page(val page: LibraryPage) : LibraryDisplay
    data object ConfirmLogout : LibraryDisplay
    data object ConfirmCancel : LibraryDisplay
    data object Outcome : LibraryDisplay
}

private fun displayDepth(display: LibraryDisplay): Int = when (display) {
    is LibraryDisplay.Page -> display.page.depth()
    LibraryDisplay.ConfirmLogout -> 2
    LibraryDisplay.ConfirmCancel -> 3
    LibraryDisplay.Outcome -> 9
}

private fun displayKey(display: LibraryDisplay): String = when (display) {
    is LibraryDisplay.Page -> display.page.key
    LibraryDisplay.ConfirmLogout -> "confirm_logout"
    LibraryDisplay.ConfirmCancel -> "confirm_cancel"
    LibraryDisplay.Outcome -> "outcome"
}
