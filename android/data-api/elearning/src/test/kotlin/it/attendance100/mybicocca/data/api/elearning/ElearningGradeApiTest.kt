package it.attendance100.mybicocca.data.api.elearning

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningGradeApiTest : ElearningTestApiBase() {

    @Test
    suspend fun getCourseGrades() {
        val grades = api.grades.getCourseGrades(session.wsToken, profile.userId)
        assertNotNull(grades)
        assertNotNull(grades.grades)
    }

    @Test
    suspend fun getGradeItems() {
        val grades = api.grades.getCourseGrades(session.wsToken, profile.userId)
        if (grades.grades.isNotEmpty()) {
            val courseId = grades.grades.first().courseId
            val gradeItems = api.grades.getGradeItems(session.wsToken, courseId, profile.userId)
            assertNotNull(gradeItems)
            assertNotNull(gradeItems.userGrades)
        }
    }
}
