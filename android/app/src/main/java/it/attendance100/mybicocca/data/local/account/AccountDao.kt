package it.attendance100.mybicocca.data.local.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Transaction
    @Query("SELECT * FROM accounts ORDER BY last_used_at DESC")
    fun observeAll(): Flow<List<AccountWithCareers>>

    @Transaction
    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AccountWithCareers?>

    @Transaction
    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AccountWithCareers?

    @Transaction
    @Query("SELECT * FROM accounts WHERE record_user_id = :recordUserId LIMIT 1")
    suspend fun findByRecordUserId(recordUserId: String): AccountWithCareers?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCareers(careers: List<CareerEntity>)

    @Query("DELETE FROM careers WHERE account_id = :accountId")
    suspend fun deleteCareersFor(accountId: String)

    @Transaction
    suspend fun replaceCareers(accountId: String, careers: List<CareerEntity>) {
        deleteCareersFor(accountId)
        upsertCareers(careers)
    }

    @Query(
        "UPDATE accounts SET selected_career_id = :careerId, last_used_at = :timestamp " +
            "WHERE id = :accountId"
    )
    suspend fun updateSelectedCareer(accountId: String, careerId: Long, timestamp: Long)

    @Query("UPDATE accounts SET last_used_at = :timestamp WHERE id = :accountId")
    suspend fun updateLastUsed(accountId: String, timestamp: Long)

    @Query("UPDATE accounts SET last_synced_at = :timestamp WHERE id = :accountId")
    suspend fun updateLastSynced(accountId: String, timestamp: Long)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun delete(accountId: String)
}
