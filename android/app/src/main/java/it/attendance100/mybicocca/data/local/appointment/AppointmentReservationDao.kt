package it.attendance100.mybicocca.data.local.appointment

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentReservationDao {

    @Query("SELECT * FROM appointment_reservation ORDER BY start_epoch_seconds")
    fun observeAll(): Flow<List<AppointmentReservationEntity>>

    @Query("SELECT * FROM appointment_reservation")
    suspend fun getAll(): List<AppointmentReservationEntity>

    @Upsert
    suspend fun upsert(reservation: AppointmentReservationEntity)

    @Query("DELETE FROM appointment_reservation WHERE code = :code")
    suspend fun delete(code: String)
}
