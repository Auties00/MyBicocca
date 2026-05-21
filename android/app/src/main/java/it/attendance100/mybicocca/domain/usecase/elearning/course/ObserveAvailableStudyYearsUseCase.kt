package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

// Emits the distinct study years that the student's currently-enrolled,
// non-hidden courses actually resolve to via the combined plan+program
// code->year map. StudyYear.Unknown is excluded — it doesn't surface as a chip.
class ObserveAvailableStudyYearsUseCase @Inject constructor(
    private val courseRepository: ElearningCourseRepository,
    private val studyPlanRepository: StudyPlanRepository,
) {
    operator fun invoke(
        accountId: AccountId?,
        careerId: CareerId?,
    ): Flow<List<StudyYear>> {
        if (accountId == null) return flowOf(emptyList())
        return combine(
            courseRepository.observeEnrolledCourses(accountId),
            yearByCodeFlow(careerId),
        ) { coursesLoadable, yearByCode ->
            val courses = (coursesLoadable as? Loadable.Loaded)?.value ?: return@combine emptyList()
            courses.asSequence()
                .filter { !it.hidden }
                .mapNotNull { it.courseCode().code?.let(yearByCode::get) }
                .filter { it != StudyYear.Unknown }
                .distinct()
                .sorted()
                .toList()
        }
    }

    private fun yearByCodeFlow(careerId: CareerId?): Flow<Map<String, StudyYear>> =
        if (careerId == null) flowOf(emptyMap())
        else flow { emit(studyPlanRepository.getActivityYearByCodeForCareer(careerId)) }
}
