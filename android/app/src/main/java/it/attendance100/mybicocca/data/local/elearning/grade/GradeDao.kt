package it.attendance100.mybicocca.data.local.elearning.grade

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room access to the per-account gradebook cache: per-course grade items (in gradebook
 * order) and the cross-course total overview (sorted by course name). Refreshes go
 * through the `replace*` transactions so observers see one atomic swap;
 * `clearAllForAccount` wipes both tables on sign-out.
 */
@Dao
interface GradeDao {

    @Query(
        "SELECT * FROM elearning_grade_items " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY sort_order"
    )
    fun observeCourseGradeItems(accountId: String, courseId: Int): Flow<List<GradeItemEntity>>

    @Query(
        "SELECT * FROM elearning_course_grade_overview " +
            "WHERE account_id = :accountId " +
            "ORDER BY course_name"
    )
    fun observeAllCourseGrades(accountId: String): Flow<List<CourseGradeOverviewEntity>>

    @Upsert
    suspend fun upsertItems(rows: List<GradeItemEntity>)

    @Upsert
    suspend fun upsertOverviews(rows: List<CourseGradeOverviewEntity>)

    @Query("DELETE FROM elearning_grade_items WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteItemsForCourse(accountId: String, courseId: Int)

    @Query("DELETE FROM elearning_course_grade_overview WHERE account_id = :accountId")
    suspend fun deleteOverviewsForAccount(accountId: String)

    @Query("DELETE FROM elearning_grade_items WHERE account_id = :accountId")
    suspend fun deleteItemsForAccount(accountId: String)

    @Transaction
    suspend fun replaceCourseGradeItems(accountId: String, courseId: Int, rows: List<GradeItemEntity>) {
        deleteItemsForCourse(accountId, courseId)
        if (rows.isNotEmpty()) upsertItems(rows)
    }

    @Transaction
    suspend fun replaceAllCourseGrades(accountId: String, rows: List<CourseGradeOverviewEntity>) {
        deleteOverviewsForAccount(accountId)
        if (rows.isNotEmpty()) upsertOverviews(rows)
    }

    @Transaction
    suspend fun clearAllForAccount(accountId: String) {
        deleteItemsForAccount(accountId)
        deleteOverviewsForAccount(accountId)
    }
}
