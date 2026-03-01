package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Response wrapper for the user's calendar.
 */
@Serializable
internal data class BicoccappCalendarResponse(
    /**
     * List of calendar days containing events.
     */
    @SerialName("calendar")
    val days: List<BicoccappCalendarDay> = emptyList()
)

/**
 * Represents a single day in the calendar.
 */
@Serializable
data class BicoccappCalendarDay(
    /**
     * The date of this calendar entry
     */
    @SerialName("day")
    @Serializable(with = BicoccappLocalDateSerializer::class)
    val date: LocalDate,

    /**
     * List of course events/lessons for this day.
     */
    @SerialName("events")
    val events: List<BicoccappCourseEvent> = emptyList(),

    /**
     * List of exam appeals for this day.
     */
    @SerialName("appeals")
    val appeals: List<BicoccappCourseAppeal> = emptyList()
)

/**
 * Represents a specific course event or lesson.
 */
@Serializable
data class BicoccappCourseEvent(
    /**
     * Unique identifier for the event.
     */
    @SerialName("eventId")
    val eventId: String,

    /**
     * Name of the course.
     */
    @SerialName("courseName")
    val courseName: String,

    /**
     * Code identifying the course.
     */
    @SerialName("courseCode")
    val courseCode: String,

    /**
     * Partition code if the course is split (e.g., A-L, M-Z).
     */
    @SerialName("partition")
    val partition: String,

    /**
     * Date of the event.
     */
    @SerialName("date")
    @Serializable(with = BicoccappLocalDateSerializer::class)
    val date: LocalDate,

    /**
     * Time of the event.
     */
    @SerialName("time")
    val time: String,

    /**
     * Type of event (e.g., LEZ for lesson).
     */
    @SerialName("type")
    val type: String,

    /**
     * Code of the room where the event takes place.
     */
    @SerialName("roomCode")
    val roomCode: String,

    /**
     * Description or name of the room.
     */
    @SerialName("room")
    val room: String,

    /**
     * Status indicating if the event is canceled.
     */
    @SerialName("canceled")
    @Serializable(with = BioccappStringBooleanSerializer::class)
    val cancellationStatus: Boolean,

    /**
     * Geographic coordinates of the event location.
     */
    @SerialName("coordinates")
    @Serializable(with = BicoccappEventCoordinatesSerializer::class)
    val coordinates: BicoccappEventCoordinates? = null,

    /**
     * List of teachers associated with this event.
     */
    @SerialName("teachers")
    @Serializable(with = BicoccappTeachersSerializer::class)
    val teachers: List<BicoccappTeacher> = emptyList(),

    /**
     * Activity code associated with the event.
     */
    @SerialName("activityCode")
    val activityCode: String? = null,

    /**
     * Indicates if the session is booked.
     */
    @SerialName("session_booked")
    val isSessionBooked: Boolean
)

/**
 * Geographic coordinates (Latitude and Longitude).
 */
@Serializable
data class BicoccappEventCoordinates(
    /**
     * Latitude value as a string.
     */
    @SerialName("latitude")
    val latitude: Double,

    /**
     * Longitude value as a string.
     */
    @SerialName("longitude")
    val longitude: Double
)

/**
 * Represents an exam appeal (appello).
 */
