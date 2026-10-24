package it.attendance100.mybicocca.data.remote.elearning.dto

import kotlinx.serialization.Serializable

/**
 * Public, no-authentication information scraped from `/course/info.php?id={id}`.
 *
 * @property id The unique identifier of the course (matches `data-courseid`).
 * @property name The full display name of the course.
 * @property code The short course code (e.g. "2526-3-E3101Q113").
 * @property viewUrl URL to the (gated) course content page.
 * @property syllabus Per-language syllabus content (typically one entry for "it" and one for "en").
 * @property staff Course staff grouped by role ("Docente", "Tutor", "Esercitatore", ...).
 * @property enrolmentMethods Localized names of available enrolment methods.
 * @property studentOpinionUrl URL to the previous-year course evaluation, if exposed.
 * @property bibliographyUrl URL to the library catalog search for this course, if exposed.
 */
@Serializable
data class ElearningCoursePublicInfo(
    val id: Int,
    val name: String,
    val code: String,
    val viewUrl: String,
    val syllabus: List<ElearningCourseSyllabus>,
    val staff: List<ElearningCourseStaffGroup>,
    val enrolmentMethods: List<String>,
    val studentOpinionUrl: String?,
    val bibliographyUrl: String?
)

/**
 * Syllabus content for a single language tab on the public info page.
 *
 * @property language Language code from the tab id (typically "it" or "en").
 * @property exportPdfUrl URL to the syllabus PDF export for this language.
 * @property fields Ordered list of syllabus fields shown in the tab.
 */
@Serializable
data class ElearningCourseSyllabus(
    val language: String,
    val exportPdfUrl: String?,
    val fields: List<ElearningCourseSyllabusField>
)

/**
 * A single syllabus field (server-localized title plus HTML body).
 *
 * @property title Localized field title (e.g. "Obiettivi", "Aims").
 * @property htmlContent Raw HTML body of the field.
 */
@Serializable
data class ElearningCourseSyllabusField(
    val title: String,
    val htmlContent: String
)

/**
 * A group of staff members sharing the same role on the public info page.
 *
 * @property role Localized role name (e.g. "Docente", "Tutor", "Esercitatore").
 * @property members Members belonging to this role, in display order.
 */
@Serializable
data class ElearningCourseStaffGroup(
    val role: String,
    val members: List<ElearningCourseStaffMember>
)

/**
 * A single staff member as listed on the public info page.
 *
 * @property name Display name of the staff member.
 * @property profileUrl URL to the staff member's Moodle profile page.
 * @property userId Numeric user id parsed from [profileUrl], if present.
 * @property initials Two-letter initials shown in the avatar bubble.
 * @property email Email address parsed from the row id, if present.
 */
@Serializable
data class ElearningCourseStaffMember(
    val name: String,
    val profileUrl: String,
    val userId: Int?,
    val initials: String?,
    val email: String?
)
