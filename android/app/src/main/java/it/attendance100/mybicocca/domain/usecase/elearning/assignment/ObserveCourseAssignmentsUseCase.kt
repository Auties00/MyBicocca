package it.attendance100.mybicocca.domain.usecase.elearning.assignment

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.assignment.Assignment
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningAssignmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams a course's cached assignments, sorted by due date, backing the activity rows of the
 * course detail screen.
 */
class ObserveCourseAssignmentsUseCase @Inject constructor(
    private val repository: ElearningAssignmentRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Assignment>>> =
        repository.observeForCourse(accountId, courseId)
}
