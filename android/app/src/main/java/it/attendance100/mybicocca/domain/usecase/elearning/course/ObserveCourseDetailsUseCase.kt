package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseDetails
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams one course's full detail (sections, modules, staff, syllabus) hot from the
 * local cache for the course detail screen; emits not-yet-loaded until the course has
 * been cached at least once.
 */
class ObserveCourseDetailsUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    operator fun invoke(accountId: AccountId, courseId: CourseId): Flow<Loadable<CourseDetails>> =
        repository.observeCourseDetails(accountId, courseId)
}
