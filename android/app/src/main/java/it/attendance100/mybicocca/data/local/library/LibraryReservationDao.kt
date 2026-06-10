package it.attendance100.mybicocca.data.local.library

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryReservationDao {

    @Query("SELECT * FROM library_reservation ORDER BY start_epoch_seconds")
    fun observeAll(): Flow<List<LibraryReservationEntity>>

    @Query("SELECT COUNT(*) FROM library_reservation")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(reservations: List<LibraryReservationEntity>)

    @Query("DELETE FROM library_reservation")
    suspend fun clear()

    @Query("DELETE FROM library_reservation WHERE reservation_id = :id")
    suspend fun delete(id: Int)

    /**
     * Mirrors the server list exactly — clears and rewrites the whole cache in one transaction,
     * since the server is the source of truth and this table is just the cache.
     */
    @Transaction
    suspend fun replaceAll(reservations: List<LibraryReservationEntity>) {
        clear()
        upsertAll(reservations)
    }
}
