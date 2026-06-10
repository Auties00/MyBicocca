package it.attendance100.mybicocca.data.local.elearning.grade

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Cached course-total grade of one enrolled course, keyed by (account_id, course_id).
 * Refreshes replace all rows of the account in one transaction. `courseName` may be
 * stored empty because the overview web service doesn't carry names; the UI resolves
 * it from the enrolled-course cache.
 */
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
