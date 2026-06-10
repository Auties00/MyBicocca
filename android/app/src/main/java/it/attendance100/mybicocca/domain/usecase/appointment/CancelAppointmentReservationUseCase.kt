package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

/**
 * Cancels a reservation when the user asks to from the "Appuntamenti" bookings list. Deletes
 * the booking on Portale Planning (authorized by code + email) and removes it from the
 * device-local store.
 */
class CancelAppointmentReservationUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(reservation: AppointmentReservation) =
        repository.cancelReservation(reservation)
}
