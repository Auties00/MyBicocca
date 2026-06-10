package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Discards the student's current submission from the assignment detail sheet, reverting the
 * hand-in to empty. Refreshes the cached submission status afterwards; throws on failure.
 */
class RemoveSubmissionUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(accountId: AccountId, assignmentId: AssignmentId) =
        repository.removeSubmission(accountId, assignmentId)
}
