package it.attendance100.mybicocca.domain.model.studyplan

data class PlannedCourse(
    val easyStaffSubjectId: String,
    val easyStaffSubjectCode: String,
    // University activity code (e.g. "E3101Q123") — the join key shared with the
    // elearning idNumber base code. Null when the plan row carries no code.
    val activityCode: String?,
    val name: String,
    val normalizedName: String,
    val teacherName: String,
    val periodId: String,
    val semester: Semester,
    val studyYear: StudyYear,
    val cfu: Int?,
)
