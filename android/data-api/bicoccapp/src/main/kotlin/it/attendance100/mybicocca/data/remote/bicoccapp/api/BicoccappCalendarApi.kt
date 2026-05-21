package it.attendance100.mybicocca.data.remote.bicoccapp.api

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappCalendarCoursesResponse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappCalendarDay
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappCalendarResponse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappCourse
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappCourseDetails
import it.attendance100.mybicocca.data.remote.bicoccapp.dto.BicoccappSetCalendarResult
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * API client for calendar-related operations.
 */
class BicoccappCalendarApi(
    client: HttpClient,
    json: Json
) : BicoccappAbstractApi(client, json) {
    companion object {
        /**
         * Italian date formatter.
         **/
        private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    }

    /**
     * Retrieves calendar events (lectures, exams, deadlines) for a student.
     *
     * @param enrollmentId Student's enrollment number (matricola).
     * @param date Optional date of the results (this is not a start date, if specified the method will return only the events at this date)
     * @return Calendar with days and events.
     */
    suspend fun getCalendarDays(
        enrollmentId: String,
        date: LocalDate? = null
    ): List<BicoccappCalendarDay> {
        val response = executeJsonGet<BicoccappCalendarResponse>("/calendar") {
            parameter("matricId", enrollmentId)
            date?.let { parameter("date", formatter.format(it)) }
        }
        return response.days
    }

    /**
     * Lists all courses available for calendar subscription.
     *
     * @return Available courses.
     */
    suspend fun getAvailableCourses(): List<BicoccappCourse> {
        val response = executeJsonGet<BicoccappCalendarCoursesResponse>("/calendar_courses")
        return response.courses
    }

    /**
     * Retrieves detailed information about a specific course.
     *
     * @param activityCode Unique identifier for this course instance.
     * @param courseCode General course identifier.
     * @return Course details with events.
     */
    suspend fun getCourseDetail(
        activityCode: String,
        courseCode: String
    ): BicoccappCourseDetails {
        val response = executeJsonGet<BicoccappCourseDetails>("/course_detail") {
            parameter("activityCode", activityCode)
            parameter("courseCode", courseCode)
        }
        return response
    }

    /**
     * Adds a course's lecture schedule to the user's calendar.
     *
     * @param appUserId User identifier.
     * @param degreeCourseCode Degree program code (Corso di Studi).
     * @param activityCode Specific course instance identifier.
     * @param lessonName Course display name for calendar events.
     * @param courseCode General course code.
     * @param partition Optional section identifier for partitioned courses.
     * @return Result of the operation.
     */
    suspend fun addCourseToCalendar(
        appUserId: String,
        degreeCourseCode: String,
        activityCode: String,
        lessonName: String,
        courseCode: String,
        partition: String?
    ): BicoccappSetCalendarResult {
        return executeJsonPost<BicoccappSetCalendarResult>("/set_calendar") {
            setBody(FormDataContent(Parameters.build {
                append("user_id", appUserId)
                append("cdsCode", degreeCourseCode)
                append("activityCode", activityCode)
                append("lessonName", lessonName)
                append("courseCode", courseCode)
                partition?.let { append("partition", it) }
            }))
        }
    }

    // There is no way to remove courses (bruh)
}
