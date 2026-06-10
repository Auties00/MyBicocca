package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the persisted course-list filter so the e-learning tab's filter chips
 * restore the user's last selection across sessions.
 */
class ObserveCourseFilterUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    operator fun invoke(): Flow<CourseFilter> = repository.observeFilter()
}
