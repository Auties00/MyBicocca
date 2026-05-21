package it.attendance100.mybicocca.data.remote.elearning.api

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ElearningCalendarApiTest : ElearningTestApiBase() {

    @Test
    suspend fun getCalendarUpcomingView() {
        val upcoming = api.calendar.getCalendarUpcomingView(session.wsToken)
        assertNotNull(upcoming)
        assertNotNull(upcoming.events)
    }

    @Test
    suspend fun getCalendarMonthlyView() {
        val now = LocalDate.now()
        val monthly = api.calendar.getCalendarMonthlyView(session.wsToken, now.year, now.monthValue)
        assertNotNull(monthly)
        assertNotNull(monthly.weeks)
    }

    @Test
    suspend fun getActionEventsByTimesort() {
        val actionEvents = api.calendar.getActionEventsByTimesort(session.wsToken)
        assertNotNull(actionEvents)
        assertNotNull(actionEvents.events)
    }

    @Test
    suspend fun getActionEventsByCourse() {
        val coursesResponse = api.courses.getUserCourses(session.wsToken, profile.userId)
        for (course in coursesResponse.courses) {
            val events = api.calendar.getActionEventsByCourse(session.wsToken, course.id)
            assertNotNull(events)
            assertNotNull(events.events)
        }
    }
}
