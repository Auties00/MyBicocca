package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.elearning.course.CourseCareers
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilterOptions
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Streams the e-learning tab's filter chips for [Account]: the selected career's years of study
 * and the student's other careers that have courses (see [CourseCareers]). A pure local-cache
 * transform with no network use; courses without a year of study never surface as a chip.
 */
class ObserveCourseFilterOptionsUseCase @Inject constructor(
    private val courseRepository: ElearningCourseRepository,
) {
    operator fun invoke(account: Account?): Flow<CourseFilterOptions> {
        if (account == null) return flowOf(CourseFilterOptions.Empty)
        val careers = CourseCareers(account.academic)
        return courseRepository.observeEnrolledCourses(account.id).map { coursesLoadable ->
            val courses = (coursesLoadable as? Loadable.Loaded)?.value ?: return@map CourseFilterOptions.Empty
            careers.filterOptions(courses)
        }
    }
}
