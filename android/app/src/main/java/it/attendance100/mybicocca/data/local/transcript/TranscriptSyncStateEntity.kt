package it.attendance100.mybicocca.data.local.transcript

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Last-refresh bookkeeping for the transcript cache, one row per career keyed by
 * `career_id`. Drives the staleness check that lets non-forced refreshes no-op while
 * the cache is within its TTL.
 *
 * @property lastRefreshedAtMs Epoch milliseconds of the last successful sync.
 */
@Entity(tableName = "transcript_sync_state")
data class TranscriptSyncStateEntity(
    @PrimaryKey @ColumnInfo(name = "career_id") val careerId: Long,
    @ColumnInfo(name = "last_refreshed_at_ms") val lastRefreshedAtMs: Long,
)
