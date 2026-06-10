package it.attendance100.mybicocca.data.local.transcript

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room access to the career-scoped transcript cache: libretto rows, stats and the
 * grade-rollup aggregate. Account-wide deletes resolve the account's careers through the
 * `careers` table, so a sign-out purges every career's transcript in one statement.
 */
@Dao
interface TranscriptDao {

    /** Rows ordered chronologically by exam date (dateless rows last, then by id). */
    @Query(
        "SELECT * FROM transcript_rows WHERE career_id = :careerId " +
            "ORDER BY CASE WHEN exam_date IS NULL THEN 1 ELSE 0 END, exam_date ASC, id ASC"
    )
    fun observeRows(careerId: Long): Flow<List<TranscriptRowEntity>>

    @Query("SELECT * FROM transcript_stats WHERE career_id = :careerId")
    fun observeStats(careerId: Long): Flow<TranscriptStatsEntity?>

    /** SQL-side aggregation over passed, numerically graded rows; one row or null. */
    @Query(
        "SELECT " +
            "COUNT(*) AS graded_exam_count, " +
            "SUM(CAST(grade AS INTEGER)) AS grade_sum, " +
            "SUM(CAST(grade AS REAL) * credits) AS weighted_grade_sum, " +
            "SUM(credits) AS graded_credits_sum " +
            "FROM transcript_rows " +
            "WHERE career_id = :careerId AND state = 'Passed' AND grade IS NOT NULL"
    )
    fun observeGradeRollup(careerId: Long): Flow<GradeRollupProjection?>

    @Query(
        "SELECT activity_name FROM transcript_rows " +
            "WHERE career_id = :careerId AND state = 'Passed'"
    )
    suspend fun getPassedActivityNames(careerId: Long): List<String>

    @Upsert
    suspend fun upsertRows(rows: List<TranscriptRowEntity>)

    @Upsert
    suspend fun upsertStats(stats: TranscriptStatsEntity)

    @Query("DELETE FROM transcript_rows WHERE career_id = :careerId")
    suspend fun deleteRows(careerId: Long)

    @Query("DELETE FROM transcript_stats WHERE career_id = :careerId")
    suspend fun deleteStats(careerId: Long)

    @Query(
        "DELETE FROM transcript_rows WHERE career_id IN " +
            "(SELECT id FROM careers WHERE account_id = :accountId)"
    )
    suspend fun deleteRowsForAccount(accountId: String)

    @Query(
        "DELETE FROM transcript_stats WHERE career_id IN " +
            "(SELECT id FROM careers WHERE account_id = :accountId)"
    )
    suspend fun deleteStatsForAccount(accountId: String)

    /**
     * Atomically swaps a career's cached transcript: rows are deleted and rewritten so
     * activities removed server-side disappear, while the stats row is upserted.
     */
    @Transaction
    suspend fun replaceAll(careerId: Long, rows: List<TranscriptRowEntity>, stats: TranscriptStatsEntity) {
        deleteRows(careerId)
        if (rows.isNotEmpty()) upsertRows(rows)
        upsertStats(stats)
    }
}
