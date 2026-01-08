package it.attendance100.mybicocca.data.dto.bicoccapp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response containing user career details.
 */
@Serializable
data class BicoccappUserCareerResponse(
    /**
     * Career object containing averages and statistics.
     */
    @SerialName("career")
    val career: BicoccappUserCareerCareer
)

/**
 * Detailed career information.
 */
@Serializable
data class BicoccappUserCareerCareer(
    /**
     * List of grade averages (e.g., weighted, arithmetic).
     */
    @SerialName("averages")
    val averages: List<BicoccappCareerAverage> = emptyList(),

    /**
     * Statistics regarding exams and credits.
     */
    @SerialName("stats")
    val stats: BicoccappCareerStats,
)

/**
 * Represents a type of grade average.
 */
@Serializable
data class BicoccappCareerAverage(
    /**
     * Base value for the average calculation.
     */
    @SerialName("base")
    val base: Int,

    /**
     * Definition or name of the base value.
     */
    @SerialName("basedefinition")
    val definition: String,

    /**
     * Weighted average value.
     */
    @SerialName("weighted")
    val weighted: Double,

    /**
     * Arithmetic average value.
     */
    @SerialName("arithmetic")
    val arithmetic: Double
)

/**
 * Career statistics summary.
 */
@Serializable
data class BicoccappCareerStats(
    /**
     * Status code
     */
    @SerialName("valueCode")
    val statusCode: String,

    /**
     * Description of the status.
     */
    @SerialName("valueDescr")
    val statusDescription: String,

    /**
     * Number of exams completed.
     */
    @SerialName("examsDone")
    val examsDone: Int,

    /**
     * Total credits (CFU) to do.
     */
    @SerialName("totalToDo")
    val totalToDo: Double,

    /**
     * Total credits (CFU) completed.
     */
    @SerialName("totalDone")
    val totalDone: Double
)

/**
 * Response containing user registration history.
 */
@Serializable
data class BicoccappUserRegistrationsResponse(
    /**
     * Career object containing registrations.
     */
    @SerialName("career")
    val career: BicoccappUserRegistrationsCareer
)

/**
 * Container for registration entries.
 */
@Serializable
data class BicoccappUserRegistrationsCareer(
    /**
     * List of career registrations.
     */
    @SerialName("registrations")
    val registrations: List<BicoccappCareerRegistration> = emptyList()
)

/**
 * Represents a specific registration or exam appeal entry in the career.
 */
@Serializable
data class BicoccappCareerRegistration(
    /**
     * Degree course ID.
     */
    @SerialName("cdsId")
    val degreeCourseId: Int,

    /**
     * Activity ID.
     */
    @SerialName("activityId")
    val activityId: Int,

    /**
     * Appeal/Exam session ID.
     */
    @SerialName("activityAppealId")
    val activityAppealId: Int,

    /**
     * Activity Item ID.
     */
    @SerialName("activityItemId")
    val activityItemId: Int,

    /**
     * Student ID.
     */
    @SerialName("studentId")
    val studentId: Int,

    /**
     * Date of the appeal.
     */
    @SerialName("appealDate")
    val appealDate: String,

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
     * Code for the appeal type.
     */
    @SerialName("typeAppealCode")
    val typeAppealCode: String,

    /**
     * Position in the list.
     */
    @SerialName("position")
    val position: Int,

    /**
     * Status of the registration.
     */
    @SerialName("status")
    val status: String
)
