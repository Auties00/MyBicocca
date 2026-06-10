package it.attendance100.mybicocca.data.local.calendar

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Access to the calendar's per-(career, source, scope) freshness stamps. Stamps gate TTL
 * checks before a refresh; deleting them forces the next refresh to treat the source as
 * stale.
 */
@Dao
interface CalendarSyncStateDao {

    /** Stamp of one sync unit, or null when the unit has never completed a sync. */
    @Query(
        "SELECT * FROM calendar_sync_state " +
            "WHERE career_id = :careerId AND source = :source AND year_month = :yearMonth"
    )
    suspend fun getState(careerId: Long, source: String, yearMonth: String): CalendarSyncStateEntity?

    /** Hot stream of one sync unit's stamp; emits null while the unit has never synced. */
    @Query(
        "SELECT * FROM calendar_sync_state " +
            "WHERE career_id = :careerId AND source = :source AND year_month = :yearMonth"
    )
    fun observeState(careerId: Long, source: String, yearMonth: String): Flow<CalendarSyncStateEntity?>

    @Upsert
    suspend fun upsertState(state: CalendarSyncStateEntity)

    /**
     * Drops every stamp of one source across all scopes — the invalidation path: with no
     * stamp left, the next refresh of that source bypasses its TTL.
     */
    @Query(
        "DELETE FROM calendar_sync_state " +
            "WHERE career_id = :careerId AND source = :source"
    )
    suspend fun deleteStatesForSource(careerId: Long, source: String)

    /**
     * Wipes the stamps of every career belonging to an account, resolving the careers
     * through the careers table; part of full account removal.
     */
    @Query(
        "DELETE FROM calendar_sync_state WHERE career_id IN " +
            "(SELECT id FROM careers WHERE account_id = :accountId)"
    )
    suspend fun deleteForAccount(accountId: String)
}
