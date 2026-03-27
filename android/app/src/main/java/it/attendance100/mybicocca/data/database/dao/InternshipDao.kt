package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.internship.InternshipApplication
import kotlinx.coroutines.flow.Flow

@Dao
interface InternshipDao {
    @Query("SELECT * FROM internship_applications ORDER BY startDate DESC")
    fun observeAll(): Flow<List<InternshipApplication>>

    @Query("SELECT * FROM internship_applications WHERE studentId = :studentId")
    fun observeByStudent(studentId: Long): Flow<List<InternshipApplication>>

    @Upsert
    suspend fun upsertAll(applications: List<InternshipApplication>)

    @Query("DELETE FROM internship_applications")
    suspend fun deleteAll()
}
