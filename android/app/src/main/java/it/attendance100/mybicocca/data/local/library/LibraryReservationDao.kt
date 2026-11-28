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

    @Query(
        "DELETE FROM library_reservation " +
            "WHERE end_epoch_seconds > :nowEpochSeconds OR reservation_id IN (:cancelledIds)",
    )
    suspend fun deleteUnendedOrCancelled(nowEpochSeconds: Long, cancelledIds: List<Int>)

    /**
     * Applies the server list in one transaction. The server is the source of truth for bookings
     * that have not ended; ended ones stay as history even once it stops listing them.
     */
    @Transaction
    suspend fun syncWithServer(
        reservations: List<LibraryReservationEntity>,
        cancelledIds: List<Int>,
        nowEpochSeconds: Long,
    ) {
        deleteUnendedOrCancelled(nowEpochSeconds, cancelledIds)
        upsertAll(reservations)
    }
}
