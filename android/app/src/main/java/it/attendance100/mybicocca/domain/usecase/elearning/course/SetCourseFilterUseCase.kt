package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

/**
 * Persists the course-list filter when the user taps a filter chip on the e-learning
 * tab; the filtered course stream reacts immediately.
 */
class SetCourseFilterUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(filter: CourseFilter) =
        repository.setFilter(filter)
}
