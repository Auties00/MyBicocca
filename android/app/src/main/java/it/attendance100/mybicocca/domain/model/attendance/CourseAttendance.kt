package it.attendance100.mybicocca.domain.model.attendance

import it.attendance100.mybicocca.domain.model.studyplan.Semester
import it.attendance100.mybicocca.domain.model.studyplan.StudyYear

/**
 * A study-plan course the student has not yet passed, enriched with every attendance signal
 * available for it. Either attendance source can be absent: most courses have no badge
 * tracking and no session register. One card per course on the registry "Presenze" sub-screen.
 *
 * @property name Course name from the study plan.
 * @property code Esse3 activity code, when known.
 * @property year Year of study the course is scheduled in.
 * @property semester Semester the course is taught in.
 * @property credits Course credits (CFU).
 * @property teacherName Lead teacher, when EasyStaff provides one.
 * @property classroomAttendance EasyStaff EasyBadge attendance, when the course is tracked.
 * @property sessionAttendance Per-register Moodle mod_attendance summaries, when any exist.
 * @property attendanceModules Moodle attendance activities of the course's current edition,
 *   used to look up sessions open for self-marking.
 */
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
