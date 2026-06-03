package it.attendance100.mybicocca.domain.usecase.attendance

import it.attendance100.mybicocca.domain.model.attendance.CourseAttendance
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetPendingAttendanceCoursesUseCase @Inject constructor(
    private val repository: AttendanceRepository,
) {
    suspend operator fun invoke(careerId: CareerId): List<CourseAttendance> =
        repository.getPendingCourses(careerId)
}
