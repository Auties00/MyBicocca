package it.attendance100.mybicocca.data.local.calendar

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CalendarSyncStateDao {

    @Query(
        "SELECT * FROM calendar_sync_state " +
            "WHERE career_id = :careerId AND source = :source AND year_month = :yearMonth"
    )
    suspend fun getState(careerId: Long, source: String, yearMonth: String): CalendarSyncStateEntity?

    @Upsert
    suspend fun upsertState(state: CalendarSyncStateEntity)

    @Query(
        "DELETE FROM calendar_sync_state WHERE career_id IN " +
            "(SELECT id FROM careers WHERE account_id = :accountId)"
    )
    suspend fun deleteForAccount(accountId: String)
}
