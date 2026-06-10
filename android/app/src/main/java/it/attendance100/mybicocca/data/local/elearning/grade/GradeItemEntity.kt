package it.attendance100.mybicocca.data.local.elearning.grade

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cached gradebook row of a course, keyed by (account_id, course_id, item_id).
 * Refreshes replace all rows of the course in one transaction; `sortOrder` preserves
 * the gradebook's own row order. `gradedAtMs` is epoch milliseconds.
 */
@Entity(
    tableName = "elearning_grade_items",
    primaryKeys = ["account_id", "course_id", "item_id"],
    indices = [Index("account_id", "course_id")],
)
data class GradeItemEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "item_id") val itemId: Long,
    val name: String,
    @ColumnInfo(name = "type_raw") val typeRaw: String,
    @ColumnInfo(name = "activity_type") val activityType: String?,
    val grade: Double?,
    @ColumnInfo(name = "max_grade") val maxGrade: Double?,
    val percentage: Double?,
    @ColumnInfo(name = "grade_formatted") val gradeFormatted: String?,
    val feedback: String?,
    @ColumnInfo(name = "graded_at_ms") val gradedAtMs: Long?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)
