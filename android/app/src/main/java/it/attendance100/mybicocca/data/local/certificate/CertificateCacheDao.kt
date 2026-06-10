package it.attendance100.mybicocca.data.local.certificate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Read/replace access to the offline certificate mirror. The list is replaced wholesale on a
 * successful fetch (delete-then-insert in one transaction) and read back ordered by the
 * preserved server position; reads are consulted only when a live call fails offline.
 */
@Dao
interface CertificateCacheDao {

    @Query("SELECT * FROM cached_certificate WHERE owner_id = :ownerId ORDER BY cache_order")
    suspend fun getCertificates(ownerId: Long): List<CertificateEntity>

    @Query("DELETE FROM cached_certificate WHERE owner_id = :ownerId")
    suspend fun clearCertificates(ownerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificates(rows: List<CertificateEntity>)

    @Transaction
    suspend fun replaceCertificates(ownerId: Long, rows: List<CertificateEntity>) {
        clearCertificates(ownerId)
        insertCertificates(rows)
    }
}
