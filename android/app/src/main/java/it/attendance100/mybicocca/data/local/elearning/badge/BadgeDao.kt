package it.attendance100.mybicocca.data.local.elearning.badge

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query(
        "SELECT * FROM elearning_badges " +
            "WHERE account_id = :accountId " +
            "ORDER BY issued_at_ms DESC, name"
    )
    fun observeAll(accountId: String): Flow<List<BadgeEntity>>

    @Query(
        "SELECT * FROM elearning_badges " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY issued_at_ms DESC, name"
    )
    fun observeForCourse(accountId: String, courseId: Int): Flow<List<BadgeEntity>>

    @Upsert
    suspend fun upsert(rows: List<BadgeEntity>)

    @Query("DELETE FROM elearning_badges WHERE account_id = :accountId AND course_id IS :courseId")
    suspend fun deleteForCourse(accountId: String, courseId: Int?)

    @Query("DELETE FROM elearning_badges WHERE account_id = :accountId")
    suspend fun deleteForAccount(accountId: String)

    @Transaction
    suspend fun replaceForCourse(accountId: String, courseId: Int?, rows: List<BadgeEntity>) {
        deleteForCourse(accountId, courseId)
        if (rows.isNotEmpty()) upsert(rows)
    }
}
