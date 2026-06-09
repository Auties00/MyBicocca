package it.attendance100.mybicocca.domain.usecase.appointment

import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import javax.inject.Inject

class GetAppointmentPdfUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(entryId: Int): ByteArray =
        repository.getReservationPdf(entryId)
}
