package it.attendance100.mybicocca.data.remote.bicoccapp.api

import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappSetCalendarResult
import org.jsoup.helper.Validate.fail
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BicoccappCalendarApiTest : BicoccappApiTestBase() {

    @Test
    suspend fun getCalendarDays() {
        val days = api.calendar.getCalendarDays(profile.enrollmentId)
        assertNotNull(days)
        assertTrue(days.isNotEmpty())
    }

    @Test
    suspend fun getCalendarDaysForDate() {
        val date = LocalDate.now()
        val days = api.calendar.getCalendarDays(profile.enrollmentId, date)
        assertNotNull(days)
        assertTrue(days.size == 1)
        assertTrue(days.first().date.equals(date))
    }

    @Test
    suspend fun getAvailableCourses() {
        val courses = api.calendar.getAvailableCourses()
        assertNotNull(courses)
        assertTrue(courses.isNotEmpty())
    }

    @Test
    suspend fun getCourseDetail() {
        val courses = api.calendar.getAvailableCourses()
        assertNotNull(courses)
        assertTrue(courses.isNotEmpty())
        for(course in courses) {
            val detail = api.calendar.getCourseDetail(course.activityCode, course.courseCode)
            assertNotNull(detail)
        }
    }

    @Test
    suspend fun addCourseToCalendar() {
        val courses = api.calendar.getAvailableCourses()
        for(course in courses) {
            val result = api.calendar.addCourseToCalendar(
                appUserId = profile.appUserId,
                degreeCourseCode = course.degreeCourseCode,
                activityCode = course.activityCode,
                lessonName = course.lessonName,
                courseCode = course.courseCode,
                partition = course.partition
            )
            assertNotNull(result)
            if (result is BicoccappSetCalendarResult.Error) {
                fail(result.message)
            }
        }
    }
}
