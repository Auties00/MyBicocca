package it.attendance100.mybicocca.data.api.elearning

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningQuizApiTest : ElearningTestBase() {

    @Test
    suspend fun getQuizzes() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val quizzesResponse = api.quizzes.getQuizzes(session.wsToken, courseIds)
            assertNotNull(quizzesResponse)
            assertNotNull(quizzesResponse.quizzes)
        }
    }

    @Test
    suspend fun getQuizAccessInfo() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val quizzesResponse = api.quizzes.getQuizzes(session.wsToken, courseIds)
            if (quizzesResponse.quizzes.isNotEmpty()) {
                val quiz = quizzesResponse.quizzes.first()
                val accessInfo = api.quizzes.getQuizAccessInfo(session.wsToken, quiz.id)
                assertNotNull(accessInfo)
            }
        }
    }

    @Test
    suspend fun getUserAttempts() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val quizzesResponse = api.quizzes.getQuizzes(session.wsToken, courseIds)
            if (quizzesResponse.quizzes.isNotEmpty()) {
                val quiz = quizzesResponse.quizzes.first()
                val attempts = api.quizzes.getUserAttempts(session.wsToken, quiz.id)
                assertNotNull(attempts)
            }
        }
    }
}
