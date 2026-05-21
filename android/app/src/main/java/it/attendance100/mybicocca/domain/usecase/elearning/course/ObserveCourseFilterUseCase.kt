package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCourseFilterUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    operator fun invoke(): Flow<CourseFilter> = repository.observeFilter()
}
