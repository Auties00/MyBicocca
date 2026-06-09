package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

class GetAppointmentServicesUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(): List<AppointmentService> =
        repository.getServices()
}
