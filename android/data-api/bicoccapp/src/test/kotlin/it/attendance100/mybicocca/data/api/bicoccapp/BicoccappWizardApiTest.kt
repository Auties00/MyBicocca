package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class BicoccappWizardApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getCategories() {
        val categories = api.wizard.getCategories()
        assertNotNull(categories.categories)
    }

    @Test
    suspend fun getDegreeTypes() {
        val categoriesResponse = api.wizard.getCategories()
        val categories = categoriesResponse.categories
        if (categories.isNotEmpty()) {
            val category = categories.first()
            val degreeTypes = api.wizard.getDegreeTypes(category.code)
            assertNotNull(degreeTypes.degrees)
        }
    }

    @Test
    suspend fun getDegreePrograms() {
        val categoriesResponse = api.wizard.getCategories()
        val categories = categoriesResponse.categories
        if (categories.isEmpty()) return
        val category = categories.first()

        val degreeTypesResponse = api.wizard.getDegreeTypes(category.code)
        val degrees = degreeTypesResponse.degrees
        if (degrees.isEmpty()) return
        val degree = degrees.first()

        val programs = api.wizard.getDegreePrograms(category.code, degree.code)
        assertNotNull(programs.lessonsByYear)
    }

    @Test
    suspend fun getCourseLessons() {
        val categoriesResponse = api.wizard.getCategories()
        val categories = categoriesResponse.categories
        if (categories.isEmpty()) return
        val category = categories.first()

        val degreeTypesResponse = api.wizard.getDegreeTypes(category.code)
        val degrees = degreeTypesResponse.degrees
        if (degrees.isEmpty()) return
        val degree = degrees.first()

        val programsResponse = api.wizard.getDegreePrograms(category.code, degree.code)
        val programsMap = programsResponse.lessonsByYear

        var courseCode: String? = null
        for (programs in programsMap.values) {
            for (program in programs) {
                courseCode = program.courseCode
                break
            }
            if (courseCode != null) break
        }

        if (courseCode != null) {
            val lessons = api.wizard.getCourseLessons(category.code, degree.code, courseCode)
            assertNotNull(lessons.lessonsByYear)
        }
    }

    @Test
    suspend fun getUserCourses() {
        val userCourses = api.wizard.getUserCourses(profile.enrollmentId)
        assertNotNull(userCourses.lessonsByYear)
    }
}
