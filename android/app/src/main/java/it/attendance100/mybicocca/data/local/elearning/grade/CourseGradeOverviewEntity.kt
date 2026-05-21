package it.attendance100.mybicocca.data.local.elearning.grade

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "elearning_course_grade_overview",
    primaryKeys = ["account_id", "course_id"],
)
data class CourseGradeOverviewEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "course_name") val courseName: String,
    val grade: Double?,
    @ColumnInfo(name = "max_grade") val maxGrade: Double?,
    @ColumnInfo(name = "grade_formatted") val gradeFormatted: String?,
)
