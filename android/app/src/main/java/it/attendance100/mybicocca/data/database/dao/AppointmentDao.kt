package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.appointment.Appointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY date DESC")
    fun observeAll(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE personId = :personId ORDER BY date DESC")
    fun observeByPerson(personId: Long): Flow<List<Appointment>>

    @Upsert
    suspend fun upsertAll(appointments: List<Appointment>)

    @Query("DELETE FROM appointments")
    suspend fun deleteAll()
}
