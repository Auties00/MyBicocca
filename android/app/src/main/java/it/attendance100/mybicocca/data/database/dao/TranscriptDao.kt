package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.transcript.RecordBookRow
import it.attendance100.mybicocca.data.model.transcript.RecordBookStats
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscriptDao {
    @Query("SELECT * FROM record_book_rows WHERE careerId = :careerId")
    fun observeRows(careerId: Long): Flow<List<RecordBookRow>>

    @Upsert
    suspend fun upsertAllRows(rows: List<RecordBookRow>)

    @Query("DELETE FROM record_book_rows WHERE careerId = :careerId")
    suspend fun deleteRowsByCareerId(careerId: Long)

    @Query("SELECT * FROM record_book_stats WHERE careerId = :careerId")
    fun observeStats(careerId: Long): Flow<RecordBookStats?>

    @Upsert
    suspend fun upsertStats(stats: RecordBookStats)

    @Query("DELETE FROM record_book_stats WHERE careerId = :careerId")
    suspend fun deleteStatsByCareerId(careerId: Long)
}
