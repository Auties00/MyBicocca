package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Loads the time slots of a chosen day, for the slot-picker step of the "Appuntamenti" booking
 * flow. Always fetched live — slot occupancy is volatile.
 */
class GetAppointmentDaySlotsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        serviceId: Int,
        areaId: Int,
        date: LocalDate,
        durationSeconds: Int,
    ): List<AppointmentSlot> =
        repository.getDaySlots(serviceId, areaId, date, durationSeconds)
}
