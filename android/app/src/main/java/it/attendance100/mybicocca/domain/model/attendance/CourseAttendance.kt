package it.attendance100.mybicocca.domain.model.attendance

import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

// A study-plan course the student has not yet passed, enriched with every
// attendance signal available for it. Either attendance source can be absent:
// most courses have no badge tracking and no session register.
data class CourseAttendance(
    val name: String,
    val code: String?,
    val year: StudyYear,
    val semester: Semester,
    val credits: Float,
    val teacherName: String?,
    val classroomAttendance: ClassroomAttendance?,
    val sessionAttendance: List<SessionAttendance>,
    val attendanceModules: List<AttendanceModuleRef> = emptyList(),
)
