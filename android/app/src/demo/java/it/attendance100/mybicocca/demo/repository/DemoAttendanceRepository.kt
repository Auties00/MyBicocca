package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoCourse
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
import it.attendance100.mybicocca.domain.model.attendance.AttendanceStatusOption
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendance
import it.attendance100.mybicocca.domain.model.attendance.ClassroomAttendanceStatus
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.OpenAttendanceSession
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
import it.attendance100.mybicocca.domain.model.attendance.SessionAttendance
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attendance for the courses being attended, at different levels. The lecture in progress at the
 * demo clock has a session open; marking it, or scanning any recognised code, succeeds once.
 */
@Singleton
class DemoAttendanceRepository @Inject constructor() : AttendanceRepository {

    // Lessons attended out of those held so far, per course.
    private val attended = mapOf(
        DemoCourses.algorithms to (38 to 42),
        DemoCourses.databases to (25 to 28),
        DemoCourses.operatingSystems to (19 to 30),
        DemoCourses.statistics to (27 to 28),
        DemoCourses.languages to (11 to 29),
    )

    private val marked = mutableSetOf<String>()

    override suspend fun getPendingCourses(careerId: CareerId): List<CourseAttendance> = DemoCourses.current.map { course ->
        val (present, held) = attended.getValue(course)
        val percentage = present * 100.0 / held
        CourseAttendance(
            name = course.name,
            code = course.code,
            year = StudyYear(course.year),
            semester = Semester.Second,
            credits = course.cfu.toFloat(),
            teacherName = course.teacher,
            classroomAttendance = ClassroomAttendance(
                attendancePercentage = percentage,
                lessonsAttended = present,
                hoursCompleted = present * 2,
                requirementProgressPercentage = (percentage / REQUIRED * 100).coerceAtMost(100.0),
                status = when {
                    percentage >= REQUIRED -> ClassroomAttendanceStatus.Attending
                    percentage >= REQUIRED - 20 -> ClassroomAttendanceStatus.InProgress
                    else -> ClassroomAttendanceStatus.NotAttending
                },
            ),
            sessionAttendance = listOf(
                SessionAttendance(
                    label = "Lezioni",
                    attendedSessions = present,
                    recordedPercentage = percentage,
                    totalSessions = held + 6,
                    overallPercentage = present * 100.0 / (held + 6),
                    bestPossiblePercentage = (present + 6) * 100.0 / (held + 6),
                ),
            ),
            attendanceModules = listOf(moduleOf(course)),
        )
    }

    override suspend fun getOpenSessions(modules: List<AttendanceModuleRef>): List<OpenAttendanceSession> {
        val open = moduleOf(DemoCourses.databases)
        if (modules.none { it.courseModuleId == open.courseModuleId } || OPEN_SESSION in marked) return emptyList()
        return listOf(
            OpenAttendanceSession(
                sessionId = OPEN_SESSION,
                module = open,
                requiresPassword = false,
                statuses = listOf(AttendanceStatusOption("P", "Presente")),
            ),
        )
    }

    override suspend fun markSession(
        module: AttendanceModuleRef,
        sessionId: String,
        statusId: String?,
        password: String?,
    ): PresenceMarkOutcome = record(sessionId)

    override suspend fun registerPresence(scan: PresenceScan, careerId: CareerId): PresenceMarkOutcome = when (scan) {
        is PresenceScan.SessionLink -> record(scan.sessionId)
        is PresenceScan.LessonCode -> record(scan.code)
        is PresenceScan.Unrecognized -> PresenceMarkOutcome.Failed()
    }

    private fun record(id: String): PresenceMarkOutcome =
        if (marked.add(id)) PresenceMarkOutcome.Recorded(statusDescription = "Presente") else PresenceMarkOutcome.AlreadyRecorded()

    private fun moduleOf(course: DemoCourse) = AttendanceModuleRef(
        courseId = course.moodleCourseId,
        courseModuleId = DemoElearning.cmId(course, ATTENDANCE_MODULE),
        name = "Registro presenze",
    )

    private companion object {
        const val REQUIRED = 75.0
        const val ATTENDANCE_MODULE = 3
        const val OPEN_SESSION = "demo-session-1"
    }
}
