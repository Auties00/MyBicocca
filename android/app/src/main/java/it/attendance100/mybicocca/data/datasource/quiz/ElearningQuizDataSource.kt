package it.attendance100.mybicocca.data.datasource.quiz

import it.attendance100.mybicocca.data.api.elearning.ElearningApi
import it.attendance100.mybicocca.data.datastore.AuthTokenStore
import it.attendance100.mybicocca.data.model.quiz.Quiz
import it.attendance100.mybicocca.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ElearningQuizDataSource @Inject constructor(
    private val elearningApi: ElearningApi,
    private val authTokenStore: AuthTokenStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun getQuizzes(courseIds: List<Int>): List<Quiz> = withContext(ioDispatcher) {
        val wsToken = authTokenStore.elearningWsToken ?: return@withContext emptyList()
        if (courseIds.isEmpty()) return@withContext emptyList()

        elearningApi.quizzes.getQuizzes(wsToken, courseIds).quizzes.map { dto ->
            Quiz(
                id = dto.id,
                courseId = dto.courseId,
                name = dto.name,
                description = dto.introduction,
                timeLimit = dto.timeLimitSeconds,
                maxAttempts = dto.maximumAttempts,
                gradeMethod = dto.gradeMethod,
                openDate = dto.openTimestamp,
                closeDate = dto.closeTimestamp,
            )
        }
    }
}
