package it.attendance100.mybicocca.data.mapper.attendance

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
        PresenceMarkOutcome.Recorded("Presenza registrata", statusDescription)

    ElearningAttendanceMarkResult.AlreadyMarked ->
        PresenceMarkOutcome.AlreadyRecorded("Presenza già registrata")

    ElearningAttendanceMarkResult.WrongPassword ->
        PresenceMarkOutcome.WrongCredential("Password o codice non corretti")

    is ElearningAttendanceMarkResult.NotOpen -> PresenceMarkOutcome.NotOpen(
        when (reason) {
            "subnetwrong" -> "Devi essere connesso alla rete dell'aula per registrare la presenza"
            "preventsharederror" -> "Una presenza è già stata registrata da questa rete"
            else -> "La sessione non è più disponibile"
        }
    )

    is ElearningAttendanceMarkResult.Failed ->
        PresenceMarkOutcome.Failed("Registrazione non riuscita")
}

/**
 * Translates the EasyBadge certification result into a user-facing outcome. The backend
 * returns only a success flag plus a message, so a failure mentioning "codice" is classified
 * as a wrong lesson code.
 */
fun EasyStaffCertifyAttendanceResult.toOutcome(): PresenceMarkOutcome = when {
    success -> PresenceMarkOutcome.Recorded(message.ifBlank { "Presenza registrata" })
    message.contains("codice", ignoreCase = true) -> PresenceMarkOutcome.WrongCredential(message)
    else -> PresenceMarkOutcome.Failed(message.ifBlank { "Registrazione non riuscita" })
}
