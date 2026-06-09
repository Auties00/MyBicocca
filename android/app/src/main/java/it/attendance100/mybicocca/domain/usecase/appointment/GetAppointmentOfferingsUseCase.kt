package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

class GetAppointmentOfferingsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(serviceId: Int): List<AppointmentOffering> =
        repository.getOfferings(serviceId)
}
