package it.attendance100.mybicocca.data.mapper.attendance

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.text.UiText
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceRecord
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffAttendanceStatus
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffCertifyAttendanceResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkResult
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceMarkableSession
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceStatusOption
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAttendanceSummary
import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendanceStatus
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import org.junit.Test

/**
 * Covers the attendance mappers: EasyBadge status enum -> classroom standing, Moodle summary and
 * markable-session mapping, and the self-mark / certify result translation into the user-facing
 * PresenceMarkOutcome (including the subnet/shared NotOpen copy and the "codice" failure
 * classification).
 */
class AttendanceMapperTest {

    @Test
    fun `easybadge record maps the three attendance statuses`() {
        assertThat(record(EasyStaffAttendanceStatus.ATTENDING).toDomain().status)
            .isEqualTo(ClassroomAttendanceStatus.Attending)
        assertThat(record(EasyStaffAttendanceStatus.IN_PROGRESS).toDomain().status)
            .isEqualTo(ClassroomAttendanceStatus.InProgress)
        assertThat(record(EasyStaffAttendanceStatus.NOT_ATTENDING).toDomain().status)
            .isEqualTo(ClassroomAttendanceStatus.NotAttending)
    }

    @Test
    fun `easybadge record carries the percentages and counts`() {
        val attendance = record(EasyStaffAttendanceStatus.ATTENDING).toDomain()
        assertThat(attendance.attendancePercentage).isEqualTo(75.0)
        assertThat(attendance.lessonsAttended).isEqualTo(9)
        assertThat(attendance.hoursCompleted).isEqualTo(18)
        assertThat(attendance.requirementProgressPercentage).isEqualTo(60.0)
    }

    @Test
    fun `moodle summary maps to a labeled session register`() {
        val summary = ElearningAttendanceSummary(
            attendedSessions = 5,
            takenSessionsPercentage = 83.3,
            totalSessions = 12,
            allSessionsPercentage = 41.6,
            pointsTakenLabel = "5/6",
            maxPossiblePointsLabel = "12/12",
            maxPossiblePercentage = 100.0,
        ).toDomain("Laboratorio")
        assertThat(summary.label).isEqualTo("Laboratorio")
        assertThat(summary.attendedSessions).isEqualTo(5)
        assertThat(summary.recordedPercentage).isEqualTo(83.3)
        assertThat(summary.totalSessions).isEqualTo(12)
        assertThat(summary.overallPercentage).isEqualTo(41.6)
        assertThat(summary.pointsLabel).isEqualTo("5/6")
        assertThat(summary.bestPossiblePercentage).isEqualTo(100.0)
    }

    @Test
    fun `markable session binds to its module and maps statuses`() {
        val module = AttendanceModuleRef(courseId = 10, courseModuleId = 20, name = "Presenze")
        val session = ElearningAttendanceMarkableSession(
            sessionId = "55",
            requiresPassword = true,
            statuses = listOf(
                ElearningAttendanceStatusOption("1", "Presente"),
                ElearningAttendanceStatusOption("2", "Assente"),
            ),
        ).toDomain(module)
        assertThat(session.sessionId).isEqualTo("55")
        assertThat(session.module).isEqualTo(module)
        assertThat(session.requiresPassword).isTrue()
        assertThat(session.statuses.map { it.id }).containsExactly("1", "2").inOrder()
        assertThat(session.statuses.first().description).isEqualTo("Presente")
    }

    @Test
    fun `mark result Marked becomes Recorded with the status description`() {
        val outcome = ElearningAttendanceMarkResult.Marked("Presente").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Recorded::class.java)
        outcome as PresenceMarkOutcome.Recorded
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_recorded))
        assertThat(outcome.statusDescription).isEqualTo("Presente")
    }

    @Test
    fun `mark result AlreadyMarked becomes AlreadyRecorded`() {
        val outcome = ElearningAttendanceMarkResult.AlreadyMarked.toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.AlreadyRecorded::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_already_recorded))
    }

    @Test
    fun `mark result WrongPassword becomes WrongCredential`() {
        val outcome = ElearningAttendanceMarkResult.WrongPassword.toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.WrongCredential::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_wrong_credential))
    }

    @Test
    fun `mark result NotOpen subnetwrong explains the classroom network requirement`() {
        val outcome = ElearningAttendanceMarkResult.NotOpen("subnetwrong").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.NetworkRestricted::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_network_wrong))
    }

    @Test
    fun `mark result NotOpen preventsharederror explains the network already recorded`() {
        val outcome = ElearningAttendanceMarkResult.NotOpen("preventsharederror").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.DeviceAlreadyUsed::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_network_prevent_shared))
    }

    @Test
    fun `mark result NotOpen with an unknown reason uses the generic copy`() {
        val outcome = ElearningAttendanceMarkResult.NotOpen("closed").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.NotOpen::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_session_closed))
    }

    @Test
    fun `mark result NotOpen with a null reason uses the generic copy`() {
        val outcome = ElearningAttendanceMarkResult.NotOpen(null).toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.NotOpen::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_session_closed))
    }

    @Test
    fun `mark result Failed becomes Failed`() {
        val outcome = ElearningAttendanceMarkResult.Failed("template").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Failed::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_failed))
    }

    @Test
    fun `certify success returns Recorded with the backend message`() {
        val outcome = EasyStaffCertifyAttendanceResult(success = true, message = "Presenza confermata").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Recorded::class.java)
        assertThat(outcome.message).isEqualTo(UiText.DynamicString("Presenza confermata"))
    }

    @Test
    fun `certify success with a blank message falls back to default copy`() {
        val outcome = EasyStaffCertifyAttendanceResult(success = true, message = "").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Recorded::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_recorded))
    }

    @Test
    fun `certify failure mentioning codice classifies as wrong credential`() {
        val outcome = EasyStaffCertifyAttendanceResult(success = false, message = "Codice lezione errato").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.WrongCredential::class.java)
        assertThat(outcome.message).isEqualTo(UiText.DynamicString("Codice lezione errato"))
    }

    @Test
    fun `certify other failure classifies as failed with fallback copy`() {
        val outcome = EasyStaffCertifyAttendanceResult(success = false, message = "").toOutcome()
        assertThat(outcome).isInstanceOf(PresenceMarkOutcome.Failed::class.java)
        assertThat(outcome.message).isEqualTo(UiText.StringResource(R.string.attendance_msg_failed))
    }

    private fun record(state: EasyStaffAttendanceStatus) = EasyStaffAttendanceRecord(
        courseCode = "E1234",
        courseName = "Analisi",
        attendancePercentage = 75.0,
        lessonsAttended = 9,
        totalHours = 18,
        requirementProgressPercentage = 60.0,
        state = state,
    )
}
