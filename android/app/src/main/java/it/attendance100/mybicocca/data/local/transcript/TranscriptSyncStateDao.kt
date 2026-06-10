package it.attendance100.mybicocca.data.local.transcript

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * Room access to the per-career transcript sync bookkeeping. The account-wide delete
 * resolves the account's careers through the `careers` table, mirroring the transcript
 * tables' sign-out purge.
 */
@Dao
interface TranscriptSyncStateDao {

    @Query("SELECT * FROM transcript_sync_state WHERE career_id = :careerId")
    suspend fun getState(careerId: Long): TranscriptSyncStateEntity?

    @Upsert
    suspend fun upsertState(state: TranscriptSyncStateEntity)

    @Query(
        "DELETE FROM transcript_sync_state WHERE career_id IN " +
            "(SELECT id FROM careers WHERE account_id = :accountId)"
    )
    suspend fun deleteForAccount(accountId: String)
}
