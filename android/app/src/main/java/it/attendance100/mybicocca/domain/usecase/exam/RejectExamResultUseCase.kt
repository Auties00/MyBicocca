package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Rejects a published exam outcome (presa visione "R") on the student's behalf,
 * triggered from the exam-results sheet in the registry tab. Rejecting renounces the
 * grade so the exam can be retaken; only possible while the outcome's acknowledgment
 * window is open, after which Esse3 auto-verbalises. Throws on failure.
 */
class RejectExamResultUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId, applicationListId: Long) =
        repository.acknowledgeExamResult(careerId, applicationListId, accept = false)
}
