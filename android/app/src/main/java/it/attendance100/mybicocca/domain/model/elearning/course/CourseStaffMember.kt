package it.attendance100.mybicocca.domain.model.elearning.course

/**
 * One person listed in the staff box of a Moodle course's public page (teacher, tutor,
 * or assistant). Shown in the course detail screen's staff section.
 *
 * @property userId The Moodle user id, or null when the page lists the person without
 * a linked profile.
 * @property fullName Display name as printed on the course page.
 * @property role The staff role group the person was listed under.
 * @property initials Avatar initials provided by the page, or null to derive them
 * from the name.
 * @property email Contact email when published, else null.
 * @property profileUrl Link to the person's Moodle profile page, when available.
 */
data class CourseStaffMember(
    val userId: Int?,
    val fullName: String,
    val role: CourseStaffRole,
    val initials: String?,
    val email: String?,
    val profileUrl: String?,
)
