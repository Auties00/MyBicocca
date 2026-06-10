package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import javax.inject.Inject

/**
 * Loads the not-yet-passed courses with their attendance signals when the registry "Presenze"
 * sub-screen opens or is refreshed. Joins the Esse3 study plan with EasyStaff badge attendance
 * and Moodle mod_attendance registers — always fetched live, never cached.
 */
class GetPendingAttendanceCoursesUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<CourseAttendance> =
        repository.getPendingCourses(careerId)
}
