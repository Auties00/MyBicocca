package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import javax.inject.Inject

class SetCourseFilterUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    suspend operator fun invoke(filter: CourseFilter) =
        repository.setFilter(filter)
}
