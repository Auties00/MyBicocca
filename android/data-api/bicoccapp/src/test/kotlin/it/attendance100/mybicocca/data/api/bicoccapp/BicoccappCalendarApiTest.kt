package it.attendance100.mybicocca.data.api.bicoccapp

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class BicoccappCalendarApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getCalendar() {
        val calendar = api.calendar.getCalendar(profile.enrollmentId)
        assertNotNull(calendar.days)
    }

    @Test
    suspend fun getAvailableCourses() {
        val courses = api.calendar.getAvailableCourses()
        assertNotNull(courses.courses)
    }

    @Test
    suspend fun getCourseDetail() {
        val coursesResponse = api.calendar.getAvailableCourses()
        val courses = coursesResponse.courses
        if (courses.isNotEmpty()) {
            val course = courses.firstOrNull()
            if (course != null) {
                val detail = api.calendar.getCourseDetail(course.activityCode, course.courseCode)
                assertNotNull(detail)
            }
        }
    }

    @Test
    suspend fun addCourseToCalendar() {
        val coursesResponse = api.calendar.getAvailableCourses()
        val courses = coursesResponse.courses
        val target = courses.firstOrNull()

        if (target != null) {
            val result = api.calendar.addCourseToCalendar(
                userId = profile.enrollmentId,
                cdsCode = target.degreeCourseCode,
                activityCode = target.activityCode,
                lessonName = target.lessonName,
                courseCode = target.courseCode,
                partition = target.partition
            )
            assertNotNull(result)
        }
    }
}
