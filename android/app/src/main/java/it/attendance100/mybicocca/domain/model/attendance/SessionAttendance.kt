package it.attendance100.mybicocca.domain.model.attendance

// Attendance over a course's tracked session register (e.g. lab sessions).
// "Recorded" counts only the sessions whose attendance has been taken so far,
// so its percentage is the meaningful progress signal during the semester.
data class SessionAttendance(
    val label: String,
    val attendedSessions: Int?,
    val recordedPercentage: Double?,
    val totalSessions: Int?,
    val overallPercentage: Double?,
    val pointsLabel: String? = null,
    val bestPossiblePercentage: Double? = null,
)
