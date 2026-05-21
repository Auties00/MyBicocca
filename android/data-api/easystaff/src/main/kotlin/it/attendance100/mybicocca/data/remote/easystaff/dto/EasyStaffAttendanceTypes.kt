package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload to retrieve the attendance history for a specific student.
 *
 *  @property studentId The student's identification number (matricola)
 */
@Serializable
internal data class EasyStaffGetAttendanceHistoryRequest(
    @SerialName("Matricola")
    val studentId: String
)

@Serializable
enum class EasyStaffAttendanceStatus {
    /**
     * Corresponds to "frequentante" (Green status).
     */
    @SerialName("frequentante")
    ATTENDING,

    /**
     * Corresponds to "in_frequenza" (Yellow status).
     */
    @SerialName("in_frequenza")
    IN_PROGRESS,

    /**
     * Corresponds to "non_frequentante" (Red status).
     */
    @SerialName("non_frequentante")
    NOT_ATTENDING
}

/**
 * Represents the attendance statistics for a specific course or teaching activity.
 *
 * @property courseCode The unique code identifying the course
 * @property courseName The full display name of the course
 * @property attendancePercentage The current percentage of lessons attended (0.0 to 100.0)
 * @property lessonsAttended The total number of individual lessons the student attended
 * @property totalHours The total number of academic hours completed by the professor
 * @property requirementProgressPercentage The progress percentage towards meeting the minimum attendance required for the exam
 * @property state the attendance state
 */
@Serializable
data class EasyStaffAttendanceRecord(
    @SerialName("codice")
    val courseCode: String,

    @SerialName("nome")
    val courseName: String,

    @SerialName("Percentuale")
    val attendancePercentage: Double,

    @SerialName("Frequentate")
    val lessonsAttended: Int,

    @SerialName("OreFatte")
    val totalHours: Int,

    @SerialName("percentuale_conseguimento")
    val requirementProgressPercentage: Double,

    @SerialName("Stato")
    val state: EasyStaffAttendanceStatus
)

/**
 * Domain-level representation of a student's full attendance history.
 *
 * @property title The title/header of the history view
 * @property records The list of attendance statistics for each course
 */
@Serializable
data class EasyStaffCourseAttendanceHistory(
    @SerialName("title")
    val title: String,

    @SerialName("records")
    val records: List<EasyStaffAttendanceRecord> = emptyList()
)

/**
 * Request payload to certify a student's presence at a specific lesson.
 *
 * @property studentId The student's identification number (matricola)
 * @property lessonCode The unique identifier for the lesson
 * @property deviceToken A token identifying the mobile device used for certification
 * @property seat The seat identifier or label occupied by the student
 * @property language The language code of the user (e.g., "it", "en")
 * @property isAuthenticated Whether the user's identity has been verified
 * @property action The specific certification action (e.g., "certify")
 * @property additionalData Geolocation and timing metadata for verification
 */
@Serializable
internal data class EasyStaffCertifyAttendanceRequest(
    @SerialName("matricola_studente")
    val studentId: String,

    @SerialName("codice_lezione")
    val lessonCode: String,

    @SerialName("device_token")
    val deviceToken: String,

    @SerialName("posto")
    val seat: String,

    @SerialName("lingua")
    val language: String,

    @SerialName("autenticazione")
    val isAuthenticated: Boolean,

    @SerialName("action")
    val action: String,

    @SerialName("dati_addizionali")
    val additionalData: EasyStaffCertifyAttendanceAdditionalData
)

/**
 * Metadata for attendance certification, used for location-based validation.
 *
 * @property timestamp The epoch timestamp of the request
 * @property longitude The GPS longitude coordinate
 * @property latitude The GPS latitude coordinate
 */
@Serializable
internal data class EasyStaffCertifyAttendanceAdditionalData(
    @SerialName("timestamp")
    val timestamp: Long,

    @SerialName("longitudine")
    val longitude: Double,

    @SerialName("latitudine")
    val latitude: Double
)

/**
 * Response received after an attendance certification attempt.
 *
 * @property success Whether the certification was successful
 * @property message A descriptive message from the server (often displayed to the user)
 */
@Serializable
data class EasyStaffCertifyAttendanceResult(
    @SerialName("result")
    @Serializable(with = ResponseResultSerializer::class)
    val success: Boolean,

    @SerialName("message")
    val message: String
)