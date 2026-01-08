package it.attendance100.mybicocca.data.api.elearning

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningCompletionApiTest : ElearningTestBase() {

    @Test
    suspend fun getActivitiesCompletionStatus() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val course = coursesResponse.courses.first()
            val completion = api.completion.getActivitiesCompletionStatus(session.wsToken, course.id, profile.userId)
            assertNotNull(completion)
            assertNotNull(completion.statuses)
        }
    }
}
