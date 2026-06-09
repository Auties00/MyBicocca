package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

// Catalog and availability are intentionally NOT cached — slot occupancy is volatile and a
// stale grid sells slots that are already gone. Only reservations created from this device
// are persisted (Room), because the portal is anonymous and has no server-side listing.
interface AppointmentRepository {

    suspend fun getServices(): List<AppointmentService>

    suspend fun getOfferings(serviceId: Int): List<AppointmentOffering>

    suspend fun getMonthAvailability(
        serviceId: Int,
        areaId: Int,
        month: YearMonth,
        durationSeconds: Int,
    ): AppointmentMonthAvailability

    suspend fun getDaySlots(
        serviceId: Int,
        areaId: Int,
        date: LocalDate,
        durationSeconds: Int,
    ): List<AppointmentSlot>

    suspend fun getBookingForm(serviceId: Int): AppointmentForm

    // Creates the hold, finalizes it server-side and persists the reservation locally.
    // Values are keyed by field code and must include the primary (email) field.
    suspend fun bookAppointment(
        service: AppointmentService,
        offering: AppointmentOffering,
        form: AppointmentForm,
        slotStart: LocalDateTime,
        values: Map<String, String>,
    ): AppointmentReservation

    fun observeReservations(): Flow<List<AppointmentReservation>>

    // Re-validates locally-saved reservations against the portal, dropping the ones that
    // were cancelled elsewhere.
    suspend fun refreshReservations()

    suspend fun cancelReservation(reservation: AppointmentReservation)

    suspend fun getReservationPdf(entryId: Int): ByteArray
}
