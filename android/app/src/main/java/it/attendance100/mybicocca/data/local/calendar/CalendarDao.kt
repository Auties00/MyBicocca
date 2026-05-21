package it.attendance100.mybicocca.data.local.calendar

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {

    @Query(
        "SELECT * FROM calendar_events " +
            "WHERE career_id = :careerId AND date BETWEEN :startIso AND :endIso " +
            "ORDER BY date, start_time"
    )
    fun observeInRange(careerId: Long, startIso: String, endIso: String): Flow<List<CalendarEventEntity>>

    @Query(
        "SELECT * FROM calendar_events " +
            "WHERE career_id = :careerId AND date = :dateIso " +
            "ORDER BY start_time"
    )
    fun observeForDay(careerId: Long, dateIso: String): Flow<List<CalendarEventEntity>>

    @Query(
        "SELECT * FROM calendar_events " +
            "WHERE career_id = :careerId AND date >= :fromIso " +
            "ORDER BY date, start_time LIMIT :limit"
    )
    fun observeFrom(careerId: Long, fromIso: String, limit: Int): Flow<List<CalendarEventEntity>>

    @Upsert
    suspend fun upsertAll(rows: List<CalendarEventEntity>)

    @Query(
        "DELETE FROM calendar_events " +
            "WHERE career_id = :careerId AND source = :source AND date BETWEEN :startIso AND :endIso"
    )
    suspend fun deleteBySourceAndRange(careerId: Long, source: String, startIso: String, endIso: String)

    @Query(
        "DELETE FROM calendar_events WHERE career_id IN " +
            "(SELECT id FROM careers WHERE account_id = :accountId)"
    )
    suspend fun deleteForAccount(accountId: String)

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
}
