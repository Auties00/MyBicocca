package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import java.time.LocalDate
import javax.inject.Inject

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
