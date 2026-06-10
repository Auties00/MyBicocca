package it.attendance100.mybicocca.domain.model.attendance

/**
 * Attendance over a course's tracked session register (e.g. lab sessions), summarized from a
 * Moodle mod_attendance activity. "Recorded" counts only the sessions whose attendance has
 * been taken so far, so its percentage is the meaningful progress signal during the semester.
 *
 * @property label Display name of the register (the attendance activity name).
 * @property attendedSessions Sessions the student was marked present at.
 * @property recordedPercentage Attendance percentage over the sessions taken so far.
 * @property totalSessions Total sessions planned in the register.
 * @property overallPercentage Attendance percentage over all planned sessions.
 * @property pointsLabel Points summary as Moodle renders it, when available.
 * @property bestPossiblePercentage Highest percentage still reachable given the sessions
 *   already taken.
 */
data class SessionAttendance(
    val label: String,
    val attendedSessions: Int?,
    val recordedPercentage: Double?,
    val totalSessions: Int?,
    val overallPercentage: Double?,
    val pointsLabel: String? = null,
    val bestPossiblePercentage: Double? = null,
)
