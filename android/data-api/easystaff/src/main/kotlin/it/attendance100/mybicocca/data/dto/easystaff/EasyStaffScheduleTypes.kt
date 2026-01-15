package it.attendance100.mybicocca.data.dto.easystaff

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Represents a subject/course (insegnamento) that can be scheduled.
 *
 * @property id The subject's internal ID (ex. 516278)
 * @property code The subject code (ex. EC510731)
 * @property name The subject name (ex. METODI QUALITATIVI PER LA RICERCA DIGITALE)
 * @property teacherName The primary teacher's name (ex. M. LUCCHINI)
 * @property periodId The teaching period ID (ex. 1444)
 */
@Serializable
data class EasyStaffSubject(
    @SerialName("id")
    val id: String,

    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val name: String,

    @SerialName("docente")
    val teacherName: String,

    @SerialName("id_periodo")
    val periodId: String
)

/**
 * Response wrapper for schedule API (grid_call.php).
 *
 * Note: The API returns data in the "celle" array (Italian for "cells"),
 * representing schedule grid cells.
 */
@Serializable
internal data class EasyStaffScheduleResponse(
    @SerialName("celle")
    val cells: List<EasyStaffScheduleCell> = emptyList()
)

/**
 * A schedule cell from the grid_call.php API.
 *
 * This represents a single lesson/event in the weekly schedule grid.
 * The structure is different from [EasyStaffEvent] (bookings_call.php) and
 * [EasyStaffRoomOccupationEvent] (rooms_call.php).
 *
 * @property id The cell/event ID
 * @property dateTime The start time as LocalDateTime (parsed from Unix timestamp)
 * @property date The date of the lesson
 * @property startTime The lesson start time
 * @property endTime The lesson end time
 * @property roomCode The room code (e.g., "U24-DISCO-C2")
 * @property buildingCode The building code (e.g., "U24")
 * @property roomName The room name with building (e.g., "U24-DISCO-AulaC2 con Podio [ZIFERA ex U24]")
 * @property showRoom Whether to display room information
 * @property eventType The type of event (e.g., "Lezione")
 * @property eventTypeCode The event type code (e.g., "LEZ", may be empty)
 * @property status The booking status (confirmed/cancelled)
 * @property subjectCode The subject/course code
 * @property subjectName The subject/course name
 * @property teacherCodes List of teacher codes (parsed from comma-separated string)
 * @property teacherNames List of teacher names (parsed from comma-separated string)
 * @property teacherEmails List of teacher email addresses (parsed from comma-separated string)
 * @property teacherPhones List of teacher phone numbers (parsed from comma-separated string)
 * @property curriculumPath The curriculum/study path description
 * @property subjectTypes List of subject type labels (e.g., ["Obbligatorio"], ["Consigliato"])
 * @property mapsUrl Google Maps embed URL extracted from iframe HTML (null if not available)
 * @property displayFields List of field names to display in the UI
 * @property isHighlighted Whether this cell is highlighted in the grid (parsed from int 0/1)
 * @property isExternal Whether this event comes from an external source (parsed from int 0/1)
 */
@Serializable
data class EasyStaffScheduleCell(
    @SerialName("id")
    val id: String,

    @SerialName("timestamp")
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateTime: LocalDateTime,

    @SerialName("data")
    @Serializable(with = ItalianLocalDateSerializer::class)
    val date: LocalDate,

    @SerialName("ora_inizio")
    @Serializable(with = LocalTimeSerializer::class)
    val startTime: LocalTime,

    @SerialName("ora_fine")
    @Serializable(with = LocalTimeSerializer::class)
    val endTime: LocalTime,

    @SerialName("codice_aula")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val roomCode: String?,

    @SerialName("codice_sede")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val buildingCode: String?,

    @SerialName("aula")
    val roomName: String,

    @SerialName("mostra_aula")
    val showRoom: Boolean = true,

    @SerialName("tipo")
    val eventType: String,

    @SerialName("codice_tipo")
    val eventTypeCode: String = "",

    @SerialName("Annullato")
    val status: EasyStaffBookingStatus,

    @SerialName("codice_insegnamento")
    val subjectCode: String,

    @SerialName("nome_insegnamento")
    val subjectName: String,

    @SerialName("codice_docente")
    @Serializable(with = CommaSeparatedListSerializer::class)
    val teacherCodes: List<String> = emptyList(),

    @SerialName("docente")
    @Serializable(with = CommaSeparatedListSerializer::class)
    val teacherNames: List<String> = emptyList(),

    @SerialName("mail_docente")
    @Serializable(with = CommaSeparatedListSerializer::class)
    val teacherEmails: List<String> = emptyList(),

    @SerialName("tel_docente")
    @Serializable(with = CommaSeparatedListSerializer::class)
    val teacherPhones: List<String> = emptyList(),

    @SerialName("percorso_didattico")
    val curriculumPath: String = "",

    @SerialName("insegnamento_tipo")
    val subjectTypes: List<String> = emptyList(),

    @SerialName("maps")
    @Serializable(with = MapsIframeUrlSerializer::class)
    val mapsUrl: String? = null,

    @SerialName("display")
    val displayFields: List<String> = emptyList(),

    @SerialName("highlighted")
    @Serializable(with = IntBooleanSerializer::class)
    val isHighlighted: Boolean = false,

    @SerialName("from_esterno")
    @Serializable(with = IntBooleanSerializer::class)
    val isExternal: Boolean = false
)

/**
 * Detailed study program information including years and subjects.
 *
 * @property code The program code
 * @property name The program name
 * @property degreeType The degree type text (Italian)
 * @property internalId The internal database ID
 * @property teachingAreaCode The teaching area code
 * @property years The available years of study
 * @property teachingPeriods The available teaching periods
 * @property displayMode The default schedule display mode value
 */
@Serializable
data class EasyStaffStudyProgramDetails(
    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val name: String,

    @SerialName("tipo")
    val degreeType: String,

    @SerialName("cdl_id")
    val internalId: String,

    @SerialName("scuola")
    val teachingAreaCode: String,

    @SerialName("elenco_anni")
    val years: List<EasyStaffYearOfStudyDetails> = emptyList(),

    @SerialName("periodi")
    val teachingPeriods: List<EasyStaffTeachingPeriod> = emptyList(),

    @SerialName("pub_type")
    val displayMode: String
)

/**
 * Detailed year of study including available subjects.
 *
 * @property value The API value (e.g., "GGG|1")
 * @property year The year index (e.g. 1)
 * @property label The display label
 * @property trackName The curriculum/track name
 * @property subjects Subjects available in this year
 */
@Serializable
data class EasyStaffYearOfStudyDetails(
    @SerialName("valore")
    val value: String,

    @SerialName("label")
    val label: String,

    @SerialName("order_lbl")
    val trackName: String,

    @SerialName("elenco_insegnamenti")
    val subjects: List<EasyStaffSubject> = emptyList()
) {
    val year: Int = value.substringAfter("|").toInt()
}

/**
 * Represents a teaching period (semester) within an academic year.
 *
 * @property id The period ID
 * @property code The period code (e.g., "S1", "S2")
 * @property label The display label (e.g., "Primo semestre (S1)")
 */
@Serializable
data class EasyStaffTeachingPeriod(
    @SerialName("id")
    val id: String,

    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val label: String
) {
    override fun toString(): String = label
}