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

    /**
     * Replaces the booking list while carrying over each row's lazily fetched total.
     * Returns the merged rows as persisted.
     */
    @Transaction
    suspend fun replaceBookingsPreservingTotals(
        careerId: Long,
        rows: List<BookedExamEntity>,
    ): List<BookedExamEntity> {
        val knownTotals = getBookings(careerId)
            .filter { it.totalBookings != null }
            .associate { Triple(it.courseOfStudyId, it.activityId, it.callId) to it.totalBookings }
        val merged = rows.map { row ->
            if (row.totalBookings != null) row
            else knownTotals[Triple(row.courseOfStudyId, row.activityId, row.callId)]
                ?.let { row.copy(totalBookings = it) } ?: row
        }
        clearBookings(careerId)
        insertBookings(merged)
        return merged
    }

    /** Persists a lazily fetched numIscritti onto its cached booking row (see getCallTotalBookings). */
    @Query(
        "UPDATE cached_booked_exam SET total_bookings = :totalBookings " +
                "WHERE career_id = :careerId AND course_of_study_id = :courseOfStudyId " +
                "AND activity_id = :activityId AND call_id = :callId",
    )
    suspend fun updateBookingTotal(
        careerId: Long,
        courseOfStudyId: Long,
        activityId: Long,
        callId: Int,
        totalBookings: Int,
    )

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
