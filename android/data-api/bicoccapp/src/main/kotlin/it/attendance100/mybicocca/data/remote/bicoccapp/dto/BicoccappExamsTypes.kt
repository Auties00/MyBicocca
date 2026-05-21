package it.attendance100.mybicocca.data.remote.bicoccapp.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Response containing user exams career data.
 */
@Serializable
internal data class BicoccappUserExamsResponse(
    /**
     * Career object containing exams and notations.
     */
    @SerialName("career")
    val career: BicoccappUserExamsCareer
)

/**
 * Detailed exams career information.
 */
@Serializable
data class BicoccappUserExamsCareer(
    /**
     * List of career notations.
     */
    @SerialName("notations")
    val notations: List<BicoccappUserExamsCareerNotation> = emptyList(),

    /**
     * List of completed exams.
     */
    @SerialName("exams")
    val exams: List<BicoccappUserExamsCareerEntry> = emptyList(),

    /**
     * List of remaining/pending exams.
     */
    @SerialName("remainings")
    val remainingExams: List<BicoccappUserExamsCareerEntry> = emptyList()
)

/**
 * Represents a career notation (e.g., year, grade).
 */
@Serializable
data class BicoccappUserExamsCareerNotation(
    /**
     * Academic year of the notation.
     */
    @SerialName("year")
    val year: String? = null,

    /**
     * Date of the exam (string format).
     */
    @SerialName("dateExam")
    @Serializable(with = BicoccappLocalDateTimeSerializer::class)
    val examDate: LocalDateTime,

    /**
     * Flag indicating cum laude (lode).
     */
    @SerialName("laudFlag")
    @Serializable(with = BicoccappIntBooleanSerializer::class)
    val cumLaude: Boolean,

    /**
     * Grade value.
     */
    @SerialName("grade")
    val grade: Float
)

/**
 * Represents an entry in the user's exam career (completed or remaining).
 */
@Serializable
data class BicoccappUserExamsCareerEntry(
    /**
     * Year frequency ID.
     */
    @SerialName("yearFreqId")
    val yearFrequencyId: Int,

    /**
     * Activity code.
     */
    @SerialName("activityCode")
    val activityCode: String,

    /**
     * Description of the activity.
     */
    @SerialName("activityDescr")
    val activityDescription: String,

    /**
     * Course year.
     */
    @SerialName("courseYear")
    val courseYear: Int,

    /**
     * Order number.
     */
    @SerialName("order")
    val order: Int,

    /**
     * University Credits (CFU).
     */
    @SerialName("cfu")
    val universityCredits: String,

    /**
     * Description of the status.
     */
    @SerialName("statusDescr")
    val statusDescription: String,

    /**
     * Code for the type of exam.
     */
    @SerialName("typeExamCode")
    val examTypeCode: String,

    /**
     * Description of the exam type.
     */
    @SerialName("typeExamDescr")
    val examTypeDescription: String,

    /**
     * Code for the course type.
     */
    @SerialName("typeCourseCode")
    val courseTypeCode: String? = null,

    /**
     * Description of the course type.
     */
    @SerialName("typeCourseDescr")
    val courseTypeDescription: String? = null,

    /**
     * Status code (e.g., "S" for Superata).
     */
    @SerialName("status")
    val status: String,

    /**
     * Academic year of the exam.
     */
    @SerialName("year")
    val year: String? = null,

    /**
     * Date of the exam.
     */
    @SerialName("dateExam")
    @Serializable(with = BicoccappLocalDateTimeSerializer::class)
    val examDate: LocalDateTime,

    /**
     * Type of value.
     */
    @SerialName("valueType")
    val valueType: String,

    /**
     * Grade received.
     */
    @SerialName("grade")
    val grade: String? = null,

    /**
     * Flag indicating cum laude (lode).
     */
    @SerialName("laudFlag")
    @Serializable(with = BicoccappIntBooleanSerializer::class)
    val cumLaude: Boolean,

    /**
     * Evaluation code.
     */
    @SerialName("evaluationCode")
    val evaluationCode: String? = null,

    /**
     * Evaluation description.
     */
    @SerialName("evaluationDescr")
    val evaluationDescription: String? = null,

    /**
     * Indicates if the entry is editable.
     */
    @SerialName("editable")
    val isEditable: Boolean = false,

    /**
     * ID of the teacher.
     */
    @SerialName("teacherId")
    val teacherId: String? = null,

    /**
     * Code of the teacher.
     */
    @SerialName("teacherCode")
    val teacherCode: String? = null,

    /**
     * Fiscal code of the teacher.
     */
    @SerialName("teacherFiscalCode")
    val teacherFiscalCode: String? = null,

    /**
     * Full name of the teacher.
     */
    @SerialName("teacherFullname")
    val teacherFullName: String? = null,

    /**
     * Email of the teacher.
     */
    @SerialName("teacherEmail")
    val teacherEmail: String? = null,

    /**
     * Key identifier of the teacher.
     */
    @SerialName("teacherKey")
    val teacherKey: String? = null
)

/**
 * Response containing available exam sessions (appeals).
 */
@Serializable
internal data class BicoccappExamsSessionsResponse(
    /**
     * Career object containing courses with exam sessions.
     */
    @SerialName("career")
    val career: BicoccappUserAppealsCareer
)

