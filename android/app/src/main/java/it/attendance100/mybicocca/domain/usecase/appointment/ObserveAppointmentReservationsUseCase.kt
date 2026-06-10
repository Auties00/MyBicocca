package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the reservations booked from this device, ordered by start time, for the bookings
 * list of the registry "Appuntamenti" sub-screen. Backed by the device-local Room store.
 */
class ObserveAppointmentReservationsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    operator fun invoke(): Flow<List<AppointmentReservation>> =
        repository.observeReservations()
}
