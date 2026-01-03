package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningAssignApiTest : ElearningTestBase() {

    @Test
    fun `getAssignments works for enrolled courses`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)
        
        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val assignmentsResponse = api.assignments.getAssignments(wsToken, courseIds)
            
            assertNotNull(assignmentsResponse)
            assertNotNull(assignmentsResponse.courses)
            println("Found assignments for ${assignmentsResponse.courses.size} courses")
            
            // Try to find an assignment to test submission status
            val courseWithAssign = assignmentsResponse.courses.firstOrNull { it.assignments.isNotEmpty() }
            if (courseWithAssign != null) {
                val assignment = courseWithAssign.assignments.first()
                println("Testing submission status for assignment: ${assignment.name}")
                
                val status = api.assignments.getSubmissionStatus(wsToken, assignment.id)
                assertNotNull(status)
            }
        }
    }

    @Test
    fun `getAssignmentsForCourse works for all enrolled courses`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)

        for (course in coursesResponse.courses) {
            println("Getting assignments for course: ${course.fullName} (${course.id})")
            val assignmentsResponse = api.assignments.getAssignmentsForCourse(wsToken, course.id)
            assertNotNull(assignmentsResponse)
            assertNotNull(assignmentsResponse.courses)
        }
    }
}
