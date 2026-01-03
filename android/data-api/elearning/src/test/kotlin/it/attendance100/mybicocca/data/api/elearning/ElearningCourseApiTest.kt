package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElearningCourseApiTest : ElearningTestBase() {

    @Test
    fun `getUserCourses and getCourseContents return valid data`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)

        assertNotNull(coursesResponse)
        assertNotNull(coursesResponse.courses)
        
        if (coursesResponse.courses.isNotEmpty()) {
            val firstCourse = coursesResponse.courses.first()
            println("Testing with course: ${firstCourse.fullName} (${firstCourse.id})")
            
            val contents = api.courses.getCourseContents(wsToken, firstCourse.id)
            assertNotNull(contents)
            assertNotNull(contents.sections)
            
            // Just verifying we got some structure back
            println("Found ${contents.sections.size} sections")
        } else {
            println("WARNING: No courses found for user, skipping content test")
        }
    }

    @Test
    fun `getCoursesAreas and getCategoryContents return valid data`() = runBlocking {
        println("Fetching all areas...")
        val areas = api.courses.getCoursesAreas()
        assertNotNull(areas)
        assertTrue(areas.isNotEmpty(), "Should return at least one area")
        println("Found ${areas.size} areas")
        areas.forEach { area ->
            println("Area: ${area.name} (${area.categories.size} categories)")
            if (area.categories.isNotEmpty()) {
                val firstCategory = area.categories.first()
                println("  Testing category: ${firstCategory.name} -> ${firstCategory.url}")
                
                val contents = api.courses.getCourseCategoryContents(firstCategory)
                assertNotNull(contents)
                println("    Found ${contents.subcategories.size} subcategories and ${contents.courses.size} courses")
            }
        }
    }
}
