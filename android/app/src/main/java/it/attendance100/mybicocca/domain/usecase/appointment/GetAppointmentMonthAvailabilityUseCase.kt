package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import java.time.YearMonth
import javax.inject.Inject

/**
 * Loads which days of a month still have free slots, as the user pages through the calendar of
 * the "Appuntamenti" booking flow. Always fetched live — slot occupancy is volatile.
 */
class GetAppointmentMonthAvailabilityUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        serviceId: Int,
        areaId: Int,
        month: YearMonth,
        durationSeconds: Int,
    ): AppointmentMonthAvailability =
        repository.getMonthAvailability(serviceId, areaId, month, durationSeconds)
}
