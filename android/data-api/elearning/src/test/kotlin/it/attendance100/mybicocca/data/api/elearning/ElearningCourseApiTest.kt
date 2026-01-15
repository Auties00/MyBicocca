package it.attendance100.mybicocca.data.api.elearning

import it.attendance100.mybicocca.data.dto.elearning.ElearningCourse
import it.attendance100.mybicocca.data.dto.elearning.ElearningCourseCategory
import it.attendance100.mybicocca.data.exception.ElearningException
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.LinkedList

class ElearningCourseApiTest : ElearningTestBase() {
    @Test
    suspend fun getUserCourses() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        assertNotNull(coursesResponse)
        assertNotNull(coursesResponse.courses)
    }

    @Test
    suspend fun getCourseContents() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        if (coursesResponse.courses.isNotEmpty()) {
            val firstCourse = coursesResponse.courses.first()
            val contents = api.courses.getCourseContents(session.wsToken, firstCourse.id)
            assertNotNull(contents)
            assertNotNull(contents.sections)
        }
    }

    @Test
    suspend fun getCoursesAreas() {
        val areas = api.courses.getCoursesAreas()
        assertNotNull(areas)
        assertTrue(areas.isNotEmpty())
    }

    @Test
    suspend fun getCourseCategoryContents() {
        val areas = api.courses.getCoursesAreas()
        if (areas.isNotEmpty()) {
            val firstArea = areas.first()
            if (firstArea.categories.isNotEmpty()) {
                val firstCategory = firstArea.categories.first()
                val contents = api.courses.getCourseCategoryContents(firstCategory)
                assertNotNull(contents)
            }
        }
    }

    @Test
    suspend fun subscribeAndUnenrollFromCourse() {
        val enrolledCourses = api.courses.getUserCourses(session.wsToken, profile.userId)
            .courses
            .associateBy { it.id }

        val areas = api.courses.getCoursesAreas()
        for(area in areas) {
            val categories = LinkedList<ElearningCourseCategory>()
            categories.addAll(area.categories)
            while (!categories.isEmpty()) {
                val category = categories.removeFirst()
                val contents = api.courses.getCourseCategoryContents(category)
                for(course in contents.courses) {
                    val enrolledCourse = enrolledCourses[course.id]
                    if(enrolledCourse == null) {
                        val subscriptionResult = api.courses.enrollIntoCourse(session.wsToken, course)
                        assertTrue(subscriptionResult.status)
                        val enrolledCourse = api.courses.getUserCourses(session.wsToken, profile.userId)
                            .courses
                            .firstOrNull { it.id == course.id }
                        assertNotNull(enrolledCourse)
                        return
                    }
                }
                categories.addAll(contents.subcategories)
            }
        }
    }
}
