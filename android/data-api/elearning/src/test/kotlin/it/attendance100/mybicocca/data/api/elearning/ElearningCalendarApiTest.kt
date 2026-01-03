package it.attendance100.mybicocca.data.api.elearning

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ElearningCalendarApiTest : ElearningTestBase() {

    @Test
    fun `calendar operations return valid data`() = runBlocking {
        // Upcoming View
        val upcoming = api.calendar.getCalendarUpcomingView(wsToken)
        assertNotNull(upcoming)
        assertNotNull(upcoming.events)
        println("Found ${upcoming.events.size} upcoming events")

        // Monthly View
        val now = LocalDate.now()
        val monthly = api.calendar.getCalendarMonthlyView(wsToken, now.year, now.monthValue)
        assertNotNull(monthly)
        assertNotNull(monthly.weeks)
        println("Found monthly view with ${monthly.weeks.size} weeks")

        // Action Events
        val actionEvents = api.calendar.getActionEventsByTimesort(wsToken)
        assertNotNull(actionEvents)
        assertNotNull(actionEvents.events)
        println("Found ${actionEvents.events.size} action events")
    }

    @Test
    fun `getActionEventsByCourse works for all enrolled courses`() = runBlocking {
        val siteInfo = api.site.getSiteInfo(wsToken)
        val coursesResponse = api.courses.getUserCourses(wsToken, siteInfo.userId)

        for (course in coursesResponse.courses) {
            println("Getting action events for course: ${course.fullName} (${course.id})")

            // 1. Default parameters
            val defaultEvents = api.calendar.getActionEventsByCourse(wsToken, course.id)
            assertNotNull(defaultEvents)
            assertNotNull(defaultEvents.events)

            // 2. With Time Range (e.g., +/- 1 month)
            // Moodle usually expects seconds for timestamps
            val nowSec = System.currentTimeMillis() / 1000
            val oneMonth = 30 * 24 * 60 * 60
            val timeRangeEvents = api.calendar.getActionEventsByCourse(
                wsToken,
                course.id,
                timesortFrom = nowSec - oneMonth,
                timesortTo = nowSec + oneMonth
            )
            assertNotNull(timeRangeEvents)
            assertNotNull(timeRangeEvents.events)

            // 3. With Limit
            val limitedEvents = api.calendar.getActionEventsByCourse(
                wsToken,
                course.id,
                limitNum = 5
            )
            assertNotNull(limitedEvents)
            assertNotNull(limitedEvents.events)

            // 4. With Search
            val searchEvents = api.calendar.getActionEventsByCourse(
                wsToken,
                course.id,
                searchValue = "test"
            )
            assertNotNull(searchEvents)
            assertNotNull(searchEvents.events)

            // 5. With afterEventId (pagination)
            if (defaultEvents.events.isNotEmpty()) {
                val firstEventId = defaultEvents.events.first().id
                val pagedEvents = api.calendar.getActionEventsByCourse(
                    wsToken,
                    course.id,
                    afterEventId = firstEventId
                )
                assertNotNull(pagedEvents)
                assertNotNull(pagedEvents.events)
            }
        }
    }
}
