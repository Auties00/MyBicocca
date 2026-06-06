package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.map
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

// Hidden-filtered, user-filtered, edition-grouped courses, derived purely from the local
// cache + the persisted filter. The course year used for grouping and the year filter is
// parsed from each course's idNumber (see CourseCode), so this never touches the network —
// the list renders the instant Room emits.
class ObserveFilteredCoursesUseCase @Inject constructor(
    private val courseRepository: ElearningCourseRepository,
) {
    operator fun invoke(accountId: AccountId): Flow<Loadable<List<EnrolledCourseGroup>>> = combine(
        courseRepository.observeEnrolledCourses(accountId),
        courseRepository.observeFilter(),
    ) { coursesLoadable, filter ->
        coursesLoadable.map { courses -> apply(courses, filter) }
    }

    private fun apply(
        courses: List<EnrolledCourse>,
        filter: CourseFilter,
    ): List<EnrolledCourseGroup> {
        val visible = courses.asSequence()
            .filter { !it.hidden }
            .filter {
                when (filter) {
                    CourseFilter.All -> true
                    CourseFilter.Favourites -> it.isFavourite
                    is CourseFilter.ByYear -> it.courseCode().courseYear == filter.year
                }
            }
            .toList()

        val groups = groupEditions(visible)
        return when (filter) {
            // Sort by the group's representative course year ascending, unmatched groups
            // last. Stable sort preserves Moodle order within ties.
            CourseFilter.All -> groups.sortedBy { group ->
                group.latest.courseCode().courseYear?.value ?: Int.MAX_VALUE
            }
            else -> groups
        }
    }

    private fun groupEditions(courses: List<EnrolledCourse>): List<EnrolledCourseGroup> {
        // Two editions belong to the same course when they share a parsed code OR an exact
        // (whitespace-trimmed) name. Esse3 sometimes reissues a course under a new code
        // across years while the name is unchanged, and occasionally rewords the name while
        // keeping the code — neither key alone catches every edition, so we union both.
        // Union-find makes the relation transitive: a chain A=code=B=name=C collapses into
        // one group. A course with neither a code nor a name simply stays on its own.
        val parent = IntArray(courses.size) { it }
        fun find(index: Int): Int {
            var root = index
            while (parent[root] != root) root = parent[root]
            var node = index
            while (parent[node] != node) {
                val next = parent[node]
                parent[node] = root
                node = next
            }
            return root
        }
        fun union(a: Int, b: Int) { parent[find(a)] = find(b) }

        val seedByCode = HashMap<String, Int>()
        val seedByName = HashMap<String, Int>()
        courses.forEachIndexed { i, course ->
            course.courseCode().code?.let { code ->
                seedByCode.putIfAbsent(code, i)?.let { union(it, i) }
            }
            course.fullName.trim().takeIf { it.isNotBlank() }?.let { name ->
                seedByName.putIfAbsent(name, i)?.let { union(it, i) }
            }
        }

        // Group by component, keeping the first-appearance (Moodle) order of each group.
        // Editions within a group are sorted latest-first by parsed academic year; null
        // years sort to the bottom of the edition list.
        val components = LinkedHashMap<Int, MutableList<EnrolledCourse>>()
        courses.forEachIndexed { i, course ->
            components.getOrPut(find(i)) { mutableListOf() } += course
        }
        return components.values.map { editions ->
            EnrolledCourseGroup(editions = editions.sortedByLatestEdition())
        }
    }

    // Latest academic year first; within a year the base course (no stream) precedes its
    // streams, T1 before T2.
    private fun List<EnrolledCourse>.sortedByLatestEdition(): List<EnrolledCourse> =
        sortedWith(
            compareByDescending<EnrolledCourse> { it.courseCode().academicYear?.startYear ?: Int.MIN_VALUE }
                .thenBy { it.courseCode().stream ?: 0 }
        )
}
