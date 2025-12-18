package it.attendance100.mybicocca.data.api.bicoccapp

import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarCoursesResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseDetailResponse
import it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappSetCalendarResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * # BicoccApp Calendar API
 *
 * This interface provides endpoints for managing the student's academic calendar,
 * including lecture schedules, course information, and personal calendar customization.
 *
 * ## Features
 *
 * - **Calendar events:** View scheduled lectures, exams, and academic deadlines
 * - **Course catalog:** Browse available courses for calendar subscription
 * - **Course details:** Access detailed information about specific courses
 * - **Calendar management:** Add or remove courses from the personal calendar
 *
 * ## Calendar Sources
 *
 * The calendar aggregates events from multiple sources:
 * - **Lectures:** Scheduled class times from subscribed courses
 * - **Exams:** Registered exam sessions (appelli)
 * - **Deadlines:** Registration deadlines, fee payments, etc.
 * - **University events:** Holidays, closures, ceremonies
 *
 * ## Authentication
 *
 * All endpoints require a valid authentication token.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Fetch calendar for current month
 * val calendar = calendarApi.getCalendar()
 *
 * // Fetch calendar for a specific date
 * val calendar = calendarApi.getCalendar(date = "2024-03-15")
 *
 * // Browse available courses
 * val courses = calendarApi.getAvailableCourses()
 *
 * // Add a course to the calendar
 * calendarApi.addCourseToCalendar(
 *     userId = "user123",
 *     activityCode = "E3101Q",
 *     courseCode = "F0801Q",
 *     lessonName = "Analisi Matematica 1"
 * )
 * ```
 */
interface BicoccappCalendarApi {
    /**
     * Retrieves the academic calendar with scheduled events.
     *
     * Returns a calendar view containing all events relevant to the student,
     * including lectures from subscribed courses, exam sessions, and
     * university-wide events.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `calendar`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarResponse] object contains:
     * - **Events:** List of calendar entries with:
     *   - Event title (course name, exam, etc.)
     *   - Start and end datetime
     *   - Location (building, room)
     *   - Event type (lecture, exam, deadline)
     *   - Associated course information
     *   - Professor name
     * - **Date range:** The time period covered by the response
     * - **Metadata:** Total event count, filters applied
     *
     * ## Date Parameter
     * The `date` parameter controls which events are returned:
     * - When omitted, returns events for the current week/month
     * - When specified, returns events around that date
     *
     * ## Event Types
     * - **LECTURE:** Regular class sessions
     * - **LAB:** Laboratory sessions
     * - **EXAM:** Exam sessions
     * - **DEADLINE:** Administrative deadlines
     * - **HOLIDAY:** University closures
     *
     * @param enrollmentId enrollment number to fetch the calendar for a specific student.
     *
     * @param date Optional date to center the calendar view around.
     *             Format: ISO 8601 date string (e.g., "2024-03-15").
     *             When omitted, defaults to the current date.
     *
     * @return A [retrofit2.Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarResponse] with the scheduled events.
     *
     * @see getAvailableCourses For courses that can be added to the calendar
     * @see addCourseToCalendar For subscribing to course schedules
     */
    @GET("calendar")
    suspend fun getCalendar(
        @Query("matricId") enrollmentId: String,
        @Query("date") date: String? = null
    ): Response<BicoccappCalendarResponse>

    /**
     * Lists all courses available for calendar subscription.
     *
     * Returns a catalog of courses whose lecture schedules can be added
     * to the student's personal calendar. This includes courses from the
     * student's degree program as well as other available courses.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `calendar_courses`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarCoursesResponse] object contains:
     * - **Course list:** Available courses with:
     *   - Course name and code
     *   - Activity code (for specific course instances)
     *   - Department and degree program
     *   - Academic year and semester
     *   - Whether already subscribed
     * - **Categories:** Grouped by department or program
     * - **Search metadata:** For filtering and pagination
     *
     * ## Subscription Status
     * Each course indicates whether the student has already added it
     * to their calendar, allowing the UI to show appropriate actions
     * (add vs. remove).
     *
     * @return A [retrofit2.Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCalendarCoursesResponse] with the course catalog.
     *
     * @see getCourseDetail For detailed information about a specific course
     * @see addCourseToCalendar For adding a course to the calendar
     */
    @GET("calendar_courses")
    suspend fun getAvailableCourses(): Response<BicoccappCalendarCoursesResponse>

    /**
     * Retrieves detailed information about a specific course.
     *
     * Returns comprehensive data about a course including description,
     * syllabus, teaching staff, schedule, and examination details.
     *
     * ## HTTP Details
     * - **Method:** GET
     * - **Path:** `course_detail`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Response Structure
     * The [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseDetailResponse] object contains:
     *
     * ### Basic Information
     * - Course name (Italian and English)
     * - Course code and activity code
     * - Credits (CFU)
     * - Academic year and semester
     * - Department and degree program
     *
     * ### Content
     * - Course description and objectives
     * - Syllabus (program outline)
     * - Prerequisites
     * - Recommended textbooks and materials
     * - Teaching methods
     *
     * ### Schedule
     * - Lecture times and locations
     * - Lab sessions
     * - Office hours
     *
     * ### Teaching Staff
     * - Professor name and contact
     * - Teaching assistants
     * - Tutors
     *
     * ### Assessment
     * - Examination format (written, oral, project)
     * - Grading criteria
     * - Exam dates (appelli)
     *
     * ## Identifier Parameters
     * At least one of `activityCode` or `courseCode` should be provided
     * to identify the course. Using both provides the most specific match.
     *
     * @param activityCode The unique identifier for a specific course instance.
     *                     This identifies a particular offering of a course
     *                     (e.g., "E3101Q" for a specific section).
     *
     * @param courseCode The general course identifier.
     *                   This identifies the course type regardless of section
     *                   (e.g., "F0801Q" for "Computer Science Fundamentals").
     *
     * @return A [retrofit2.Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappCourseDetailResponse] with course information.
     *
     * @see getAvailableCourses For browsing the course catalog
     */
    @GET("course_detail")
    suspend fun getCourseDetail(
        @Query("activityCode") activityCode: String,
        @Query("courseCode") courseCode: String
    ): Response<BicoccappCourseDetailResponse>

    /**
     * Adds a course's lecture schedule to the user's personal calendar.
     *
     * Subscribes the user to receive calendar events for the specified
     * course, including all scheduled lectures, labs, and related sessions.
     *
     * ## HTTP Details
     * - **Method:** POST
     * - **Path:** `set_calendar`
     * - **Content-Type:** `application/x-www-form-urlencoded`
     * - **Authentication:** Required (Bearer token)
     *
     * ## Request Parameters
     * Multiple identifiers are used to uniquely identify the course:
     * - **cdsCode:** Degree program code (Corso di Studi)
     * - **activityCode:** Specific course instance identifier
     * - **courseCode:** General course identifier
     * - **partition:** Section/group for courses with multiple partitions
     *
     * ## Response Structure
     * The [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappSetCalendarResponse] indicates:
     * - Success/failure status
     * - Number of events added
     * - Any conflicts with existing events
     *
     * ## Subscription Behavior
     * - If the course is already subscribed, the request is idempotent
     * - Subscription persists until explicitly removed
     * - Changes to the course schedule are automatically reflected
     *
     * ## Partitions
     * Some courses are divided into partitions (sections) by:
     * - Alphabetical range (A-L, M-Z)
     * - Study group
     * - Lab section
     *
     * The `partition` parameter ensures the correct schedule is added.
     *
     * @param userId The unique identifier of the user.
     *               Should match the authenticated user's ID.
     *
     * @param cdsCode The degree program code (Corso di Studi).
     *                Identifies which degree program the course belongs to.
     *
     * @param activityCode The specific activity/course instance code.
     *                     Uniquely identifies this offering of the course.
     *
     * @param lessonName The display name of the course/lesson.
     *                   Used for event titles in the calendar.
     *
     * @param courseCode The general course code.
     *                   Identifies the course type.
     *
     * @param partition Optional partition/section identifier.
     *                  Required when the course has multiple sections
     *                  with different schedules.
     *
     * @return A [retrofit2.Response] containing [it.attendance100.mybicocca.data.dto.bicoccapp.BicoccappSetCalendarResponse] with the result.
     *
     * @see getCalendar For viewing the updated calendar
     * @see getAvailableCourses For browsing courses to add
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
