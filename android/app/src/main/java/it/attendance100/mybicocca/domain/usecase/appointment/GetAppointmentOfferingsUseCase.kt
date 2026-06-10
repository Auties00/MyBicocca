package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

/**
 * Loads the areas (and per-area constraints) where a selected service can be booked, for the
 * location step of the "Appuntamenti" booking flow.
 */
class GetAppointmentOfferingsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(serviceId: Int): List<AppointmentOffering> =
        repository.getOfferings(serviceId)
}