@Serializable
data class BicoccappCourseAppeal(
    /**
     * ID of the degree course (Corso di Studi).
     */
    @SerialName("cdsId")
    val degreeCourseId: Int,

    /**
     * Type of appeal.
     */
    @SerialName("type")
    val type: String,

    /**
     * ID of the teaching activity.
     */
    @SerialName("activityId")
    val activityId: Int,

    /**
     * Unique ID of the appeal.
     */
    @SerialName("activityAppealId")
    val activityAppealId: Int,

    /**
     * Item ID of the activity.
     */
    @SerialName("activityItemId")
    val activityItemId: Int,

    /**
     * ID of the student.
     */
    @SerialName("studentId")
    val studentId: Int,

    /**
     * Date of the appeal.
     */
    @SerialName("appealDate")
    @Serializable(with = BicoccappLocalDateTimeSerializer::class)
    val appealDate: LocalDateTime,

    /**
     * Description of the appeal.
     */
    @SerialName("appealDescr")
    val appealDescription: String,

    /**
     * Description of the course.
     */
    @SerialName("courseDescr")
    val courseDescription: String,

    /**
     * Description of the session.
     */
    @SerialName("sessionDescr")
    val sessionDescription: String,

    /**
     * Code for the type of appeal.
     */
    @SerialName("typeAppealCode")
    val typeAppealCode: String,

    /**
     * Position or queue number.
     */
    @SerialName("position")
    val position: Int,

    /**
     * Indicates if the session is booked.
     */
    @SerialName("session_booked")
    val isSessionBooked: Boolean
)

/**
 * Response containing a list of courses available for calendar subscription.
 */
@Serializable
internal data class BicoccappCalendarCoursesResponse(
    /**
     * List of available courses.
     */
    @SerialName("courses")
    val courses: List<BicoccappCourse> = emptyList()
)

/**
 * Represents a course available for subscription.
 */
@Serializable
data class BicoccappCourse(
    /**
     * Code of the degree course (Corso di Studi).
     */
    @SerialName("cdsCode")
    val degreeCourseCode: String,

    /**
     * Activity code for the course.
     */
    @SerialName("activityCode")
    val activityCode: String,

    /**
     * Name of the lesson/course.
     */
    @SerialName("lessonName")
    val lessonName: String,

    /**
     * Partition code (e.g., A-L).
     */
    @SerialName("partition")
    val partition: String,

    /**
     * General course code.
     */
    @SerialName("courseCode")
    val courseCode: String,

    /**
     * Indicates if the course is active.
     */
    @SerialName("is_active")
    val isActive: Boolean,

    /**
     * Indicates if the course is in the student's booklet.
     */
    @SerialName("is_booklet")
    val isBooklet: Boolean,

    /**
     * Enrollment ID (Matricola numeric ID).
     */
    @SerialName("matricId")
    val enrollmentId: Int? = null,

    /**
     * Activity item ID.
     */
    @SerialName("activityItemId")
    val activityItemId: Double? = null
)

/**
 * Detailed information about a specific course.
 */
@Serializable
data class BicoccappCourseDetails(
    /**
     * Activity code.
     */
    @SerialName("activity_code")
    val activityCode: String,

    /**
     * Degree course code.
     */
    @SerialName("cds_code")
    val degreeCourseCode: String,

    /**
     * Name of the lesson.
     */
    @SerialName("lesson_name")
    val lessonName: String,

    /**
     * Partition code.
     */
    @SerialName("partition")
    val partition: String,

    /**
     * Course code.
     */
    @SerialName("course_code")
    val courseCode: String,

    /**
     * List of teachers for this course.
     */
    @SerialName("teachers")
    @Serializable(with = BicoccappTeachersSerializer::class)
    val teachers: List<BicoccappTeacher> = emptyList(),

    /**
     * List of events associated with this course.
     */
    @SerialName("events")
    val events: List<BicoccappCourseEvent> = emptyList()
)

/**
 * Represents the outcome of modifying the calendar.
 * This sealed hierarchy allows for exhaustive handling of the different states
 * returned by the Bicoccapp API. Use a `when` expression to branch logic based
 * on whether the operation was a standard [Success] or an [Error].
 */
@Serializable(with = BicoccappSetCalendarResultSerializer::class)
sealed interface BicoccappSetCalendarResult {
    /**
     * Indicates the operation completed successfully
     */
    @Serializable
    object Success : BicoccappSetCalendarResult

    /**
     * Indicates the operation failed.
     *
     * @property message A human-readable message describing the failure.
     */
    @Serializable
    data class Error(
        val message: String,
    ) : BicoccappSetCalendarResult
}


