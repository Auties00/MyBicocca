package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.exam.ExamBooking
import it.attendance100.mybicocca.data.model.exam.ExamCall
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_calls ORDER BY date")
    fun observeExamCalls(): Flow<List<ExamCall>>

    @Query("SELECT * FROM exam_calls WHERE careerId = :careerId ORDER BY date")
    fun observeExamCallsByCareer(careerId: Long): Flow<List<ExamCall>>

    @Upsert
    suspend fun upsertCalls(calls: List<ExamCall>)

    @Query("DELETE FROM exam_calls")
    suspend fun deleteAllCalls()

    @Query("SELECT * FROM exam_bookings ORDER BY examDate")
    fun observeBookings(): Flow<List<ExamBooking>>

    @Upsert
    suspend fun upsertBookings(bookings: List<ExamBooking>)

    @Query("DELETE FROM exam_bookings")
    suspend fun deleteAllBookings()

    @Query("SELECT * FROM exam_calls WHERE id = :id")
    suspend fun getExamCallById(id: Long): ExamCall?
}
