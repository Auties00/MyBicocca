package it.attendance100.mybicocca.data.local.elearning.quiz

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    @Query(
        "SELECT * FROM elearning_quizzes " +
            "WHERE account_id = :accountId AND course_id = :courseId " +
            "ORDER BY time_open_ms IS NULL, time_open_ms, name"
    )
    fun observeForCourse(accountId: String, courseId: Int): Flow<List<QuizEntity>>

    @Query("SELECT * FROM elearning_quizzes WHERE account_id = :accountId AND quiz_id = :quizId")
    fun observe(accountId: String, quizId: Int): Flow<QuizEntity?>

    @Query(
        "SELECT * FROM elearning_quiz_attempts " +
            "WHERE account_id = :accountId AND quiz_id = :quizId " +
            "ORDER BY attempt_number DESC"
    )
    fun observeAttempts(accountId: String, quizId: Int): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM elearning_quiz_best_grade WHERE account_id = :accountId AND quiz_id = :quizId")
    fun observeBestGrade(accountId: String, quizId: Int): Flow<QuizBestGradeEntity?>

    @Query(
        "SELECT * FROM elearning_quiz_attempt_answers " +
            "WHERE account_id = :accountId AND attempt_id = :attemptId " +
            "ORDER BY slot"
    )
    fun observeAnswers(accountId: String, attemptId: Int): Flow<List<QuizAttemptAnswerEntity>>

    @Upsert
    suspend fun upsertQuizzes(rows: List<QuizEntity>)

    @Upsert
    suspend fun upsertAttempts(rows: List<QuizAttemptEntity>)

    @Upsert
    suspend fun upsertBestGrade(row: QuizBestGradeEntity)

    @Upsert
    suspend fun upsertAnswers(rows: List<QuizAttemptAnswerEntity>)

    @Query("DELETE FROM elearning_quizzes WHERE account_id = :accountId AND course_id = :courseId")
    suspend fun deleteQuizzesForCourse(accountId: String, courseId: Int)

    @Query("DELETE FROM elearning_quiz_attempts WHERE account_id = :accountId AND quiz_id = :quizId")
    suspend fun deleteAttemptsForQuiz(accountId: String, quizId: Int)

    @Query("DELETE FROM elearning_quiz_attempt_answers WHERE account_id = :accountId AND attempt_id = :attemptId")
    suspend fun deleteAnswers(accountId: String, attemptId: Int)

    @Transaction
    suspend fun replaceForCourse(accountId: String, courseId: Int, rows: List<QuizEntity>) {
        deleteQuizzesForCourse(accountId, courseId)
        if (rows.isNotEmpty()) upsertQuizzes(rows)
    }

    @Transaction
    suspend fun replaceAttempts(accountId: String, quizId: Int, rows: List<QuizAttemptEntity>) {
        deleteAttemptsForQuiz(accountId, quizId)
        if (rows.isNotEmpty()) upsertAttempts(rows)
    }

    @Query("DELETE FROM elearning_quizzes WHERE account_id = :accountId")
    suspend fun deleteAllQuizzes(accountId: String)

    @Query("DELETE FROM elearning_quiz_attempts WHERE account_id = :accountId")
    suspend fun deleteAllAttempts(accountId: String)

    @Query("DELETE FROM elearning_quiz_best_grade WHERE account_id = :accountId")
    suspend fun deleteAllBestGrades(accountId: String)

    @Query("DELETE FROM elearning_quiz_attempt_answers WHERE account_id = :accountId")
    suspend fun deleteAllAnswers(accountId: String)

    @Transaction
    suspend fun clearAllForAccount(accountId: String) {
        deleteAllQuizzes(accountId)
        deleteAllAttempts(accountId)
        deleteAllBestGrades(accountId)
        deleteAllAnswers(accountId)
    }
}
