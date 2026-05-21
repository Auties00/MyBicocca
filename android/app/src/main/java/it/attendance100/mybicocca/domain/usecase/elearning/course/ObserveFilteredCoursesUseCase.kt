package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.map
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import it.attendance100.mybicocca.domain.repository.StudyPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveFilteredCoursesUseCase @Inject constructor(
    private val courseRepository: ElearningCourseRepository,
    private val studyPlanRepository: StudyPlanRepository,
) {
    operator fun invoke(
        accountId: AccountId,
        careerId: CareerId?,
    ): Flow<Loadable<List<EnrolledCourseGroup>>> = combine(
        courseRepository.observeEnrolledCourses(accountId),
        courseRepository.observeFilter(),
        yearByCodeFlow(careerId),
    ) { coursesLoadable, filter, yearByCode ->
        coursesLoadable.map { courses -> apply(courses, yearByCode, filter) }
    }

    private fun yearByCodeFlow(careerId: CareerId?): Flow<Map<String, StudyYear>> =
        if (careerId == null) flowOf(emptyMap())
        else flow { emit(studyPlanRepository.getActivityYearByCodeForCareer(careerId)) }

    private fun apply(
        courses: List<EnrolledCourse>,
        yearByCode: Map<String, StudyYear>,
        filter: CourseFilter,
    ): List<EnrolledCourseGroup> {
        val resolvedYearByCourse: Map<CourseId, StudyYear> = courses
            .mapNotNull { c -> c.courseCode().code?.let { code -> yearByCode[code]?.let { c.id to it } } }
            .toMap()

        val visible = courses.asSequence()
            .filter { !it.hidden }
            .filter {
                when (filter) {
                    CourseFilter.All -> true
                    CourseFilter.Favourites -> it.isFavourite
                    is CourseFilter.ByYear -> resolvedYearByCourse[it.id] == filter.year
                }
            }
            .toList()

        val groups = groupEditions(visible)
        return when (filter) {
            CourseFilter.All -> groups.sortedBy { group ->
                // Sort by the group's representative study year ascending, putting
                // unmatched groups at the end. Within ties, preserve insertion order.
                resolvedYearByCourse[group.latest.id]?.value ?: Int.MAX_VALUE
            }
            else -> groups
        }
    }

    private fun groupEditions(courses: List<EnrolledCourse>): List<EnrolledCourseGroup> {
        val singletons = mutableListOf<EnrolledCourse>()
        val byCode = LinkedHashMap<String, MutableList<EnrolledCourse>>()
        for (course in courses) {
            val code = course.courseCode().code
            if (code == null) singletons += course
            else byCode.getOrPut(code) { mutableListOf() } += course
        }
        // Order: codes grouped together (insertion order from first hit), with
        // unkeyed singletons appended at the end. Editions sorted latest-first by
        // parsed academic year; null years sort to the bottom of the edition list.
        val grouped = byCode.values.map { editions ->
            EnrolledCourseGroup(editions = editions.sortedByLatestEdition())
        }
        val unkeyed = singletons.map { EnrolledCourseGroup(editions = listOf(it)) }
        return grouped + unkeyed
    }

    private fun List<EnrolledCourse>.sortedByLatestEdition(): List<EnrolledCourse> =
        sortedByDescending { it.courseCode().academicYear?.startYear ?: Int.MIN_VALUE }
}
