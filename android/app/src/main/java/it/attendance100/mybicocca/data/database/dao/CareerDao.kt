package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.career.Career
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    @Query("SELECT * FROM careers")
    fun observeAll(): Flow<List<Career>>

    @Query("SELECT * FROM careers")
    suspend fun getAll(): List<Career>

    @Upsert
    suspend fun upsertAll(careers: List<Career>)

    @Query("DELETE FROM careers")
    suspend fun deleteAll()
}
