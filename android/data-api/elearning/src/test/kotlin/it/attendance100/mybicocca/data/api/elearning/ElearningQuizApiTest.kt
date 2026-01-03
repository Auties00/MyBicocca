package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningQuizApiTest : ElearningTestBase() {

    @Test
    fun `quiz operations work`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)

        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val quizzesResponse = api.quizzes.getQuizzes(wsToken, courseIds)
            
            assertNotNull(quizzesResponse)
            assertNotNull(quizzesResponse.quizzes)
            println("Found ${quizzesResponse.quizzes.size} quizzes")

            if (quizzesResponse.quizzes.isNotEmpty()) {
                val quiz = quizzesResponse.quizzes.first()
                println("Testing with quiz: ${quiz.name} (${quiz.id})")
                
                val accessInfo = api.quizzes.getQuizAccessInfo(wsToken, quiz.id)
                assertNotNull(accessInfo)
                
                val attempts = api.quizzes.getUserAttempts(wsToken, quiz.id)
                assertNotNull(attempts)
                println("Found ${attempts.attempts.size} attempts for quiz ${quiz.id}")
            }
        }
    }
}
