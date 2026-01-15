package it.attendance100.mybicocca.data.dto.easystaff

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

/**
 * Represents a scheduled exam session.
 *
 * @property subjectName The name of the subject being examined
 * @property date The date of the exam
 * @property startTime The start time
 * @property endTime The end time
 * @property room The room where the exam takes place
 * @property building The building containing the room
 * @property examiners List of teachers/examiners
 * @property notes Additional information about the exam
 * @property examType The type of exam (written, oral, etc.)
 * @property isCancelled Whether the exam has been cancelled
 * @property eventId The unique event identifier
 */
@Serializable
data class EasyStaffScheduledExam(
    @SerialName("nome")
    val subjectName: String,

    @SerialName("Data")
    @Serializable(with = ItalianLocalDateSerializer::class)
    val date: LocalDate,

    @SerialName("OraInizio")
    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime,

    @SerialName("OraFine")
    @Serializable(with = LocalTimeSerializer::class)
    val endTime: LocalTime,

    @SerialName("Aula")
    val room: String,

    @SerialName("Sede")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val building: String?,

    @SerialName("event_docenti")
    val examiners: List<EasyStaffExamExaminer> = emptyList(),

    @SerialName("event_PublicNotes")
    val notes: String,

    @SerialName("TipoEsame")
    val examType: EasyStaffExamType,

    @SerialName("event_Annullato")
    @Serializable(with = StringBooleanSerializer::class)
    val isCancelled: Boolean,

    @SerialName("event_id")
    val eventId: String
)

/**
 * Represents the contact details of a faculty member acting as an examiner for a session.
 *
 * @param name The examiner's given name.
 * @param lastName The examiner's family name.
 * @param code The university registration number (Matricola) identifying the examiner.
 * @param email The primary institutional email address for exam-related correspondence.
 * @param fallbackEmail A secondary email address, if provided in the system.
 */
@Serializable
data class EasyStaffExamExaminer(
    @SerialName("Nome")
    val name: String,

    @SerialName("Cognome")
    val lastName: String,

    @SerialName("Matricola")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val code: String?,

    @SerialName("Mail1")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val email: String?,

    @SerialName("Mail2")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val fallbackEmail: String? = null
)

/**
 * Subject data from exam API response.
 */
@Serializable
data class EasyStaffExamSubjectData(
    @SerialName("Codice")
    val code: String,

    @SerialName("CodiceGenerale")
    val generalCode: String,

    @SerialName("Nome")
    val name: String
)

/**
 * Subject with its exams from API response.
 */
@Serializable
data class EasyStaffExamSubject(
    @SerialName("DatiInsegnamento")
    val subjectData: EasyStaffExamSubjectData,

    @SerialName("Appelli")
    val exams: List<EasyStaffScheduledExam> = emptyList()
)

/**
 * Response wrapper for exam calendar API (test_call.php).
 */
@Serializable
internal data class EasyStaffExamCalendarResponse(
    @SerialName("Insegnamenti")
    @Serializable(with = ExamSubjectsSerializer::class)
    val subjects: List<EasyStaffExamSubject> = emptyList()
)

/**
 * Type of exam.
 */
@Serializable(with = EasyStaffExamTypeSerializer::class)
sealed interface EasyStaffExamType {
    @Serializable
    data object Written : EasyStaffExamType

    @Serializable
    data object Oral : EasyStaffExamType

    @Serializable
    data object OralAndWritten : EasyStaffExamType

    @Serializable
    data class Other(val value: String) : EasyStaffExamType
}
