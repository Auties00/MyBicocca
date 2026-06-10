package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Downloads the booking slip PDF (statino di prenotazione) for a booked exam, triggered
 * from the booked-exams modal in the registry tab. Available for any booking; returns
 * the raw PDF bytes and throws on failure.
 */
class GetBookingSlipUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId, key: ExamCallKey, studentId: Long): ByteArray =
        repository.getBookingSlip(careerId, key, studentId)
}
