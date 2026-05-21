package it.attendance100.mybicocca.domain.model.studyplan

data class PlannedCourse(
    val easyStaffSubjectId: String,
    val easyStaffSubjectCode: String,
    val name: String,
    val normalizedName: String,
    val teacherName: String,
    val periodId: String,
    val studyYear: StudyYear,
    val cfu: Int?,
)
