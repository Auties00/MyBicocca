package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import javax.inject.Inject

/**
 * Syncs a course's assignments, each with the student's submission status, from the e-learning
 * platform into the cache when the course detail screen opens or is refreshed. Skipped while
 * the cache is fresh unless forced; throws on failure.
 */
class RefreshCourseAssignmentsUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    suspend operator fun invoke(accountId: AccountId, courseId: CourseId, force: Boolean = false) =
        repository.refreshForCourse(accountId, courseId, force)
}
