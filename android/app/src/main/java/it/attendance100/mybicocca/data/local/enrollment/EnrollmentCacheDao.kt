package it.attendance100.mybicocca.data.local.enrollment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Read/replace access to the offline enrollment-history mirror. A career's timeline is replaced
 * wholesale on a successful fetch (delete-then-insert in one transaction) and read back ordered
 * by the preserved server position; reads are consulted only when a live call fails offline.
 */
@Dao
interface EnrollmentCacheDao {

    @Query("SELECT * FROM cached_annual_enrollment WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getYears(careerId: Long): List<AnnualEnrollmentEntity>

    @Query("DELETE FROM cached_annual_enrollment WHERE career_id = :careerId")
    suspend fun clearYears(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYears(rows: List<AnnualEnrollmentEntity>)

    @Transaction
    suspend fun replaceYears(careerId: Long, rows: List<AnnualEnrollmentEntity>) {
        clearYears(careerId)
        insertYears(rows)
    }
}
