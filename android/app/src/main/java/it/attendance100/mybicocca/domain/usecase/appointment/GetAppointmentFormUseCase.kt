package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

class GetAppointmentFormUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(serviceId: Int): AppointmentForm =
        repository.getBookingForm(serviceId)
}
