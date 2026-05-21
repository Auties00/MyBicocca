package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningAssignApiTest : ElearningTestApiBase() {

    @Test
    suspend fun getAssignments() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val assignmentsResponse = api.assignments.getAssignments(session.wsToken, courseIds)
            assertNotNull(assignmentsResponse)
            assertNotNull(assignmentsResponse.courses)
        }
    }

    @Test
    suspend fun getAssignmentsForCourse() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        for (course in coursesResponse.courses) {
            val assignmentsResponse = api.assignments.getAssignmentsForCourse(session.wsToken, course.id)
            assertNotNull(assignmentsResponse)
            assertNotNull(assignmentsResponse.courses)
        }
    }

    @Test
    suspend fun getSubmissionStatus() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val courseIds = coursesResponse.courses.map { it.id }
            val assignmentsResponse = api.assignments.getAssignments(session.wsToken, courseIds)
            val courseWithAssign = assignmentsResponse.courses.firstOrNull { it.assignments.isNotEmpty() }
            if (courseWithAssign != null) {
                val assignment = courseWithAssign.assignments.first()
                val status = api.assignments.getSubmissionStatus(session.wsToken, assignment.id)
                assertNotNull(status)
            }
        }
    }
}
