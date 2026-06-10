package it.attendance100.mybicocca.data.local.calendar

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Access to the materialized calendar rows. All reads are career-scoped, take ISO-8601 date
 * strings (compared lexicographically) and order results chronologically. Writes happen per
 * source, via the splice-replace transactions, so syncing one feed never disturbs the rows
 * of another.
 */
@Dao
interface CalendarDao {

    /** Hot stream of a career's rows with date in the inclusive [startIso], [endIso] range. */
    @Query(
        "SELECT * FROM calendar_events " +
            "WHERE career_id = :careerId AND date BETWEEN :startIso AND :endIso " +
            "ORDER BY date, start_time"
    )
    fun observeInRange(careerId: Long, startIso: String, endIso: String): Flow<List<CalendarEventEntity>>

    @Upsert
    suspend fun upsertAll(rows: List<CalendarEventEntity>)

    /** Clears one source's rows inside a date range, leaving other sources untouched. */
    @Query(
        "DELETE FROM calendar_events " +
            "WHERE career_id = :careerId AND source = :source AND date BETWEEN :startIso AND :endIso"
    )
    suspend fun deleteBySourceAndRange(careerId: Long, source: String, startIso: String, endIso: String)

    /** Clears every row of one source across the career's whole calendar. */
    @Query("DELETE FROM calendar_events WHERE career_id = :careerId AND source = :source")
    suspend fun deleteBySource(careerId: Long, source: String)

    /**
     * Wipes the rows of every career belonging to an account, resolving the careers through
     * the careers table; part of full account removal.
     */
    @Query(
        "DELETE FROM calendar_events WHERE career_id IN " +
            "(SELECT id FROM careers WHERE account_id = :accountId)"
    )
    suspend fun deleteForAccount(accountId: String)

    /**
     * Atomically swaps one source's rows within a date range: the slice is deleted and the
     * fresh rows inserted in a single transaction. The unit of sync for month-scoped sources
     * such as lessons.
     */
    @Transaction
    suspend fun replaceSource(
        careerId: Long,
        source: String,
        startIso: String,
        endIso: String,
        rows: List<CalendarEventEntity>,
    ) {
        deleteBySourceAndRange(careerId, source, startIso, endIso)
        if (rows.isNotEmpty()) upsertAll(rows)
    }

    /**
     * Whole-career replacement for sources whose remote returns the complete data set in one
     * call (Esse3 bookings), where month-by-month splicing would leave stale rows.
     */
    @Transaction
    suspend fun replaceSourceAll(
        careerId: Long,
        source: String,
        rows: List<CalendarEventEntity>,
    ) {
        deleteBySource(careerId, source)
        if (rows.isNotEmpty()) upsertAll(rows)
    }
}
