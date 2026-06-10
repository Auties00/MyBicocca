package it.attendance100.mybicocca.domain.usecase.exam

import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.ExamRepository
import javax.inject.Inject

/**
 * Accepts a published exam outcome (presa visione "A") on the student's behalf,
 * triggered from the exam-results sheet in the registry tab. Accepting lets Esse3
 * proceed with verbalising the grade; the decision is irreversible and only possible
 * while the outcome's acknowledgment window is open. Throws on failure.
 */
class AcceptExamResultUseCase @Inject constructor(
    private val repository: ExamRepository,
) {
    suspend operator fun invoke(careerId: CareerId, applicationListId: Long) =
        repository.acknowledgeExamResult(careerId, applicationListId, accept = true)
}
