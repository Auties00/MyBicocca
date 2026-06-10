package it.attendance100.mybicocca.data.local.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Freshness stamp of one calendar sync unit, keyed by (career, source, scope). A row exists
 * only after the unit's first successful sync, so its absence doubles as the "month never
 * hydrated" signal.
 *
 * @property careerId Row id of the owning career in the careers table.
 * @property source Code of the synced feed, matching the domain source codes.
 * @property yearMonth Sync scope: an ISO year-month ("2026-06") for month-scoped sources
 *   (lessons), or the "*" marker for career-global sources (exams, appointments, library)
 *   that refresh their whole data set at once.
 * @property lastRefreshedAtMs Epoch milliseconds of the last successful sync, compared
 *   against the per-source TTL to decide staleness.
 */
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
