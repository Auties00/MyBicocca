package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.career.CareerId

interface AttendanceRepository {

    // Study-plan courses not yet passed, up to the student's current year of
    // study and semester, each enriched with the available attendance data.
    // Live fetch, no local cache: attendance certifications change during the
    // day and staleness would be misleading (same policy as exam bookings).
    suspend fun getPendingCourses(careerId: CareerId): List<CourseAttendance>
}
