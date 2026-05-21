package it.attendance100.mybicocca.domain.model.elearning.grade

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId

data class CourseGradeOverview(
    val courseId: CourseId,
    val courseName: String,
    val grade: Double?,
    val maxGrade: Double?,
    val gradeFormatted: String?,
)
