package it.attendance100.mybicocca.data.local.elearning.course

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Cached completion status of one course activity, keyed by
 * (account_id, course_id, cm_id). Rows are upserted rather than replaced so a partial
 * completion fetch never erases known states; `completedAtMs` is epoch milliseconds.
 */
@Entity(
    tableName = "elearning_activity_completion",
    primaryKeys = ["account_id", "course_id", "cm_id"],
)
data class ActivityCompletionEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "cm_id") val cmId: Int,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "completed_at_ms") val completedAtMs: Long?,
    @ColumnInfo(name = "is_manual") val isManual: Boolean,
    @ColumnInfo(name = "is_automatic") val isAutomatic: Boolean,
    @ColumnInfo(name = "is_tracked") val isTracked: Boolean,
)
