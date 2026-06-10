package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.ExamCallKey
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Downloads the attendance certificate PDF (attestato di presenza) for a sat exam,
 * triggered from the booked-exams modal in the registry tab. Esse3 serves it only once
 * the outcome is published and responds 422 before that. Returns the raw PDF bytes and
 * throws on failure.
 */
class GetPresenceCertificateUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId, key: ExamCallKey, studentId: Long): ByteArray =
        repository.getPresenceCertificate(careerId, key, studentId)
}
