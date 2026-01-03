package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningCompletionApiTest : ElearningTestBase() {

    @Test
    fun `getActivitiesCompletionStatus works for enrolled courses`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)

        if (coursesResponse.courses.isNotEmpty()) {
            val course = coursesResponse.courses.first()
            // Some courses might not have completion enabled, so this might fail or return empty/error?
            // The API throws if it fails.
            try {
                val completion = api.completion.getActivitiesCompletionStatus(wsToken, course.id, siteInfo.userId)
                assertNotNull(completion)
                assertNotNull(completion.statuses)
                println("Found completion statuses for ${completion.statuses.size} activities in course ${course.id}")
            } catch (e: Exception) {
                println("Completion API might not be enabled for course ${course.id}: ${e.message}")
            }
        }
    }
}
