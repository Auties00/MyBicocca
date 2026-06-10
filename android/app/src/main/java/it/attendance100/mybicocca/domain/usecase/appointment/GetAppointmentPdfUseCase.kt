package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

/**
 * Downloads the reservation summary PDF issued by Portale Planning when the user requests it
 * from a booking's detail, returning the raw bytes to view or share.
 */
class GetAppointmentPdfUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(entryId: Int): ByteArray =
        repository.getReservationPdf(entryId)
}
