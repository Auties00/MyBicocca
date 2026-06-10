package it.attendance100.mybicocca.data.local.elearning.sync

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * Room access to the refresh timestamps backing the e-learning staleness checks.
 * Repositories read a scope's state before fetching and upsert a fresh stamp after a
 * successful write; deleting an account's rows forces every resource stale again.
 */
@Dao
interface ElearningSyncStateDao {

    @Query(
        "SELECT * FROM elearning_sync_state " +
            "WHERE account_id = :accountId AND scope = :scope AND scope_id = :scopeId"
    )
    suspend fun getState(accountId: String, scope: String, scopeId: Long): ElearningSyncStateEntity?

    @Upsert
    suspend fun upsertState(state: ElearningSyncStateEntity)

    @Query("DELETE FROM elearning_sync_state WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: String)
}
