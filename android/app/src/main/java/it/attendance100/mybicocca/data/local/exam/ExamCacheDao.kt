package it.attendance100.mybicocca.data.local.exam

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Read/replace access to the offline exam mirrors. Each list is replaced wholesale on a
 * successful fetch (delete-then-insert in one transaction) and read back ordered by the
 * preserved server position; reads are consulted only when a live call fails offline.
 */
@Dao
interface ExamCacheDao {

    @Query("SELECT * FROM cached_booked_exam WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getBookings(careerId: Long): List<BookedExamEntity>

    @Query("DELETE FROM cached_booked_exam WHERE career_id = :careerId")
    suspend fun clearBookings(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(rows: List<BookedExamEntity>)

    @Transaction
    suspend fun replaceBookings(careerId: Long, rows: List<BookedExamEntity>) {
        clearBookings(careerId)
        insertBookings(rows)
    }

    @Query("SELECT * FROM cached_exam_call WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getCalls(careerId: Long): List<ExamCallEntity>

    @Query("DELETE FROM cached_exam_call WHERE career_id = :careerId")
    suspend fun clearCalls(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalls(rows: List<ExamCallEntity>)

    @Transaction
    suspend fun replaceCalls(careerId: Long, rows: List<ExamCallEntity>) {
        clearCalls(careerId)
        insertCalls(rows)
    }

    @Query("SELECT * FROM cached_exam_result WHERE career_id = :careerId ORDER BY cache_order")
    suspend fun getResults(careerId: Long): List<ExamResultEntity>

    @Query("DELETE FROM cached_exam_result WHERE career_id = :careerId")
    suspend fun clearResults(careerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(rows: List<ExamResultEntity>)

    @Transaction
    suspend fun replaceResults(careerId: Long, rows: List<ExamResultEntity>) {
        clearResults(careerId)
        insertResults(rows)
    }
}
