package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.AssignmentId
import it.attendance100.mybicocca.domain.model.elearning.assignment.SubmissionForm
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Fetches the submission editor model fresh from the e-learning platform when the consegna
 * editor opens — never cached, because editability is time- and lock-sensitive. Throws on
 * failure.
 */
class LoadSubmissionFormUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(
        accountId: AccountId,
        courseId: CourseId,
        assignmentId: AssignmentId,
    ): SubmissionForm = repository.loadSubmissionForm(accountId, courseId, assignmentId)
}
