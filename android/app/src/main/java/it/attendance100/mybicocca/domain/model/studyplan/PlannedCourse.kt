package it.attendance100.mybicocca.domain.model.studyplan

/**
 * A course of the student's study plan matched to its EasyStaff timetable subject: the
 * intersection of the Esse3 plan activities with the EasyStaff study-program catalog.
 * Feeds the calendar's lesson sources and attendance lookups, which need EasyStaff ids
 * for activities the student actually has in plan.
 *
 * @property easyStaffSubjectId Opaque EasyStaff subject id used by timetable queries.
 * @property easyStaffSubjectCode EasyStaff internal subject code (EC-code).
 * @property activityCode University activity code (e.g. "E3101Q123") — the join key
 *   shared with the elearning `idNumber` base code. Null when the plan row carries no
 *   code.
 * @property name Subject name, preferring the EasyStaff spelling.
 * @property normalizedName Diacritics/spacing-normalized name used as a fallback join
 *   key.
 * @property teacherName Lecturer name as published by EasyStaff.
 * @property periodId Opaque EasyStaff teaching-period id the subject is taught in.
 * @property semester Semester decoded from the EasyStaff period.
 * @property studyYear Year of the course the activity belongs to.
 * @property cfu CFU weight from the Esse3 plan row; null when the plan omits it.
 */
data class PlannedCourse(
    val easyStaffSubjectId: String,
    val easyStaffSubjectCode: String,
    val activityCode: String?,
    val name: String,
    val normalizedName: String,
    val teacherName: String,
    val periodId: String,
    val semester: Semester,
    val studyYear: StudyYear,
    val cfu: Int?,
)
