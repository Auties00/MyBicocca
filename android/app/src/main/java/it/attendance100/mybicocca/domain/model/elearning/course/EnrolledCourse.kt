package it.attendance100.mybicocca.domain.model.elearning.course

import it.attendance100.mybicocca.domain.model.elearning.deadline.Deadline
import java.time.Instant
import java.time.LocalDate

/**
 * A Moodle course the student is enrolled in, from the user-courses web service.
 * Backs the e-learning course list and is the root of the course detail screen.
 *
 * @property id The Moodle course id.
 * @property shortName Compact course name, with any {mlang} markup resolved.
 * @property fullName Full official course name, with any {mlang} markup resolved.
 * @property displayName Name Moodle designates for display; falls back to [fullName].
 * @property idNumber Administrative code in the form aaaa-annoCorso-esse3Code (academic
 * year, year of study, Esse3 activity code, optional "-T&lt;n&gt;" stream suffix); null
 * or free-form for non-teaching courses. Parsed via CourseCode for year grouping and
 * Esse3/calendar matching.
 * @property summary Course description HTML, when authored.
 * @property courseImageUrl Header image of the course card, when set.
 * @property format Moodle course format (e.g. "topics", "weeks").
 * @property language Forced course language, or null when the site default applies.
 * @property categoryId Id of the Moodle category containing the course.
 * @property progress Completion percentage in 0..100, or null when the course does not
 * report progress.
 * @property completed Whether Moodle marks the whole course as completed.
 * @property completionEnabled Whether completion tracking is active for the course.
 * @property startDate First day of the course, when set.
 * @property endDate Last day of the course, or null for open-ended courses.
 * @property lastAccessDate When the student last opened the course on the platform.
 * @property isFavourite Locally-stored favourite flag, toggled from the course list.
 * @property hidden Locally-stored visibility flag; hidden courses are excluded from the
 * course list and year chips.
 * @property deadlines Upcoming assignment/quiz deadlines of the course, joined in from
 * the Moodle calendar cache.
 */
data class EnrolledCourse(
    val id: CourseId,
    val shortName: String,
    val fullName: String,
    val displayName: String,
    val idNumber: String?,
    val summary: String?,
    val courseImageUrl: String?,
    val format: String?,
    val language: String?,
    val categoryId: Int?,
    val progress: Float?,
    val completed: Boolean,
    val completionEnabled: Boolean,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val lastAccessDate: Instant?,
    val isFavourite: Boolean,
    val hidden: Boolean,
    val deadlines: List<Deadline> = emptyList(),
)
