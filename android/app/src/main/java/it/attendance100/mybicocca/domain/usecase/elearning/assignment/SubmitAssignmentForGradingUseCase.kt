package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Finalizes the student's draft for grading from the assignment detail sheet, optionally
 * accepting the submission statement; irreversible from the app. Refreshes the cached
 * submission status afterwards; throws on failure.
 */
class SubmitAssignmentForGradingUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        assignmentId: AssignmentId,
        acceptStatement: Boolean,
    ) = repository.submitForGrading(accountId, assignmentId, acceptStatement)
}
