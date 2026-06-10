package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.attendance100.mybicocca.core.state.Loadable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.ui.component.modal.SheetConfirmPage
import it.attendance100.mybicocca.ui.component.modal.SheetOutcome
import it.attendance100.mybicocca.ui.component.modal.SheetPagerHeader
import it.attendance100.mybicocca.ui.component.modal.SheetResultPage
import it.attendance100.mybicocca.ui.component.modal.sheetPageTransform
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component.DonePage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component.FormPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component.ReservationDetailPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component.ReservationsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component.SectionsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component.SlotsPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.component.TypesPage
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.displayName
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.ext.toDirectorySections
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentsEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentsPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DetailDateFormat = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ITALIAN)
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Body of the Appuntamenti modal: the whole student-services appointment experience as a
 * single sheet entry whose container is owned by the navigation-level BottomSheetSceneStrategy.
 *
 * A pinned [SheetPagerHeader] morphs — it never swaps — over a ViewModel-driven in-sheet pager
 * (mirroring the maps Edifici sheet): the root lists this device's reservations, a row pushes
 * the reservation detail, and "Prenota" walks the booking wizard (sections -> types -> slots ->
 * form -> done). Cancel confirmations and cancel/booking/download outcomes render as view-side
 * pages layered over the pager; system back dismisses those first, then walks the pager up one
 * level. A reservation cancelled elsewhere pops its detail page once the live list drops it.
 * Downloaded reservation PDFs are written to the cache directory and handed to [onOpenPdf].
 * When the sheet leaves composition the pager rewinds to the root and the booking session is
 * forgotten, so a re-open starts clean.
 */
