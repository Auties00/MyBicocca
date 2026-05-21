package it.attendance100.mybicocca.data.local.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "calendar_sync_state",
    primaryKeys = ["career_id", "source", "year_month"],
)
data class CalendarSyncStateEntity(
    @ColumnInfo(name = "career_id") val careerId: Long,
    val source: String,
    @ColumnInfo(name = "year_month") val yearMonth: String,
    @ColumnInfo(name = "last_refreshed_at_ms") val lastRefreshedAtMs: Long,
)
