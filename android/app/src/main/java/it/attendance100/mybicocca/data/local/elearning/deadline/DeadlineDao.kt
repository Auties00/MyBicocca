package it.attendance100.mybicocca.data.local.elearning.deadline

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

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
