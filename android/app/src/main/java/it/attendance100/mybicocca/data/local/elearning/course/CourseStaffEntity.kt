package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "elearning_course_staff",
    primaryKeys = ["account_id", "course_id", "row_index"],
)
data class CourseStaffEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    // Stable ordering across role groups; `userId` is nullable on the source page.
    @ColumnInfo(name = "row_index") val rowIndex: Int,
    @ColumnInfo(name = "user_id") val userId: Int?,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "role_raw") val roleRaw: String,
    val initials: String?,
    val email: String?,
    @ColumnInfo(name = "profile_url") val profileUrl: String?,
)
