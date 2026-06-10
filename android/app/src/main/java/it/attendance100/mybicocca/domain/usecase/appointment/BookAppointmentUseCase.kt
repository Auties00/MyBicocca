package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Books the chosen slot when the user confirms the "Appuntamenti" booking flow. Creates and
 * finalizes the reservation on Portale Planning and persists it in the device-local Room
 * store, then returns it for the confirmation page. The values map holds the form input keyed
 * by field code and must include the primary email field.
 */
class BookAppointmentUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        service: AppointmentService,
        offering: AppointmentOffering,
        form: AppointmentForm,
        slotStart: LocalDateTime,
        values: Map<String, String>,
    ): AppointmentReservation =
        repository.bookAppointment(service, offering, form, slotStart, values)
}
