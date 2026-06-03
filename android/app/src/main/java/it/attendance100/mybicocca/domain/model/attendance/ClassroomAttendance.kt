package it.attendance100.mybicocca.domain.model.attendance

// In-classroom badge attendance for a course: how many lessons were certified
// and how close the student is to the course's minimum attendance requirement.
data class ClassroomAttendance(
    val attendancePercentage: Double,
    val lessonsAttended: Int,
    val hoursCompleted: Int,
    val requirementProgressPercentage: Double,
    val status: ClassroomAttendanceStatus,
)

enum class ClassroomAttendanceStatus {
    Attending,
    InProgress,
    NotAttending,
}
