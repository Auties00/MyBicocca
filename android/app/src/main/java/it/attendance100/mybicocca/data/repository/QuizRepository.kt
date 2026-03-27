package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.database.dao.CourseDao
import it.attendance100.mybicocca.data.database.dao.QuizDao
import it.attendance100.mybicocca.data.datasource.quiz.ElearningQuizDataSource
import it.attendance100.mybicocca.data.model.quiz.Quiz
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val dataSource: ElearningQuizDataSource,
    private val dao: QuizDao,
    private val courseDao: CourseDao,
) {
    fun observeAll(): Flow<List<Quiz>> = dao.observeAll()

    fun observeByCourse(courseId: Int): Flow<List<Quiz>> = dao.observeByCourse(courseId)

    suspend fun refresh(): Result<Unit> = runCatching {
        val courseIds = courseDao.observeAll().first().map { it.id }
        val quizzes = dataSource.getQuizzes(courseIds)
        dao.upsertAll(quizzes)
    }
}
