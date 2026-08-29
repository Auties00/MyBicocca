package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.usecase.account.ObserveActiveAccountUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.BookAppointmentUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.CancelAppointmentReservationUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentDaySlotsUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentFormUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentMonthAvailabilityUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentOfferingsUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentPdfUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.GetAppointmentServicesUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.ObserveAppointmentReservationsUseCase
import it.attendance100.mybicocca.domain.usecase.appointment.RefreshAppointmentReservationsUseCase
import it.attendance100.mybicocca.testing.setBicoccaContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

/**
 * UI coverage for the Appuntamenti sheet body: the reservations root renders its empty state with no
 * bookings and the list when bookings exist, tapping Prenota walks into the desk directory, and the
 * directory renders its sections once the services land. The screen is driven by a real
 * [AppointmentsViewModel] over MockK-faked use cases (reusing the Wave 1 construction), anchored on
 * [AppointmentsTestTags], and wrapped in the shared [setBicoccaContent] environment (BicoccaTheme plus
 * the app-wide HapticManager / snackbar / device-type locals the sheet pages read).
 *
 * The empty-state surface is a fixed 400dp box pinned above the "Prenota" footer so the sheet cannot
 * stretch to full height; the tall-screen [Config.qualifiers] gives the headless window enough height
 * to lay out the whole sheet body so that footer is on-screen rather than clipped below the fold.
 */
@RunWith(AndroidJUnit4::class)
class AppointmentsPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val getServices: GetAppointmentServicesUseCase = mockk()
    private val observeReservations: ObserveAppointmentReservationsUseCase = mockk()
    private val refreshReservations: RefreshAppointmentReservationsUseCase = mockk(relaxed = true)
    private val cancelReservation: CancelAppointmentReservationUseCase = mockk(relaxed = true)
    private val getReservationPdf: GetAppointmentPdfUseCase = mockk()
    private val getOfferings: GetAppointmentOfferingsUseCase = mockk(relaxed = true)
    private val getMonthAvailability: GetAppointmentMonthAvailabilityUseCase = mockk(relaxed = true)
    private val getDaySlots: GetAppointmentDaySlotsUseCase = mockk(relaxed = true)
    private val getForm: GetAppointmentFormUseCase = mockk(relaxed = true)
    private val bookAppointment: BookAppointmentUseCase = mockk(relaxed = true)
    private val observeActiveAccount: ObserveActiveAccountUseCase = mockk(relaxed = true)

    private val reservationsFlow = MutableStateFlow<List<AppointmentReservation>>(emptyList())

    private fun service(id: Int) = AppointmentService(
        id = id,
        name = "Sportello $id",
        group = "Carriere studenti",
        descriptionHtml = null,
        durationSeconds = 1200,
    )

    private fun reservation(code: String) = AppointmentReservation(
        code = code,
        email = "mario.rossi@campus.unimib.it",
        entryId = 99,
        serviceId = 1,
        serviceName = "Sportello 1",
        serviceGroup = "Carriere Studenti",
        areaName = "U6",
        areaAddress = "Piazza Ateneo Nuovo",
        start = LocalDateTime.of(2026, 6, 20, 10, 0),
        end = LocalDateTime.of(2026, 6, 20, 10, 20),
        qrCodeDataUrl = null,
        webConferenceUrl = null,
    )

    private fun viewModel(): AppointmentsViewModel {
        every { observeReservations() } returns reservationsFlow
        every { observeActiveAccount() } returns flowOf(null)
        return AppointmentsViewModel(
            getServices, observeReservations, refreshReservations, cancelReservation,
            getReservationPdf, getOfferings, getMonthAvailability, getDaySlots, getForm,
            bookAppointment, observeActiveAccount,
        )
    }

    private fun setScreen() {
        val vm = viewModel()
        compose.setBicoccaContent {
            AppointmentsPage(viewModel = vm, onOpenPdf = mockk(relaxed = true))
        }
    }

    @Test
    fun the_reservations_root_shows_the_empty_state_with_no_bookings() {
        coEvery { getServices() } returns emptyList()
        setScreen()

        compose.onNodeWithTag(AppointmentsTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(AppointmentsTestTags.RESERVATIONS_EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(AppointmentsTestTags.PRENOTA_BUTTON).assertIsDisplayed()
    }

    @Test
    fun the_reservations_root_lists_the_bookings_when_present() {
        coEvery { getServices() } returns emptyList()
        reservationsFlow.value = listOf(reservation("ABC"))
        setScreen()

        compose.onNodeWithTag(AppointmentsTestTags.RESERVATIONS_LIST).assertIsDisplayed()
    }

    @Test
    fun tapping_Prenota_walks_into_the_sections_directory() {
        coEvery { getServices() } returns listOf(service(1))
        setScreen()

        compose.onNodeWithTag(AppointmentsTestTags.PRENOTA_BUTTON).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(AppointmentsTestTags.SECTIONS_LIST).assertIsDisplayed()
    }

    @Test
    fun the_sections_directory_renders_a_tile_per_macro_section() {
        coEvery { getServices() } returns listOf(service(1))
        setScreen()

        compose.onNodeWithTag(AppointmentsTestTags.PRENOTA_BUTTON).performClick()
        compose.waitForIdle()

        val expectedLabel =
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.getString(
                it.attendance100.mybicocca.R.string.appointments_section_label_career
            )
        compose.onNodeWithTag(AppointmentsTestTags.section(expectedLabel)).assertIsDisplayed()
    }
}
