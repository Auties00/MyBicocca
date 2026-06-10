package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionFile
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Saves the student's hand-in (online text and/or files) from the consegna editor as the
 * current submission. Existing files the student keeps must be passed alongside the new ones
 * because the platform replaces the whole file area on save. Refreshes the cached submission
 * status afterwards; throws on failure.
 */
class SaveSubmissionUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        assignmentId: AssignmentId,
        onlineText: String?,
        files: List<SubmissionFile>,
        keepFiles: List<Assignment.AttachmentRef>,
    ) = repository.saveSubmission(accountId, assignmentId, onlineText, files, keepFiles)
}
