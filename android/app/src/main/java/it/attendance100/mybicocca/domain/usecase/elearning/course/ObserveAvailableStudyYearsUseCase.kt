package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// The distinct study years the student's currently-enrolled, non-hidden courses resolve to
// via their idNumber (see CourseCode). Pure Room transform — no network. Unknown years are
// excluded; they don't surface as a chip.
class ObserveAvailableStudyYearsUseCase @Inject constructor(
    private val courseRepository: ElearningCourseRepository,
) {
    operator fun invoke(accountId: AccountId?): Flow<List<StudyYear>> {
        if (accountId == null) return flowOf(emptyList())
        return courseRepository.observeEnrolledCourses(accountId).map { coursesLoadable ->
            val courses = (coursesLoadable as? Loadable.Loaded)?.value ?: return@map emptyList()
            courses.asSequence()
                .filter { !it.hidden }
                .mapNotNull { it.courseCode().courseYear }
                .filter { it != StudyYear.Unknown }
                .distinct()
                .sorted()
                .toList()
        }
    }
}
