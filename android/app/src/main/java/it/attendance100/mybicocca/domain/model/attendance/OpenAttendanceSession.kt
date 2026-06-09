package it.attendance100.mybicocca.domain.model.attendance

data class OpenAttendanceSession(
    val sessionId: String,
    val module: AttendanceModuleRef,
    val requiresPassword: Boolean,
    val statuses: List<AttendanceStatusOption>,
)

data class AttendanceStatusOption(
    val id: String,
    val description: String,
)
