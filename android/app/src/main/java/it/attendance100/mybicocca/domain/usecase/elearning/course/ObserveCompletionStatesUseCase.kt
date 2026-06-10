package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CompletionState
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the per-activity completion states of a course, keyed by course-module id,
 * hot from the local cache. Drives the checkmarks and progress indicators on the
 * course detail screen.
 */
class ObserveCompletionStatesUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId): Flow<Map<Int, CompletionState>> =
        repository.observeCompletionStates(accountId, courseId)
}
