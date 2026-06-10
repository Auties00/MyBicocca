package it.attendance100.mybicocca.domain.model.attendance

/**
 * In-classroom badge attendance for a course, sourced from the EasyStaff EasyBadge attendance
 * history: how many lessons were certified and how close the student is to the course's
 * minimum attendance requirement. Shown per course on the registry "Presenze" sub-screen.
 *
 * @property attendancePercentage Share of held lessons the student attended.
 * @property lessonsAttended Number of lessons certified via badge.
 * @property hoursCompleted Hours of certified attendance accumulated.
 * @property requirementProgressPercentage Progress towards the course's minimum attendance
 *   requirement.
 * @property status Standing against the requirement.
 */
data class ClassroomAttendance(
    val attendancePercentage: Double,
    val lessonsAttended: Int,
    val hoursCompleted: Int,
    val requirementProgressPercentage: Double,
    val status: ClassroomAttendanceStatus,
)

/** Standing of the student against a course's minimum attendance requirement. */
enum class ClassroomAttendanceStatus {
    Attending,
    InProgress,
    NotAttending,
}
