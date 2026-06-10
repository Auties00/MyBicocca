package it.attendance100.mybicocca.data.local.elearning.deadline

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room access to the per-account deadline cache, soonest first. Refreshes go through
 * `replaceForAccount`, which swaps the whole account's rows in one transaction so
 * observers never see a half-written list.
 */
@Dao
interface DeadlineDao {

    @Query(
        "SELECT * FROM elearning_deadlines " +
            "WHERE account_id = :accountId " +
            "ORDER BY due_at_ms"
    )
    fun observeForAccount(accountId: String): Flow<List<DeadlineEntity>>

    @Upsert
    suspend fun upsert(rows: List<DeadlineEntity>)

    @Query("DELETE FROM elearning_deadlines WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: String)

    @Transaction
    suspend fun replaceForAccount(accountId: String, rows: List<DeadlineEntity>) {
        deleteForAccount(accountId)
        if (rows.isNotEmpty()) upsert(rows)
    }
}
