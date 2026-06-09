package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

class CancelAppointmentReservationUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(reservation: AppointmentReservation) =
        repository.cancelReservation(reservation)
}
