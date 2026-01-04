package it.attendance100.mybicocca.data.api.bicoccapp

import de.jensklingenberg.ktorfit.Response
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarCoursesResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseDetailResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappSetCalendarResponse

/**
 * Calendar API for managing academic schedules and course subscriptions.
 */
interface BicoccappCalendarApi {
    /**
     * Retrieves calendar events (lectures, exams, deadlines) for a student.
     *
     * @param enrollmentId Student's enrollment number (matricola).
     * @param date Optional ISO 8601 date to center the view (defaults to current date).
     * @return Calendar events for the specified period.
     */
    @GET("calendar")
    suspend fun getCalendar(
        @Query("matricId") enrollmentId: String,
        @Query("date") date: String? = null
    ): Response<BicoccappCalendarResponse>

    /**
     * Lists all courses available for calendar subscription.
     *
     * @return Course catalog with subscription status for each course.
     */
    @GET("calendar_courses")
    suspend fun getAvailableCourses(): Response<BicoccappCalendarCoursesResponse>

    /**
     * Retrieves detailed information about a specific course.
     *
     * @param activityCode Unique identifier for this course instance.
     * @param courseCode General course identifier.
     * @return Course details including syllabus, schedule, and teaching staff.
     */
    @GET("course_detail")
    suspend fun getCourseDetail(
        @Query("activityCode") activityCode: String,
        @Query("courseCode") courseCode: String
    ): Response<BicoccappCourseDetailResponse>

    /**
     * Adds a course's lecture schedule to the user's calendar.
     *
     * @param userId User identifier.
     * @param cdsCode Degree program code (Corso di Studi).
     * @param activityCode Specific course instance identifier.
     * @param lessonName Course display name for calendar events.
     * @param courseCode General course code.
     * @param partition Optional section identifier for partitioned courses.
     * @return Operation result with number of events added.
     */
    @FormUrlEncoded
    @POST("set_calendar")
    suspend fun addCourseToCalendar(
        @Field("user_id") userId: String,
        @Field("cdsCode") cdsCode: String,
        @Field("activityCode") activityCode: String,
        @Field("lessonName") lessonName: String,
        @Field("courseCode") courseCode: String,
        @Field("partition") partition: String? = null
    ): Response<BicoccappSetCalendarResponse>
}
