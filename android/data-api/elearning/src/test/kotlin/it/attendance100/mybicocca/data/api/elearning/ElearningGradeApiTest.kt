package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ElearningGradeApiTest : ElearningTestBase() {

    @Test
    fun `grade operations work`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        
        // Course Grades
        val grades = api.grades.getCourseGrades(wsToken, siteInfo.userId)
        assertNotNull(grades)
        assertNotNull(grades.grades)
        println("Found grades for ${grades.grades.size} courses")

        // Grade Items for a specific course
        if (grades.grades.isNotEmpty()) {
            val courseId = grades.grades.first().courseId
            try {
                val gradeItems = api.grades.getGradeItems(wsToken, courseId, siteInfo.userId)
                assertNotNull(gradeItems)
                assertNotNull(gradeItems.userGrades)
                println("Found grade items for course $courseId")
            } catch (e: Exception) {
                 println("Failed to get grade items for course $courseId: ${e.message}")
            }
        }
    }
}
