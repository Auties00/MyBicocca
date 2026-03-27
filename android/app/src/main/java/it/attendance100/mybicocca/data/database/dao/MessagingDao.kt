package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.messaging.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagingDao {
    @Query("SELECT * FROM conversations")
    fun observeAll(): Flow<List<Conversation>>

    @Upsert
    suspend fun upsertAll(conversations: List<Conversation>)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