@Composable
fun AppointmentsPage(
    viewModel: AppointmentsViewModel,
    onOpenPdf: (localPath: String, fileName: String) -> Unit,
) {
    DisposableEffect(Unit) {
        onDispose { viewModel.resetNavigation() }
    }

    run {
        val context = androidx.compose.ui.platform.LocalContext.current
        var outcome by remember { mutableStateOf<SheetOutcome?>(null) }
        var pendingCancel by remember { mutableStateOf<AppointmentReservation?>(null) }

        val backStack by viewModel.backStack.collectAsStateWithLifecycle()
        val services by viewModel.services.collectAsStateWithLifecycle()
        val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
        val reservations by viewModel.reservations.collectAsStateWithLifecycle()
        val cancellingCode by viewModel.cancellingCode.collectAsStateWithLifecycle()
        val bookingService by viewModel.bookingService.collectAsStateWithLifecycle()
        val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
        val selectedSlot by viewModel.selectedSlot.collectAsStateWithLifecycle()
        val submitting by viewModel.submitting.collectAsStateWithLifecycle()
        val bookedReservation by viewModel.bookedReservation.collectAsStateWithLifecycle()

        val current = backStack.last()
        val depth = backStack.lastIndex
        val reservationList = (reservations as? Loadable.Loaded)?.value.orEmpty()

        LaunchedEffect(Unit) {
            viewModel.events.collect { event ->
                when (event) {
                    AppointmentsEvent.ReservationCancelled ->
                        outcome = SheetOutcome.Success("Appuntamento annullato.")
                    is AppointmentsEvent.CancelFailed ->
                        outcome = SheetOutcome.Error("Annullamento non riuscito", event.cause)
                    is AppointmentsEvent.PdfReady -> {
                        val path = withContext(Dispatchers.IO) {
                            val dir = File(context.cacheDir, "appointments").apply { mkdirs() }
                            File(dir, event.fileName).apply { writeBytes(event.bytes) }.absolutePath
                        }
                        onOpenPdf(path, event.fileName)
                    }
                    is AppointmentsEvent.PdfFailed ->
                        outcome = SheetOutcome.Error("Download non riuscito", event.cause)
                    is AppointmentsEvent.BookingFailed ->
                        outcome = SheetOutcome.Error("Prenotazione non riuscita", event.cause)
                }
            }
        }

        val detailReservation: AppointmentReservation? = (current as? AppointmentsPage.ReservationDetail)
            ?.let { page -> reservationList.firstOrNull { it.code == page.code } }
        LaunchedEffect(current, reservationList) {
            if (current is AppointmentsPage.ReservationDetail && detailReservation == null) {
                viewModel.back()
            }
        }

        BackHandler(enabled = outcome != null || pendingCancel != null || (depth > 0 && !submitting)) {
            when {
                outcome != null -> outcome = null
                pendingCancel != null -> pendingCancel = null
                else -> viewModel.back()
            }
        }

        val display: AppointmentsDisplay = when {
            outcome != null -> AppointmentsDisplay.Outcome
            pendingCancel != null -> AppointmentsDisplay.ConfirmCancel
            else -> AppointmentsDisplay.Page(current)
        }

        val sections = remember(services) {
            (services as? Loadable.Loaded)?.value?.toDirectorySections().orEmpty()
        }
        val serviceName = bookingService?.displayName.orEmpty()
        val slotRecap = listOfNotNull(
            selectedDate?.format(DetailDateFormat)?.replaceFirstChar { it.titlecase(Locale.ITALIAN) },
            selectedSlot?.start?.format(TimeFormat)?.let { "ore $it" },
        ).joinToString(" · ").ifBlank { null }

        Column(modifier = Modifier.testTag(AppointmentsTestTags.ROOT)) {
            SheetPagerHeader(
                depth = displayDepth(display),
                title = when (display) {
                    AppointmentsDisplay.Outcome -> ""
                    AppointmentsDisplay.ConfirmCancel -> "Annullare l'appuntamento?"
                    is AppointmentsDisplay.Page -> when (val page = display.page) {
                        AppointmentsPage.Reservations -> "Segreterie"
                        is AppointmentsPage.ReservationDetail -> detailReservation?.serviceName ?: "Appuntamento"
                        AppointmentsPage.Sections -> "Prenota"
                        is AppointmentsPage.Types -> page.sectionName
                        AppointmentsPage.Slots -> serviceName.ifBlank { "Prenota" }
                        AppointmentsPage.Form -> serviceName.ifBlank { "Conferma" }
                        AppointmentsPage.Done -> "Confermato"
                    }
                },
                subtitle = when (display) {
                    AppointmentsDisplay.Outcome -> null
                    AppointmentsDisplay.ConfirmCancel -> pendingCancel?.serviceName
                    is AppointmentsDisplay.Page -> when (val page = display.page) {
                        AppointmentsPage.Reservations ->
                            if (reservations !is Loadable.Loaded) null
                            else if (reservationList.isEmpty()) "Nessuna prenotazione"
                            else if (reservationList.size == 1) "1 prenotazione"
                            else "${reservationList.size} prenotazioni"
                        is AppointmentsPage.ReservationDetail -> "Dettagli appuntamento"
                        AppointmentsPage.Sections -> "Scegli un servizio"
                        is AppointmentsPage.Types ->
                            sections.firstOrNull { it.name == page.sectionName }?.caption
                        AppointmentsPage.Slots -> "Scegli data e ora"
                        AppointmentsPage.Form -> slotRecap
                        AppointmentsPage.Done -> serviceName.ifBlank { null }
                    }
                },
                onBack = when (display) {
                    AppointmentsDisplay.Outcome -> null
                    AppointmentsDisplay.ConfirmCancel -> ({ pendingCancel = null })
                    is AppointmentsDisplay.Page -> if (depth > 0 && !submitting) viewModel::back else null
                },
            )

            AnimatedContent(
                targetState = display,
                transitionSpec = {
                    sheetPageTransform(forward = displayDepth(targetState) >= displayDepth(initialState))
                },
                contentKey = { displayKey(it) },
                label = "appointments_sheet_pages",
            ) { shown ->
                when (shown) {
                    AppointmentsDisplay.Outcome -> outcome?.let { current ->
                        SheetResultPage(outcome = current, onDismiss = { outcome = null })
                    }

                    AppointmentsDisplay.ConfirmCancel -> pendingCancel?.let { reservation ->
                        SheetConfirmPage(
                            body = "Stai per annullare l'appuntamento a \"${reservation.serviceName}\". " +
                                "Lo sportello tornerà prenotabile da altri studenti.",
                            onConfirm = {
                                pendingCancel = null
                                viewModel.cancel(reservation)
                            },
                            onKeep = { pendingCancel = null },
                        )
                    }

                    is AppointmentsDisplay.Page -> when (val page = shown.page) {
                        AppointmentsPage.Reservations -> ReservationsPage(
                            reservations = reservations,
                            onOpenReservation = viewModel::openReservation,
                            onPrenota = viewModel::openSections,
                        )

                        is AppointmentsPage.ReservationDetail -> {
                            val reservation = reservationList.firstOrNull { it.code == page.code }
                            if (reservation != null) {
                                ReservationDetailPage(
                                    reservation = reservation,
                                    isCancelling = cancellingCode == reservation.code,
                                    onCancel = { pendingCancel = it },
                                )
                            }
                        }

                        AppointmentsPage.Sections -> SectionsPage(
                            services = services,
                            syncStatus = syncStatus,
                            onRetry = viewModel::refresh,
                            onOpenSection = { viewModel.openSection(it.name) },
                        )

                        is AppointmentsPage.Types -> TypesPage(
                            services = sections.firstOrNull { it.name == page.sectionName }?.services.orEmpty(),
                            onStartBooking = viewModel::startBooking,
                        )

                        AppointmentsPage.Slots -> SlotsPage(viewModel = viewModel)

                        AppointmentsPage.Form -> FormPage(viewModel = viewModel)

                        AppointmentsPage.Done -> DonePage(
                            serviceName = serviceName,
                            reservation = bookedReservation,
                            onDone = viewModel::finishBooking,
                        )
                    }
                }
            }
        }
    }
}

/**
 * What the sheet body currently shows. The ViewModel owns the page back stack; the
 * cancel-confirm and outcome pages are view-side overlays layered on top of it.
 */
private sealed interface AppointmentsDisplay {
    data class Page(val page: AppointmentsPage) : AppointmentsDisplay
    data object ConfirmCancel : AppointmentsDisplay
    data object Outcome : AppointmentsDisplay
}

private fun displayDepth(display: AppointmentsDisplay): Int = when (display) {
    is AppointmentsDisplay.Page -> pageDepth(display.page)
    AppointmentsDisplay.ConfirmCancel -> 2
    AppointmentsDisplay.Outcome -> 6
}

private fun displayKey(display: AppointmentsDisplay): String = when (display) {
    is AppointmentsDisplay.Page -> display.page.key
    AppointmentsDisplay.ConfirmCancel -> "confirm_cancel"
    AppointmentsDisplay.Outcome -> "outcome"
}

private fun pageDepth(page: AppointmentsPage): Int = when (page) {
    AppointmentsPage.Reservations -> 0
    is AppointmentsPage.ReservationDetail -> 1
    AppointmentsPage.Sections -> 1
    is AppointmentsPage.Types -> 2
    AppointmentsPage.Slots -> 3
    AppointmentsPage.Form -> 4
    AppointmentsPage.Done -> 5
}
