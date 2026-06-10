package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

/**
 * Loads the catalog of bookable desk services when the registry "Appuntamenti" booking flow
 * opens. Fetches live from Portale Planning — the catalog is never cached.
 */
class GetAppointmentServicesUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(): List<AppointmentService> =
        repository.getServices()
}
