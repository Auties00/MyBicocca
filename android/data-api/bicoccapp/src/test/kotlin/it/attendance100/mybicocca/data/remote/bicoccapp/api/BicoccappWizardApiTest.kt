package it.attendance100.mybicocca.data.remote.bicoccapp.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BicoccappWizardApiTest : BicoccappApiTestBase() {
    @Test
    suspend fun getAll() {
        val departments = api.wizard.getDepartments()
        for(department in departments) {
            val degrees = api.wizard.getDegrees(department)
            for(degree in degrees) {
                val courses = api.wizard.getCourses(department, degree)
                for(lesson in courses) {
                    val lessonsByYear = api.wizard.getLessons(department, degree, lesson)
                    assertNotNull(lessonsByYear)
                    assertTrue(lessonsByYear.isNotEmpty())
                }
            }
        }
    }

    @Test
    suspend fun getUserCourses() {
        val lessonsByYear = api.wizard.getUserCourses(profile.enrollmentId)
        assertNotNull(lessonsByYear)
        assertTrue(lessonsByYear.isNotEmpty())
    }
}
