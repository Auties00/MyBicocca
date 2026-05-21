package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcript_sync_state")
data class TranscriptSyncStateEntity(
    @PrimaryKey @ColumnInfo(name = "career_id") val careerId: Long,
    @ColumnInfo(name = "last_refreshed_at_ms") val lastRefreshedAtMs: Long,
)
