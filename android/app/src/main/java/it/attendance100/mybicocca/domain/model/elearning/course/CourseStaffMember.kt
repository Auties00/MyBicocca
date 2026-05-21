package it.attendance100.mybicocca.domain.model.elearning.course

data class CourseStaffMember(
    val userId: Int?,
    val fullName: String,
    val role: CourseStaffRole,
    val initials: String?,
    val email: String?,
    val profileUrl: String?,
)
