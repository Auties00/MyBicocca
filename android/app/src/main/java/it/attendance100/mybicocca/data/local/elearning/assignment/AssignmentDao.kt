package it.attendance100.mybicocca.data.local.elearning.assignment

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    @Query(
        "SELECT * FROM elearning_assignments " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY due_date_ms IS NULL, due_date_ms, name"
    )
    fun observeForCourse(accountId: String, courseId: Int): Flow<List<AssignmentEntity>>

    @Query(
        "SELECT * FROM elearning_assignments " +
            "WHERE account_id = :accountId AND assignment_id = :assignmentId"
    )
    fun observe(accountId: String, assignmentId: Int): Flow<AssignmentEntity?>

    @Upsert
    suspend fun upsertAll(rows: List<AssignmentEntity>)

    @Upsert
    suspend fun upsert(row: AssignmentEntity)

    @Query("DELETE FROM elearning_assignments WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteForCourse(accountId: String, courseId: Int)

    @Query("DELETE FROM elearning_assignments WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: String)

    @Transaction
    suspend fun replaceForCourse(accountId: String, courseId: Int, rows: List<AssignmentEntity>) {
        deleteForCourse(accountId, courseId)
        if (rows.isNotEmpty()) upsertAll(rows)
    }
}
