package it.attendance100.mybicocca.domain.model.elearning.grade

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId

/**
 * The course-total grade of one enrolled course, from the Moodle grades-overview web
 * service. Feeds the cross-course grade summary in the e-learning tab.
 *
 * @property courseId The graded course.
 * @property courseName Course display name; may be empty when the overview endpoint
 * doesn't carry it, in which case the UI resolves it from the enrolled-course cache.
 * @property grade The numeric course total, or null when no grade is published.
 * @property maxGrade Maximum achievable total, when known.
 * @property gradeFormatted The total exactly as the gradebook formats it.
 */
data class CourseGradeOverview(
    val courseId: CourseId,
    val courseName: String,
    val grade: Double?,
    val maxGrade: Double?,
    val gradeFormatted: String?,
)
