package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.attendance.AttendanceModuleRef
import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.attendance.OpenAttendanceSession
import it.attendance100.mybicocca.domain.model.attendance.PresenceMarkOutcome
import it.attendance100.mybicocca.domain.model.attendance.PresenceScan
import it.attendance100.mybicocca.domain.model.career.CareerId

interface AttendanceRepository {

    // Study-plan courses not yet passed, up to the student's current year of
    // study and semester, each enriched with the available attendance data.
    // Live fetch, no local cache: attendance certifications change during the
    // day and staleness would be misleading (same policy as exam bookings).
    suspend fun getPendingCourses(careerId: CareerId): List<CourseAttendance>

    suspend fun getOpenSessions(modules: List<AttendanceModuleRef>): List<OpenAttendanceSession>

    suspend fun markSession(
        module: AttendanceModuleRef,
        sessionId: String,
        statusId: String?,
        password: String?,
    ): PresenceMarkOutcome

    suspend fun registerPresence(scan: PresenceScan, careerId: CareerId): PresenceMarkOutcome
}