/**
 * Container for courses with exam sessions.
 */
@Serializable
internal data class BicoccappUserAppealsCareer(
    /**
     * List of courses with available appeals.
     */
    @SerialName("courses")
    val courses: List<BicoccappExamSession> = emptyList()
)

/**
 * Represents a course session with associated appeals.
 */
@Serializable
data class BicoccappExamSession(
    /**
     * Year frequency ID.
     */
    @SerialName("yearFreqId")
    val yearFrequencyId: Int,

    /**
     * Activity code.
     */
    @SerialName("activityCode")
    val activityCode: String,

    /**
     * Activity item ID.
     */
    @SerialName("activityItemId")
    val activityItemId: Int,

    /**
     * Description of the activity.
     */
    @SerialName("activityDescription")
    val activityDescription: String? = null,

    /**
     * Course year.
     */
    @SerialName("courseYear")
    val courseYear: Int,

    /**
     * Activity ID.
     */
    @SerialName("activityId")
    val activityId: Int,

    /**
     * Original activity ID.
     */
    @SerialName("activityId_o")
    val activityIdOriginal: Int,

    /**
     * Degree course ID (CDS).
     */
    @SerialName("cdsId")
    val degreeCourseId: Int,

    /**
     * Original degree course ID (CDS).
     */
    @SerialName("cdsId_o")
    val degreeCourseIdOriginal: Int,

    /**
     * List of appeals for this session.
     */
    @SerialName("appeals")
    val appeals: List<BicoccappAppealSession> = emptyList()
)

/**
 * Represents a specific appeal/exam session.
 */
@Serializable
data class BicoccappAppealSession(
    /**
     * Appeal ID.
     */
    @SerialName("appealId")
    val appealId: Int,

    /**
     * Activity appeal ID.
     */
    @SerialName("activityAppealId")
    val activityAppealId: Int,

    /**
     * Year calendar ID.
     */
    @SerialName("yearCalendaId")
    val yearCalendarId: Int,

    /**
     * Indicates if reserved.
     */
    @SerialName("reserved")
    val isReserved: Boolean,

    /**
     * Indicates if bookable.
     */
    @SerialName("bookable")
    val isBookable: Boolean,

    /**
     * Start date for registration.
     */
    @SerialName("registrationStartDate")
    @Serializable(with = BicoccappLocalDateTimeSerializer::class)
    val registrationStartDate: LocalDateTime,

    /**
     * End date for registration.
     */
    @SerialName("registrationEndDate")
    @Serializable(with = BicoccappLocalDateTimeSerializer::class)
    val registrationEndDate: LocalDateTime,

    /**
     * Date of the appeal.
     */
    @SerialName("appealStartDate")
    @Serializable(with = BicoccappLocalDateTimeSerializer::class)
    val appealStartDate: LocalDateTime,

    /**
     * Description of the appeal.
     */
    @SerialName("appealDescr")
    val appealDescription: String,

    /**
     * Notes.
     */
    @SerialName("note")
    val note: String,

    /**
     * Time of the exam.
     */
    @SerialName("hourExam")
    val examTime: String,

    /**
     * ID of the chairman.
     */
    @SerialName("chairmanId")
    val chairmanId: Int,

    /**
     * Surname of the chairman.
     */
    @SerialName("chairmanSurname")
    val chairmanSurname: String,

    /**
     * Name of the chairman.
     */
    @SerialName("chairmanName")
    val chairmanName: String,
    /**
     * Status of the appeal.
     */
    @SerialName("status")
    val status: String,

    /**
     * Description of the status.
     */
    @SerialName("statusDescr")
    val statusDescription: String,

    /**
     * Code for the appeal exam.
     */
    @SerialName("appealExamCode")
    val appealExamCode: String,

    /**
     * Mode of the appeal exam.
     */
    @SerialName("appealExamMode")
    val appealExamMode: String,

    /**
     * Result of the appeal exam.
     */
    @SerialName("appealExamResult")
    val appealExamResult: String,

    /**
     * Publication status of the appeal.
     */
    @SerialName("appealPublicationStatus")
    val appealPublicationStatus: String,

    /**
     * Registration type of the appeal.
     */
    @SerialName("appealRegistrationType")
    val appealRegistrationType: String
)

/**
 * Represents the outcome of an exam booking operation.
 * This sealed hierarchy allows for exhaustive handling of the different states
 * returned by the Bicoccapp API. Use a `when` expression to branch logic based
 * on whether the operation was a standard [Success] or an [Error].
 */
@Serializable(with = BicoccappExamBookingResultSerializer::class)
sealed interface BicoccappExamBookingResult {
    /**
     * Indicates the booking operation completed successfully without requiring further navigation.
     */
    @Serializable
    object Success : BicoccappExamBookingResult

    /**
     * Indicates the booking operation failed.
     *
     * @property status The error status.
     * @property code A machine-readable error string (e.g., "SESSION_EXPIRED", "EXAM_FULL").
     * @property message A human-readable message describing the failure.
     * @property redirectUrl The destination URL where the user must be sent.
     */
    @Serializable
    data class Error(
        val status: String,
        val code: String,
        val message: String,
        val redirectUrl: String?
    ) : BicoccappExamBookingResult
}
