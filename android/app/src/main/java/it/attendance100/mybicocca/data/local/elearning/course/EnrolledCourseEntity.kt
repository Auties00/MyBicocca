package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "elearning_enrolled_courses",
    primaryKeys = ["account_id", "course_id"],
    indices = [
        Index("account_id", "is_favourite", "hidden"),
    ],
)
data class EnrolledCourseEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "short_name") val shortName: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "id_number") val idNumber: String?,
    val summary: String?,
    @ColumnInfo(name = "course_image_url") val courseImageUrl: String?,
    val format: String?,
    val language: String?,
    @ColumnInfo(name = "category_id") val categoryId: Int?,
    val progress: Double?,
    val completed: Boolean,
    @ColumnInfo(name = "completion_enabled") val completionEnabled: Boolean,
    @ColumnInfo(name = "start_date_ms") val startDateMs: Long?,
    @ColumnInfo(name = "end_date_ms") val endDateMs: Long?,
    @ColumnInfo(name = "last_access_ms") val lastAccessMs: Long?,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean,
    val hidden: Boolean,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)
