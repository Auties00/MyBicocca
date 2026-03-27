package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.evaluation.EvaluationEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationDao {
    @Query("SELECT * FROM evaluation_entries ORDER BY activityName")
    fun observeAll(): Flow<List<EvaluationEntry>>

    @Query("SELECT * FROM evaluation_entries WHERE isCompiled = 0")
    fun observePending(): Flow<List<EvaluationEntry>>

    @Upsert
    suspend fun upsertAll(entries: List<EvaluationEntry>)

    @Query("DELETE FROM evaluation_entries")
    suspend fun deleteAll()
}
