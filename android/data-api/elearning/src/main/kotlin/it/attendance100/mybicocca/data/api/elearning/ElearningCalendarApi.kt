package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetActionEventsByCourseRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetActionEventsByCourseResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetActionEventsByTimesortRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetActionEventsByTimesortResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCalendarMonthlyViewRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCalendarMonthlyViewResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCalendarUpcomingViewRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetCalendarUpcomingViewResponse
import kotlinx.serialization.json.Json

/**
 * API for calendar-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningCalendarApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets action events sorted by time.
     *
     * @param wsToken The web service token
     * @param timesortFrom Start timestamp for filtering
     * @param timesortTo End timestamp for filtering
     * @param afterEventId Return events after this event ID (for pagination)
     * @param limitNum Maximum number of events to return
     * @param searchValue Optional search filter
     * @return List of action events
     */
    suspend fun getActionEventsByTimesort(
        wsToken: String,
        timesortFrom: Long? = null,
        timesortTo: Long? = null,
        afterEventId: Int? = null,
        limitNum: Int = 20,
        searchValue: String? = null
    ): ElearningGetActionEventsByTimesortResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetActionEventsByTimesortRequest(
                timesortFrom, timesortTo, afterEventId, limitNum, searchValue
            )
        )
    }

    /**
     * Gets action events for a specific course.
     *
     * @param wsToken The web service token
     * @param courseId The course ID
     * @param timesortFrom Start timestamp for filtering
     * @param timesortTo End timestamp for filtering
     * @param afterEventId Return events after this event ID (for pagination)
     * @param limitNum Maximum number of events to return
     * @param searchValue Optional search filter
     * @return List of action events for the course
     */
    suspend fun getActionEventsByCourse(
        wsToken: String,
        courseId: Int,
        timesortFrom: Long? = null,
        timesortTo: Long? = null,
        afterEventId: Int? = null,
        limitNum: Int = 20,
        searchValue: String? = null
    ): ElearningGetActionEventsByCourseResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetActionEventsByCourseRequest(
                courseId, timesortFrom, timesortTo, afterEventId, limitNum, searchValue
            )
        )
    }

    /**
     * Gets the calendar monthly view.
     *
     * @param wsToken The web service token
     * @param year The year
     * @param month The month (1-12)
     * @param courseId Optional course filter
     * @param categoryId Optional category filter
     * @param includeNavigation Whether to include navigation info
     * @param mini Whether to return a mini view
     * @return Monthly calendar view with weeks and days
     */
    suspend fun getCalendarMonthlyView(
        wsToken: String,
        year: Int,
        month: Int,
        courseId: Int? = null,
        categoryId: Int? = null,
        includeNavigation: Boolean = true,
        mini: Boolean = false
    ): ElearningGetCalendarMonthlyViewResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetCalendarMonthlyViewRequest(year, month, courseId, categoryId, includeNavigation, mini)
        )
    }

    /**
     * Gets the upcoming events view.
     *
     * @param wsToken The web service token
     * @param courseId Optional course filter
     * @param categoryId Optional category filter
     * @return Upcoming events
     */
    suspend fun getCalendarUpcomingView(
        wsToken: String,
        courseId: Int? = null,
        categoryId: Int? = null
    ): ElearningGetCalendarUpcomingViewResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetCalendarUpcomingViewRequest(courseId, categoryId)
        )
    }
}
