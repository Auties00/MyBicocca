package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Re-fetches the student's submission state for one assignment into the cache, keeping the
 * assignment detail sheet honest after hand-in actions or while a grade is awaited. Throws on
 * failure.
 */
class RefreshSubmissionStatusUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(accountId: AccountId, assignmentId: AssignmentId) =
        repository.refreshSubmissionStatus(accountId, assignmentId)
}
