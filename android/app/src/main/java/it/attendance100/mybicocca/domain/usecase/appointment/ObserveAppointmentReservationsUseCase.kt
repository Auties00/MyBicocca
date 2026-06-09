package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAppointmentReservationsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    operator fun invoke(): Flow<List<AppointmentReservation>> =
        repository.observeReservations()
}
