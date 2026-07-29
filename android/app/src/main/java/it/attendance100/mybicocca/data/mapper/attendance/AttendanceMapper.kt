package it.attendance100.mybicocca.data.mapper.attendance

import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceRecord
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffCertifyAttendanceResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSummary
import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
import it.attendance100.mybicocca.domain.model.attendance.AttendanceStatusOption
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendanceStatus
import it.attendance100.mybicocca.domain.model.attendance.OpenAttendanceSession
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.SessionAttendance

/** Maps an EasyBadge attendance-history record to the per-course classroom attendance. */
fun EasyStaffAttendanceRecord.toDomain(): ClassroomAttendance = ClassroomAttendance(
    attendancePercentage = attendancePercentage,
    lessonsAttended = lessonsAttended,
    hoursCompleted = totalHours,
    requirementProgressPercentage = requirementProgressPercentage,
    status = when (state) {
        EasyStaffAttendanceStatus.ATTENDING -> ClassroomAttendanceStatus.Attending
        EasyStaffAttendanceStatus.IN_PROGRESS -> ClassroomAttendanceStatus.InProgress
        EasyStaffAttendanceStatus.NOT_ATTENDING -> ClassroomAttendanceStatus.NotAttending
    },
)

/**
 * Maps a Moodle mod_attendance summary to a session register, labeled with the attendance
 * activity name it was read from.
 */
fun ElearningAttendanceSummary.toDomain(label: String): SessionAttendance = SessionAttendance(
    label = label,
    attendedSessions = attendedSessions,
    recordedPercentage = takenSessionsPercentage,
    totalSessions = totalSessions,
    overallPercentage = allSessionsPercentage,
    pointsLabel = pointsTakenLabel,
    bestPossiblePercentage = maxPossiblePercentage,
)

/** Maps a markable Moodle session, binding it to the attendance activity it belongs to. */
fun ElearningAttendanceMarkableSession.toDomain(module: AttendanceModuleRef): OpenAttendanceSession =
    OpenAttendanceSession(
        sessionId = sessionId,
        module = module,
        requiresPassword = requiresPassword,
        statuses = statuses.map { AttendanceStatusOption(it.id, it.description) },
    )

/**
 * Translates the Moodle self-marking result into a user-facing outcome with Italian feedback.
 * NotOpen reasons map to specific copy: "subnetwrong" means the student is outside the
 * classroom network, "preventsharederror" that the network already recorded a presence.
 */
fun ElearningAttendanceMarkResult.toOutcome(): PresenceMarkOutcome = when (this) {
    is ElearningAttendanceMarkResult.Marked ->
        PresenceMarkOutcome.Recorded(
            message = UiText.StringResource(R.string.attendance_msg_recorded),
            statusDescription = statusDescription,
        )

    ElearningAttendanceMarkResult.AlreadyMarked ->
        PresenceMarkOutcome.AlreadyRecorded(
            message = UiText.StringResource(R.string.attendance_msg_already_recorded),
        )

    ElearningAttendanceMarkResult.WrongPassword ->
        PresenceMarkOutcome.WrongCredential(
            message = UiText.StringResource(R.string.attendance_msg_wrong_credential),
        )

    is ElearningAttendanceMarkResult.NotOpen -> when (reason) {
        "subnetwrong" -> PresenceMarkOutcome.NetworkRestricted(
            message = UiText.StringResource(R.string.attendance_msg_network_wrong),
        )

        "preventsharederror" -> PresenceMarkOutcome.DeviceAlreadyUsed(
            message = UiText.StringResource(R.string.attendance_msg_network_prevent_shared),
        )

        else -> PresenceMarkOutcome.NotOpen(
            message = UiText.StringResource(R.string.attendance_msg_session_closed),
        )
    }

    is ElearningAttendanceMarkResult.Failed ->
        PresenceMarkOutcome.Failed(
            message = UiText.StringResource(R.string.attendance_msg_failed),
        )
}

/**
 * Translates the EasyBadge certification result into a user-facing outcome. The backend
 * returns only a success flag plus a message, so a failure mentioning "codice" is classified
 * as a wrong lesson code.
 */
fun EasyStaffCertifyAttendanceResult.toOutcome(): PresenceMarkOutcome = when {
    success -> PresenceMarkOutcome.Recorded(
        message = if (message.isBlank()) UiText.StringResource(R.string.attendance_msg_recorded) else UiText.DynamicString(
            message
        ),
        statusDescription = if (message.isBlank()) null else message,
    )

    message.contains("codice", ignoreCase = true) -> PresenceMarkOutcome.WrongCredential(
        message = if (message.isBlank()) UiText.StringResource(R.string.attendance_msg_wrong_credential) else UiText.DynamicString(
            message
        ),
    )

    else -> PresenceMarkOutcome.Failed(
        message = UiText.StringResource(R.string.attendance_msg_failed),
        backendMessage = if (message.isBlank()) null else message,
    )
}
