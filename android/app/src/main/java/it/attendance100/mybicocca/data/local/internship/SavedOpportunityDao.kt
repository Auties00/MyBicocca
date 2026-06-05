package it.attendance100.mybicocca.data.local.internship

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedOpportunityDao {

    @Query("SELECT * FROM saved_opportunity WHERE account_id = :accountId ORDER BY saved_at DESC")
    fun observe(accountId: String): Flow<List<SavedOpportunityEntity>>

    @Query(
        "SELECT EXISTS(SELECT 1 FROM saved_opportunity " +
            "WHERE account_id = :accountId AND opportunity_id = :opportunityId)"
    )
    suspend fun isSaved(accountId: String, opportunityId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedOpportunityEntity)

    @Query("DELETE FROM saved_opportunity WHERE account_id = :accountId AND opportunity_id = :opportunityId")
    suspend fun delete(accountId: String, opportunityId: String)

    @Query("DELETE FROM saved_opportunity WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: String)
}
