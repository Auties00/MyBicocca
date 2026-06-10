package it.attendance100.mybicocca.domain.model.attendance

/**
 * A Moodle mod_attendance session currently open for student self-marking, discovered through
 * the tool_mobile_get_content bridge. The registry "Presenze" sub-screen lists open sessions
 * and lets the student mark themselves present.
 *
 * @property sessionId Moodle session id (the "sessid" carried by attendance QR links).
 * @property module Attendance activity the session belongs to.
 * @property requiresPassword Whether marking requires the session password ("studentpass").
 * @property statuses Status choices the student may submit.
 */
data class OpenAttendanceSession(
    val sessionId: String,
    val module: AttendanceModuleRef,
    val requiresPassword: Boolean,
    val statuses: List<AttendanceStatusOption>,
)

/**
 * One status choice offered by an attendance session (e.g. "Presente").
 *
 * @property id Moodle status id to submit.
 * @property description Display label of the status.
 */
data class AttendanceStatusOption(
    val id: String,
    val description: String,
)
