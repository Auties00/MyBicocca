package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.badge.Badge
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Query("SELECT * FROM badges")
    fun observeAll(): Flow<List<Badge>>

    @Upsert
    suspend fun upsertAll(badges: List<Badge>)

    @Query("DELETE FROM badges")
    suspend fun deleteAll()
}
