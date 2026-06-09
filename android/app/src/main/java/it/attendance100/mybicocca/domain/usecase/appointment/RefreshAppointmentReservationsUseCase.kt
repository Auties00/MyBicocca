package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

class RefreshAppointmentReservationsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke() =
        repository.refreshReservations()
}
