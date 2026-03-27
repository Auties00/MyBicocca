package it.attendance100.mybicocca.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import it.attendance100.mybicocca.data.model.quiz.Quiz
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes")
    fun observeAll(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    fun observeByCourse(courseId: Int): Flow<List<Quiz>>

    @Upsert
    suspend fun upsertAll(quizzes: List<Quiz>)

    @Query("DELETE FROM quizzes")
    suspend fun deleteAll()
}
