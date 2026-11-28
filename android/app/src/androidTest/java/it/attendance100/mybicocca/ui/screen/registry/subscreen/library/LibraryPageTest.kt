package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryCrowd
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryForecastPoint
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryOpeningRange
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibraryService
import it.attendance100.mybicocca.domain.model.library.LibraryServiceType
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.library.BookLibrarySeatUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryBookingByTokenUseCase
import it.attendance100.mybicocca.domain.usecase.library.CancelLibraryReservationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ClearLibraryBookingDraftUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConfirmLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.ConsumeLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetAvailableSeatsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibrariesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryAgreementsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryBookingConstraintsUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryLiveStatusUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryPendingStateUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryServicesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryWeekHoursUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetLibraryZonesUseCase
import it.attendance100.mybicocca.domain.usecase.library.GetPreferredLibraryCampusUseCase
import it.attendance100.mybicocca.domain.usecase.library.LogoutLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryCrowdingHistoryUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryLinkedEmailUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObserveLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.ObservePendingLibraryActionUseCase
import it.attendance100.mybicocca.domain.usecase.library.RecordLastUsedLibraryUseCase
import it.attendance100.mybicocca.domain.usecase.library.RefreshLibraryReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.library.RequestLibraryEmailValidationUseCase
import it.attendance100.mybicocca.domain.usecase.library.SaveLibraryBookingDraftUseCase
import it.attendance100.mybicocca.domain.usecase.library.VerifyLibraryPresenceUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * UI coverage for the redesigned Biblioteca sheet: hub Home (wide Libraries CTA vs split boxes with
 * next booking, Bookings and Services rows), dedicated Libraries directory, Library detail with
 * crowding chart and address chooser, Services directory, and Bookings upcoming/history tabs.
 */
