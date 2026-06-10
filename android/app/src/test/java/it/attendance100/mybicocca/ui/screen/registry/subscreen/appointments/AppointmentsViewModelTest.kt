package it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
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
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentsEvent
import it.attendance100.mybicocca.ui.screen.registry.subscreen.appointments.state.AppointmentsPage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime

/**
 * State-machine coverage for the Appuntamenti modal owner: the in-sheet back-stack
 * (push/back/resetNavigation), the services fetch state, the offline-cached reservations
 * observe mapping, the single-flight cancel/download guards, and the cancel / PDF one-shot
 * events.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
        group = "Carriere Studenti",
        descriptionHtml = null,
        durationSeconds = 1200,
    )

    private fun reservation(code: String, entryId: Int? = 99) = AppointmentReservation(
        code = code,
        email = "mario.rossi@campus.unimib.it",
        entryId = entryId,
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
        coEvery { getServices() } returns emptyList()
        return AppointmentsViewModel(
            getServices, observeReservations, refreshReservations, cancelReservation,
            getReservationPdf, getOfferings, getMonthAvailability, getDaySlots, getForm,
            bookAppointment, observeActiveAccount,
        )
    }

    @Test
    fun `the back stack starts at the reservations root`() = runTest {
        val vm = viewModel()

        assertThat(vm.backStack.value).containsExactly(AppointmentsPage.Reservations)
    }

    @Test
    fun `services reach Loaded after the init fetch`() = runTest {
        every { observeReservations() } returns reservationsFlow
        every { observeActiveAccount() } returns flowOf(null)
        coEvery { getServices() } returns listOf(service(1), service(2))

        val vm = AppointmentsViewModel(
            getServices, observeReservations, refreshReservations, cancelReservation,
            getReservationPdf, getOfferings, getMonthAvailability, getDaySlots, getForm,
            bookAppointment, observeActiveAccount,
        )

        assertThat((vm.services.value as Loadable.Loaded).value).hasSize(2)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `a services failure sets Failed and the init also refreshes reservations`() = runTest {
        every { observeReservations() } returns reservationsFlow
        every { observeActiveAccount() } returns flowOf(null)
        coEvery { getServices() } throws IOException("offline")

        val vm = AppointmentsViewModel(
            getServices, observeReservations, refreshReservations, cancelReservation,
            getReservationPdf, getOfferings, getMonthAvailability, getDaySlots, getForm,
            bookAppointment, observeActiveAccount,
        )

        assertThat(vm.services.value).isEqualTo(Loadable.NotYetLoaded)
        assertThat(vm.syncStatus.value).isInstanceOf(SyncStatus.Failed::class.java)
        coVerify { refreshReservations() }
    }

    @Test
    fun `reservations stream maps the Room list to Loaded`() = runTest {
        reservationsFlow.value = listOf(reservation("ABC"))
        val vm = viewModel()

        vm.reservations.test {
            var item = awaitItem()
            while (item is Loadable.NotYetLoaded) item = awaitItem()
            assertThat(item).isInstanceOf(Loadable.Loaded::class.java)
            assertThat((item as Loadable.Loaded).value).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openSections and openReservation push pages onto the stack`() = runTest {
        val vm = viewModel()

        vm.openSections()
        vm.openReservation(reservation("XYZ"))

        assertThat(vm.backStack.value).hasSize(3)
        assertThat(vm.backStack.value.last()).isEqualTo(AppointmentsPage.ReservationDetail("XYZ"))
    }

    @Test
    fun `back pops one page and stops at the root`() = runTest {
        val vm = viewModel()
        vm.openSections()

        vm.back()
        vm.back()

        assertThat(vm.backStack.value).containsExactly(AppointmentsPage.Reservations)
    }

    @Test
    fun `openSection pushes a Types page tagged with the section name`() = runTest {
        val vm = viewModel()

        vm.openSection("Didattica")

        assertThat(vm.backStack.value.last()).isEqualTo(AppointmentsPage.Types("Didattica"))
    }

    @Test
    fun `resetNavigation rewinds to the root`() = runTest {
        val vm = viewModel()
        vm.openSections()
        vm.openSection("Didattica")

        vm.resetNavigation()

        assertThat(vm.backStack.value).containsExactly(AppointmentsPage.Reservations)
        assertThat(vm.bookingService.value).isNull()
    }

    @Test
    fun `cancel emits ReservationCancelled and clears the in-flight code`() = runTest {
        val vm = viewModel()

        vm.events.test {
            vm.cancel(reservation("ABC"))
            assertThat(awaitItem()).isEqualTo(AppointmentsEvent.ReservationCancelled)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.cancellingCode.value).isNull()
    }

    @Test
    fun `cancel failure emits CancelFailed`() = runTest {
        val boom = IOException("offline")
        coEvery { cancelReservation(any()) } throws boom
        val vm = viewModel()

        vm.events.test {
            vm.cancel(reservation("ABC"))
            val event = awaitItem()
            assertThat(event).isInstanceOf(AppointmentsEvent.CancelFailed::class.java)
            assertThat((event as AppointmentsEvent.CancelFailed).cause).isEqualTo(boom)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `downloadPdf is a no-op when the reservation has no entry id`() = runTest {
        val vm = viewModel()

        vm.downloadPdf(reservation("ABC", entryId = null))

        assertThat(vm.downloadingPdfCode.value).isNull()
        coVerify(exactly = 0) { getReservationPdf(any()) }
    }

    @Test
    fun `downloadPdf emits PdfReady with the code-named file`() = runTest {
        coEvery { getReservationPdf(99) } returns byteArrayOf(1, 2)
        val vm = viewModel()

        vm.events.test {
            vm.downloadPdf(reservation("ABC"))
            val event = awaitItem()
            assertThat(event).isInstanceOf(AppointmentsEvent.PdfReady::class.java)
            assertThat((event as AppointmentsEvent.PdfReady).fileName).isEqualTo("Appuntamento-ABC.pdf")
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.downloadingPdfCode.value).isNull()
    }

    @Test
    fun `goToForm refuses to advance without a selected slot`() = runTest {
        val vm = viewModel()

        vm.goToForm()

        assertThat(vm.backStack.value).containsExactly(AppointmentsPage.Reservations)
    }

    @Test
    fun `setFieldValue stores the value keyed by field code`() = runTest {
        val vm = viewModel()

        vm.setFieldValue("email", "a@b.it")

        assertThat(vm.values.value["email"]).isEqualTo("a@b.it")
    }
}
