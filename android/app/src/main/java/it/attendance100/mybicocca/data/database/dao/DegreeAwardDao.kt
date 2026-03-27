package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.degreeaward.CommitteeApplication
import kotlinx.coroutines.flow.Flow

@Dao
interface DegreeAwardDao {
    @Query("SELECT * FROM committee_applications ORDER BY submissionDate DESC")
    fun observeAll(): Flow<List<CommitteeApplication>>

    @Query("SELECT * FROM committee_applications WHERE studentId = :studentId")
    fun observeByStudent(studentId: Long): Flow<List<CommitteeApplication>>

    @Upsert
    suspend fun upsertAll(applications: List<CommitteeApplication>)

    @Query("DELETE FROM committee_applications")
    suspend fun deleteAll()
}
