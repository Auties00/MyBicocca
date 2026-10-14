package it.attendance100.mybicocca.domain.usecase.elearning.course

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.map
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseCareers
import it.attendance100.mybicocca.domain.model.elearning.course.CourseFilter
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourse
import it.attendance100.mybicocca.domain.model.elearning.course.EnrolledCourseGroup
import it.attendance100.mybicocca.domain.model.elearning.course.courseCode
import it.attendance100.mybicocca.domain.repository.ElearningCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Streams the e-learning tab's course list: hidden-filtered, user-filtered,
 * edition-grouped courses, derived purely from the local cache plus the persisted
 * filter. The year of study and the career each course belongs to ([CourseCareers]) are
 * parsed from its idNumber, so this never touches the network — the list renders the
 * instant the local cache emits.
 */
class ObserveFilteredCoursesUseCase @Inject constructor(
    private val courseRepository: ElearningCourseRepository,
) {
    operator fun invoke(account: Account): Flow<Loadable<List<EnrolledCourseGroup>>> {
        val careers = CourseCareers(account.academic)
        return combine(
            courseRepository.observeEnrolledCourses(account.id),
            courseRepository.observeFilter(),
        ) { coursesLoadable, filter ->
            coursesLoadable.map { courses -> apply(courses, filter, careers) }
        }
    }

    /**
     * Filters and groups the cached courses. A year chip keeps the selected career's courses
     * of that year, a career chip that career's courses; a filter whose chip is gone falls back
     * to all. The unfiltered list puts the selected career first, then the other careers most
     * recent first, then courses of no career; within each, groups go by year of study. The
     * stable sort preserves Moodle order within ties.
     */
    private fun apply(
        courses: List<EnrolledCourse>,
        persisted: CourseFilter,
        careers: CourseCareers,
    ): List<EnrolledCourseGroup> {
        val options = careers.filterOptions(courses)
        val filter = options.resolve(persisted)
        val visible = courses.asSequence()
            .filter { !it.hidden }
            .filter {
                when (filter) {
                    CourseFilter.All -> true
                    CourseFilter.Favourites -> it.isFavourite
                    is CourseFilter.ByYear ->
                        it.courseCode().courseYear == filter.year && careers.ownerOf(it) == careers.selected
                    is CourseFilter.ByCareer -> careers.ownerOf(it) == filter.careerId
                }
            }
            .toList()

        val groups = groupEditions(visible)
        val careerRank: Map<CareerId, Int> = (listOf(careers.selected) + options.otherCareers.map { it.id })
            .withIndex()
            .associate { (index, id) -> id to index }
        val byYear = compareBy<EnrolledCourseGroup> { group ->
            group.latest.courseCode().courseYear?.value ?: Int.MAX_VALUE
        }
        return when (filter) {
            CourseFilter.All -> groups.sortedWith(
                compareBy<EnrolledCourseGroup> { group ->
                    careers.ownerOf(group.latest)?.let(careerRank::get) ?: Int.MAX_VALUE
                }.then(byYear)
            )
            is CourseFilter.ByCareer -> groups.sortedWith(byYear)
            else -> groups
        }
    }

    /**
     * Groups courses into edition sets. Two editions belong to the same course when
     * they share a parsed code OR an exact (whitespace-trimmed) name: Esse3 sometimes
     * reissues a course under a new code across years while the name is unchanged, and
     * occasionally rewords the name while keeping the code — neither key alone catches
     * every edition, so both are unioned. Union-find makes the relation transitive: a
     * chain A=code=B=name=C collapses into one group, and a course with neither a code
     * nor a name simply stays on its own. Groups keep the first-appearance (Moodle)
     * order; editions within a group are sorted latest-first by parsed academic year
     * with year-less editions at the bottom.
     */
    private fun groupEditions(courses: List<EnrolledCourse>): List<EnrolledCourseGroup> {
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

        val components = LinkedHashMap<Int, MutableList<EnrolledCourse>>()
        courses.forEachIndexed { i, course ->
            components.getOrPut(find(i)) { mutableListOf() } += course
        }
        return components.values.map { editions ->
            EnrolledCourseGroup(editions = editions.sortedByLatestEdition())
        }
    }

    /**
     * Latest academic year first; within a year the base course (no stream) precedes
     * its streams, T1 before T2.
     */
    private fun List<EnrolledCourse>.sortedByLatestEdition(): List<EnrolledCourse> =
        sortedWith(
            compareByDescending<EnrolledCourse> { it.courseCode().academicYear?.startYear ?: Int.MIN_VALUE }
                .thenBy { it.courseCode().stream ?: 0 }
        )
}
