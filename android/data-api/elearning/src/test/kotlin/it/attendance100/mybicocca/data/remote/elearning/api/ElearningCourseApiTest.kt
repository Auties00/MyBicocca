package it.attendance100.mybicocca.data.remote.elearning.api

import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningCourseCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.LinkedList

class ElearningCourseApiTest : ElearningTestApiBase() {
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
    suspend fun getCoursePublicInfo() {
        val info = api.courses.getCoursePublicInfo(60702)
        assertEquals(60702, info.id)
        assertTrue(info.name.isNotBlank())
        assertTrue(info.code.isNotBlank())
        assertTrue(info.viewUrl.contains("course/view.php"))
        assertNotNull(info.metadata)
        assertTrue(info.syllabus.isNotEmpty())
        assertTrue(info.syllabus.any { it.language == "it" })
        assertTrue(info.syllabus.all { it.fields.isNotEmpty() })
        assertTrue(info.staff.isNotEmpty())
        assertTrue(info.enrolmentMethods.isNotEmpty())
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
