package it.attendance100.mybicocca.data.local.elearning.video

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "elearning_video_progress",
    primaryKeys = ["account_id", "cm_id"],
    indices = [Index("account_id", "course_id")],
)
data class VideoProgressEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "cm_id") val cmId: Int,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "position_ms") val positionMs: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    val completed: Boolean,
    @ColumnInfo(name = "last_updated_at_ms") val lastUpdatedAtMs: Long,
)
