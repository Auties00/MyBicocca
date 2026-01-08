package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response wrapper for the user's calendar.
 */
@Serializable
data class BicoccappCalendarResponse(
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
     * The date of this calendar entry (e.g., ISO 8601 format).
     */
    @SerialName("day")
    val date: String,

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
    val date: String,

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
     * Day of the week.
     */
    @SerialName("day")
    val dayOfWeek: String,

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
     * Status indicating if the event is canceled (String value).
     */
    @SerialName("canceled")
    val cancellationStatus: String,

    /**
     * URL or data for the map location.
     */
    @SerialName("maps")
    val mapUrl: String? = null,

    /**
     * Geographic coordinates of the event location.
     */
    @SerialName("coordinates")
    val coordinates: BicoccappEventCoordinates? = null,

    /**
     * List of teachers associated with this event.
     */
    @SerialName("teachers")
    val teachers: List<BicoccappCourseTeacher> = emptyList(),

    /**
     * Activity code associated with the event.
     */
    @SerialName("activityCode")
    val activityCode: String,

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
    val latitude: String? = null,

    /**
     * Longitude value as a string.
     */
    @SerialName("longitude")
    val longitude: String? = null
)

/**
 * Represents a teacher associated with a course.
 */
@Serializable
data class BicoccappCourseTeacher(
    /**
     * Unique key or ID for the teacher.
     */
    @SerialName("teacher_key")
    val key: String,

    /**
     * Code identifying the teacher.
     */
    @SerialName("teacher_code")
    val code: String,

    /**
     * Full name of the teacher.
     */
    @SerialName("teacher_fullname")
    val fullName: String,

    /**
     * Email address of the teacher.
     */
    @SerialName("teacher_email")
    val email: String? = null
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
    val appealDate: String,

    /**
     * Date of the event/session.
     */
    @SerialName("date")
    val date: String,

    /**
     * Time of the appeal.
     */
    @SerialName("time")
    val time: String,

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
     * Status of the appeal (e.g., "ISCRITTO").
     */
    @SerialName("status")
    val status: String,

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
data class BicoccappCalendarCoursesResponse(
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
    val enrollmentId: Double? = null,

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
data class BicoccappCourseDetailResponse(
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
    val teachers: List<BicoccappCourseTeacher> = emptyList(),

    /**
     * List of events associated with this course.
     */
    @SerialName("events")
    val events: List<BicoccappCourseEvent> = emptyList()
)

/**
 * Response after setting/adding a course to the calendar.
 */
@Serializable
data class BicoccappSetCalendarResponse(
    /**
     * Response message.
     */
    @SerialName("message")
    val message: String? = null,

    /**
     * Status code of the operation.
     */
    @SerialName("status")
    val status: Int? = null
)