@RunWith(AndroidJUnit4::class)
class LibraryPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val getLibraries: GetLibrariesUseCase = mockk()
    private val getLibrary: GetLibraryUseCase = mockk(relaxed = true)
    private val getLibraryServices: GetLibraryServicesUseCase = mockk(relaxed = true)
    private val getLiveStatus: GetLibraryLiveStatusUseCase = mockk(relaxed = true)
    private val getWeekHours: GetLibraryWeekHoursUseCase = mockk(relaxed = true)
    private val getZones: GetLibraryZonesUseCase = mockk(relaxed = true)
    private val getAgreements: GetLibraryAgreementsUseCase = mockk(relaxed = true)
    private val getConstraints: GetLibraryBookingConstraintsUseCase = mockk(relaxed = true)
    private val getAvailableSeats: GetAvailableSeatsUseCase = mockk(relaxed = true)
    private val bookSeat: BookLibrarySeatUseCase = mockk(relaxed = true)
    private val observeReservations: ObserveLibraryReservationsUseCase = mockk()
    private val refreshReservations: RefreshLibraryReservationsUseCase = mockk(relaxed = true)
    private val cancelReservation: CancelLibraryReservationUseCase = mockk(relaxed = true)
    private val verifyPresenceUseCase: VerifyLibraryPresenceUseCase = mockk()
    private val observeLinkedEmail: ObserveLibraryLinkedEmailUseCase = mockk()
    private val requestEmailValidation: RequestLibraryEmailValidationUseCase = mockk()
    private val confirmEmailValidation: ConfirmLibraryEmailValidationUseCase = mockk()
    private val logout: LogoutLibraryUseCase = mockk(relaxed = true)
    private val cancelBookingByToken: CancelLibraryBookingByTokenUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk()
    private val observePendingLibraryAction: ObservePendingLibraryActionUseCase = mockk()
    private val consumeLibraryAction: ConsumeLibraryActionUseCase = mockk(relaxed = true)
    private val getPendingState: GetLibraryPendingStateUseCase = mockk()
    private val saveBookingDraft: SaveLibraryBookingDraftUseCase = mockk(relaxed = true)
    private val clearBookingDraft: ClearLibraryBookingDraftUseCase = mockk(relaxed = true)
    private val getPreferredCampus: GetPreferredLibraryCampusUseCase = mockk()
    private val recordLastUsedLibrary: RecordLastUsedLibraryUseCase = mockk(relaxed = true)
    private val observeCrowdingHistory: ObserveLibraryCrowdingHistoryUseCase = mockk()

    private val reservationsFlow = MutableStateFlow<List<LibraryReservation>>(emptyList())

    private fun centraleLiveStatus(): LibraryLiveStatus {
        val today = LocalDate.now(AppClock.clock)
        return LibraryLiveStatus(
            isOpen = true,
            statusText = "Aperta fino alle 22:00",
            occupancyPercentage = 55,
            crowdLevel = LibraryCrowd.Moderate,
            todayForecast = listOf(
                LibraryForecastPoint(LocalTime.of(8, 30), 25, rising = true, endTime = LocalTime.of(12, 0)),
                LibraryForecastPoint(LocalTime.of(12, 0), 65, rising = true, endTime = LocalTime.of(18, 0)),
                LibraryForecastPoint(LocalTime.of(18, 0), 30, rising = false, endTime = LocalTime.of(22, 0)),
            ),
            forecastDate = today,
        )
    }

    private fun centraleWeekHours(): LibraryWeekHours {
        val today = LocalDate.now(AppClock.clock)
        return LibraryWeekHours(
            days = (0L..6L).map { offset ->
                val day = today.plusDays(offset)
                LibraryDayHours(
                    day = day,
                    isToday = offset == 0L,
                    ranges = if (day.dayOfWeek == DayOfWeek.SUNDAY) {
                        emptyList()
                    } else {
                        listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(22, 0)))
                    },
                )
            },
        )
    }

    private fun centraleLibrary() = Library(
        id = LibraryCatalog.CENTRALE_ID,
        slug = "universita-bicocca-biblioteca-centrale",
        name = "Biblioteca Centrale",
        secondaryName = "Ed. U6 Agorà (Unimib)",
        address = "Piazza dell'Ateneo Nuovo 1, 20126 Milano MI, Italie",
        latitude = LibraryCatalog.Centrale.latitude,
        longitude = LibraryCatalog.Centrale.longitude,
        pictureUrl = null,
        phone = "+390264486251",
        email = "biblioteca.centrale@unimib.it",
        websiteUrl = "https://www.biblio.unimib.it/it/chi-siamo/sedi-e-orari",
        bookable = true,
        liveStatus = centraleLiveStatus(),
    )

    private fun upcomingReservation(): LibraryReservation {
        val now = LocalDateTime.now(AppClock.clock)
        return LibraryReservation(
            reservationId = 42,
            libraryName = "Biblioteca Centrale",
            librarySecondaryName = "Ed. U6 Agorà (Unimib)",
            seatName = "R 42 - Posto con presa elettrica",
            start = now.plusHours(2),
            end = now.plusHours(4),
            note = null,
            reservationCode = "A1B2",
            cancellationToken = "tok-42",
            state = LibraryReservationState.Upcoming,
        )
    }

    private fun pastReservation(): LibraryReservation {
        val now = LocalDateTime.now(AppClock.clock)
        return LibraryReservation(
            reservationId = 77,
            libraryName = "Biblioteca di Medicina",
            librarySecondaryName = "Ed. U8 Asclepio (Unimib)",
            seatName = "Posto 101",
            start = now.minusDays(2).minusHours(2),
            end = now.minusDays(2),
            note = null,
            reservationCode = "P9K4",
            cancellationToken = null,
            state = LibraryReservationState.Past,
        )
    }

    private fun viewModel(linkedEmail: String? = null): LibraryViewModel {
        val centrale = centraleLibrary()
        coEvery { getLibraries() } returns listOf(centrale)
        coEvery { getLibrary(centrale.id) } returns centrale
        coEvery { getLibraryServices(centrale.id) } returns listOf(
            LibraryService(LibraryServiceType.Wifi, "wifi", "Wi-Fi"),
            LibraryService(LibraryServiceType.Power, "power", "Prese elettriche"),
        )
        coEvery { getLiveStatus(centrale.id) } returns centraleLiveStatus()
        coEvery { getWeekHours(centrale.id) } returns centraleWeekHours()
        every { observeReservations() } returns reservationsFlow
        coEvery { refreshReservations() } returns Unit
        every { observeLinkedEmail() } returns flowOf(linkedEmail)
        every { observeActiveAccount() } returns flowOf(null)
        every { observePendingLibraryAction() } returns MutableStateFlow(null)
        coEvery { getPendingState.pendingValidation() } returns null
        coEvery { getPendingState.bookingDraft() } returns null
        coEvery { getPreferredCampus() } returns null
        every { observeCrowdingHistory(any()) } returns flowOf(emptyMap())
        return LibraryViewModel(
            getLibraries, getLibrary, getLibraryServices, getLiveStatus, getWeekHours, getZones,
            getAgreements, getConstraints, getAvailableSeats, bookSeat, observeReservations,
            refreshReservations, cancelReservation, verifyPresenceUseCase, observeLinkedEmail,
            requestEmailValidation, confirmEmailValidation, logout, cancelBookingByToken,
            observeActiveAccount, observePendingLibraryAction, consumeLibraryAction,
            getPendingState, saveBookingDraft, clearBookingDraft, getPreferredCampus,
            recordLastUsedLibrary, observeCrowdingHistory,
        )
    }

    private fun setScreen(vm: LibraryViewModel) {
        compose.setBicoccaContent {
            LibraryPage(viewModel = vm)
        }
    }

    @Test
    fun signed_out_home_shows_libraries_cta_sign_in_button_bookings_and_services_rows() {
        setScreen(viewModel(linkedEmail = null))

        compose.onNodeWithTag(LibraryTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_SIGN_IN).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_LIBRARIES_CARD).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_BOOKINGS_ROW).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_SERVICES_ROW).assertIsDisplayed()
    }

    @Test
    fun tapping_libraries_card_opens_libraries_page_detail_floor_row_and_address_sheet() {
        val vm = viewModel(linkedEmail = null)
        setScreen(vm)

        compose.onNodeWithTag(LibraryTestTags.HOME_LIBRARIES_CARD).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.LIBRARIES_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.library(LibraryCatalog.CENTRALE_ID)).assertIsDisplayed()

        compose.onNodeWithTag(LibraryTestTags.library(LibraryCatalog.CENTRALE_ID)).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.DETAIL_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.DETAIL_CROWDING_CHART).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.DETAIL_INFO_FLOOR)
            .performScrollTo()
            .assertIsDisplayed()
            .assert(hasClickAction().not())

        compose.onNodeWithTag(LibraryTestTags.DETAIL_INFO_ADDRESS).performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.ADDRESS_CHOOSER_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.ADDRESS_CHOOSER_MAP, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.ADDRESS_CHOOSER_EXTERNAL, useUnmergedTree = true).assertIsDisplayed()

        compose.runOnUiThread { vm.dismissAddressChooser() }
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.ADDRESS_CHOOSER_PAGE).assertDoesNotExist()
        compose.onNodeWithTag(LibraryTestTags.DETAIL_PAGE).assertIsDisplayed()
    }

    @Test
    fun tapping_crowding_bar_moves_callout_toggles_and_restores_live() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-06-18T10:30:00Z"), ZoneOffset.UTC)
        try {
            setScreen(viewModel(linkedEmail = null))

            compose.onNodeWithTag(LibraryTestTags.HOME_LIBRARIES_CARD).performClick()
            compose.waitForIdle()
            compose.onNodeWithTag(LibraryTestTags.library(LibraryCatalog.CENTRALE_ID)).performClick()
            compose.waitForIdle()

            val liveText = InstrumentationRegistry.getInstrumentation().targetContext
                .getString(R.string.library_crowding_live, "10:30")
            compose.onNodeWithTag(LibraryTestTags.crowdingCallout(DayOfWeek.THURSDAY)).assertIsDisplayed()
            compose.onNodeWithText(liveText).assertIsDisplayed()

            compose.onNodeWithTag(LibraryTestTags.crowdingSlot(DayOfWeek.THURSDAY, 0)).performClick()
            compose.waitForIdle()

            compose.onNodeWithText("08:30–09:00").assertIsDisplayed()
            compose.onNodeWithText(liveText).assertDoesNotExist()

            compose.onNodeWithTag(LibraryTestTags.crowdingSlot(DayOfWeek.THURSDAY, 0)).performClick()
            compose.waitForIdle()

            compose.onNodeWithText(liveText).assertIsDisplayed()
            compose.onNodeWithText("08:30–09:00").assertDoesNotExist()

            compose.onNodeWithTag(LibraryTestTags.crowdingSlot(DayOfWeek.THURSDAY, 0)).performClick()
            compose.waitForIdle()
            compose.onNodeWithTag(LibraryTestTags.crowdingSlot(DayOfWeek.THURSDAY, 4)).performClick()
            compose.waitForIdle()

            compose.onNodeWithText(liveText).assertIsDisplayed()
        } finally {
            AppClock.clock = Clock.systemDefaultZone()
        }
    }

    @Test
    fun tapping_bookings_row_while_signed_out_opens_login() {
        setScreen(viewModel(linkedEmail = null))

        compose.onNodeWithTag(LibraryTestTags.HOME_BOOKINGS_ROW).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.LOGIN_PAGE).assertIsDisplayed()
    }

    @Test
    fun tapping_services_row_opens_services_directory() {
        setScreen(viewModel(linkedEmail = null))

        compose.onNodeWithTag(LibraryTestTags.HOME_SERVICES_ROW).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.SERVICES_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.service("accesso-e-consultazione")).assertIsDisplayed()
    }

    @Test
    fun signed_in_home_shows_account_button_and_split_libraries_and_next_booking_boxes() {
        reservationsFlow.value = listOf(upcomingReservation())
        setScreen(viewModel(linkedEmail = "m.rossi99@campus.unimib.it"))

        compose.onNodeWithTag(LibraryTestTags.HOME_ACCOUNT).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_LIBRARIES_CARD).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_NEXT_BOOKING).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_BOOKINGS_ROW).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.HOME_SERVICES_ROW).assertIsDisplayed()
    }

    @Test
    fun tapping_account_button_opens_account_page_with_logout_action() {
        setScreen(viewModel(linkedEmail = "m.rossi99@campus.unimib.it"))

        compose.onNodeWithTag(LibraryTestTags.HOME_ACCOUNT).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.ACCOUNT_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.ACCOUNT_LOGOUT).assertIsDisplayed()
    }

    @Test
    fun tapping_bookings_row_when_signed_in_opens_empty_bookings_page_with_tab_switch() {
        reservationsFlow.value = emptyList()
        setScreen(viewModel(linkedEmail = "m.rossi99@campus.unimib.it"))

        compose.onNodeWithTag(LibraryTestTags.HOME_BOOKINGS_ROW).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.BOOKINGS_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.BOOKINGS_EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.BOOKINGS_TAB_SWITCH).assertIsDisplayed()
    }

    @Test
    fun bookings_history_tab_opens_view_only_ended_booking_detail() {
        val past = pastReservation()
        reservationsFlow.value = listOf(upcomingReservation(), past)
        setScreen(viewModel(linkedEmail = "m.rossi99@campus.unimib.it"))

        compose.onNodeWithTag(LibraryTestTags.HOME_BOOKINGS_ROW).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.booking(42)).assertIsDisplayed()
        val historyLabel = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.library_bookings_tab_history)
        compose.onNodeWithText(historyLabel).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.booking(past.reservationId)).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_MAP).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_CANCEL, useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_VERIFY, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun active_booking_detail_shows_cancel_and_verify_button_pair() {
        val upcoming = upcomingReservation()
        reservationsFlow.value = listOf(upcoming)
        setScreen(viewModel(linkedEmail = "m.rossi99@campus.unimib.it"))

        compose.onNodeWithTag(LibraryTestTags.HOME_BOOKINGS_ROW).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.booking(upcoming.reservationId)).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_PAGE).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_MAP).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_CANCEL, useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithTag(LibraryTestTags.RESERVATION_DETAIL_VERIFY, useUnmergedTree = true).assertIsDisplayed()
    }
}
