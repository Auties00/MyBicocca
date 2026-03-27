package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.forum.Forum
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumDao {
    @Query("SELECT * FROM forums")
    fun observeAll(): Flow<List<Forum>>

    @Query("SELECT * FROM forums WHERE courseId = :courseId")
    fun observeByCourse(courseId: Int): Flow<List<Forum>>

    @Upsert
    suspend fun upsertAll(forums: List<Forum>)

    @Query("DELETE FROM forums")
    suspend fun deleteAll()
}
