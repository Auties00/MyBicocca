package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.valueOrNull
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Enrolled courses keyed by their activity code (the idNumber base segment, shared with
// the calendar's plan-derived activityCode), so an event can resolve its course editions
// with a plain map lookup. Editions are sorted best-first: latest academic year, then
// visible before hidden, then the base course before its streams.
class ObserveCoursesByActivityCodeUseCase @Inject constructor(
    private val repository: ElearningCourseRepository,
) {
    operator fun invoke(accountId: AccountId): Flow<Map<String, List<EnrolledCourse>>> =
        repository.observeEnrolledCourses(accountId).map { loadable ->
            loadable.valueOrNull().orEmpty()
                .filter { it.courseCode().code != null }
                .sortedWith(
                    compareByDescending<EnrolledCourse> { it.courseCode().academicYear?.startYear ?: Int.MIN_VALUE }
                        .thenBy { it.hidden }
                        .thenBy { it.courseCode().stream ?: 0 }
                )
                .groupBy { it.courseCode().code!! }
        }
}
