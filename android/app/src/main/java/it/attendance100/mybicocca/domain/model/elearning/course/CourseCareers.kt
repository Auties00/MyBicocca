package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.account.AcademicIdentity
import it.attendance100.mybicocca.domain.model.career.Career
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

/**
 * Ties e-learning courses to the student's careers. Moodle enrolment knows nothing of careers, so
 * a student with a bachelor's and a master's is enrolled in both careers' courses; this tells
 * them apart from each course's idNumber alone, offline:
 * - a course whose activity code starts with a career's programme code ("F1802Q107" and
 *   "F1802Q") belongs to that career;
 * - otherwise the course's cohort, the year its students started (its academic year minus its
 *   year of study, plus one), points to the latest career started by then. That also places a
 *   course taken after the standard years, which points to a later cohort of the same career.
 *
 * A course without a year of study (a programme's announcements page, a tutoring space) belongs
 * to no career.
 */
class CourseCareers(
    private val careers: List<Career>,
    val selected: CareerId,
) {
    constructor(academic: AcademicIdentity) : this(academic.careers, academic.selectedCareerId)

    private val byStart = careers.filter { it.academicYear > 0 }.sortedBy { it.academicYear }

    fun ownerOf(code: CourseCode): CareerId? {
        val academicYear = code.academicYear ?: return null
        val year = code.courseYear ?: return null
        if (careers.size <= 1) return careers.firstOrNull()?.id ?: selected
        code.code?.let { activity ->
            careers.firstOrNull { career ->
                career.easyStaffProgramCode?.let { activity.startsWith(it, ignoreCase = true) } == true
            }?.let { return it.id }
        }
        val cohort = academicYear.startYear - year.value + 1
        return (byStart.lastOrNull { it.academicYear <= cohort } ?: byStart.firstOrNull())?.id ?: selected
    }

    fun ownerOf(course: EnrolledCourse): CareerId? = ownerOf(course.courseCode())

    /**
     * The filter chips [courses] call for: the selected career's years of study, and every other
     * career that owns one of them, most recent first.
     */
    fun filterOptions(courses: List<EnrolledCourse>): CourseFilterOptions {
        val owned = courses.filter { !it.hidden }.map { it to ownerOf(it) }
        val years = owned
            .filter { (_, owner) -> owner == selected }
            .mapNotNull { (course, _) -> course.courseCode().courseYear }
            .filter { it != StudyYear.Unknown }
            .distinct()
            .sorted()
        val owners = owned.mapNotNullTo(HashSet()) { (_, owner) -> owner }
        val others = careers
            .filter { it.id != selected && it.id in owners }
            .sortedByDescending { it.academicYear }
        return CourseFilterOptions(years = years, otherCareers = others)
    }
}

/**
 * The e-learning filter chips beyond "Tutti" and "Preferiti".
 *
 * @property years The selected career's years of study that have courses.
 * @property otherCareers The student's other careers that have courses, most recent first.
 */
data class CourseFilterOptions(
    val years: List<StudyYear>,
    val otherCareers: List<Career>,
) {
    /**
     * [filter] if it still has a chip, else [CourseFilter.All]: a persisted year or career can
     * disappear when the student switches career or a course is hidden.
     */
    fun resolve(filter: CourseFilter): CourseFilter = when (filter) {
        CourseFilter.All, CourseFilter.Favourites -> filter
        is CourseFilter.ByYear -> if (filter.year in years) filter else CourseFilter.All
        is CourseFilter.ByCareer -> if (otherCareers.any { it.id == filter.careerId }) filter else CourseFilter.All
    }

    companion object {
        val Empty = CourseFilterOptions(years = emptyList(), otherCareers = emptyList())
    }
}
