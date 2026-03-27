package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.assignment.Assignment
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments")
    fun observeAll(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE courseId = :courseId")
    fun observeByCourse(courseId: Int): Flow<List<Assignment>>

    @Upsert
    suspend fun upsertAll(assignments: List<Assignment>)

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()
}
