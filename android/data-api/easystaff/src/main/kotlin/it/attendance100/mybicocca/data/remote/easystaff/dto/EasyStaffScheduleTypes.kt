package it.attendance100.mybicocca.data.remote.easystaff.dto

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
data class EasyStaffStudyProgramSubject(
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
 * This is a sealed interface representing different types of schedule entries:
 * - [Lesson]: A regular lesson/lecture entry with full details
 * - [Closure]: A closure/holiday entry (e.g., "FESTIVITÀ", "Sospensione delle attività didattiche")
 * - [Other]: An unknown entry type for logging/debugging purposes
 *
 * Implements [Comparable] to allow sorting by date and start time.
 */
@Serializable(with = ScheduleCellSerializer::class)
sealed interface EasyStaffScheduleCell : Comparable<EasyStaffScheduleCell> {
    /**
     * The type discriminator from the API ("tipo" field).
     */
    val type: String

    /**
     * The name/description of this schedule entry.
     */
    val name: String?

    /**
     * The date of this schedule entry.
     */
    val date: LocalDate?

    /**
     * The start time of this schedule entry.
     */
    val startTime: LocalTime?

    /**
     * The end time of this schedule entry.
     */
    val endTime: LocalTime?

    override fun compareTo(other: EasyStaffScheduleCell): Int {
        val dateComparison = compareValues(date, other.date)
        if (dateComparison != 0) return dateComparison
        val startComparison = compareValues(startTime, other.startTime)
        if (startComparison != 0) return startComparison
        return compareValues(endTime, other.endTime)
    }

    /**
     * A regular lesson/lecture entry from the schedule.
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
     * @property status The booking status (confirmed/cancelled)
     * @property subjectCode The subject/course code
     * @property name The subject/course name
     * @property teacherCodes List of teacher codes (parsed from comma-separated string)
     * @property teacherNames List of teacher names (parsed from comma-separated string)
     * @property teacherEmails List of teacher email addresses (parsed from comma-separated string)
     * @property teacherPhones List of teacher phone numbers (parsed from comma-separated string)
     * @property curriculumPath The curriculum/study path description
     * @property subjectTypes List of subject type labels (e.g., ["Obbligatorio"], ["Consigliato"])
     * @property mapsUrl Google Maps embed URL extracted from iframe HTML (null if not available)
     * @property isHighlighted Whether this cell is highlighted in the grid (parsed from int 0/1)
     * @property isExternal Whether this event comes from an external source (parsed from int 0/1)
     */
    @Serializable
    data class Lesson(
        @SerialName("id")
        val id: String,

        @SerialName("timestamp")
        @Serializable(with = LocalDateTimeSerializer::class)
        val dateTime: LocalDateTime,

        @SerialName("data")
        @Serializable(with = ItalianLocalDateSerializer::class)
        override val date: LocalDate,

        @SerialName("ora_inizio")
        @Serializable(with = LocalTimeSerializer::class)
        override val startTime: LocalTime,

        @SerialName("ora_fine")
        @Serializable(with = LocalTimeSerializer::class)
        override val endTime: LocalTime,

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

        @SerialName("Annullato")
        val status: EasyStaffBookingStatus,

        @SerialName("codice_insegnamento")
        val subjectCode: String,

        @SerialName("nome_insegnamento")
        override val name: String,

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

        @SerialName("highlighted")
        @Serializable(with = IntBooleanSerializer::class)
        val isHighlighted: Boolean = false,

        @SerialName("from_esterno")
        @Serializable(with = IntBooleanSerializer::class)
        val isExternal: Boolean = false
    ) : EasyStaffScheduleCell {
        override val type: String get() = TYPE

        companion object {
            const val TYPE = "Lezione"
        }
    }

    /**
     * A closure/holiday entry indicating no lessons are scheduled.
     *
     * @property name The closure description (e.g., "FESTIVITÀ", "Sospensione delle attività didattiche")
     * @property date The date of the closure
     * @property startTime The start time of the closure period
     * @property endTime The end time of the closure period
     */
    @Serializable
    data class Closure(
        @SerialName("nome")
        override val name: String,

        @SerialName("data")
        @Serializable(with = ItalianLocalDateSerializer::class)
        override val date: LocalDate,

        @SerialName("ora_inizio")
        @Serializable(with = LocalTimeSerializer::class)
        override val startTime: LocalTime,

        @SerialName("ora_fine")
        @Serializable(with = LocalTimeSerializer::class)
        override val endTime: LocalTime
    ) : EasyStaffScheduleCell {
        override val type: String get() = TYPE

        companion object {
            const val TYPE = "chiusura_type"
        }
    }

    /**
     * An unknown entry type for logging/debugging purposes.
     *
     * This captures any entry that doesn't match the known types,
     * preserving the raw fields for inspection.
     *
     * @property type The type discriminator value
     * @property rawFields The raw JSON fields as a map for logging
     */
    @Serializable
    data class Other(
        override val type: String,
        val rawFields: Map<String, kotlinx.serialization.json.JsonElement>
    ) : EasyStaffScheduleCell {
        override val name: String? get() = null
        override val date: LocalDate? get() = null
        override val startTime: LocalTime? get() = null
        override val endTime: LocalTime? get() = null
    }
}

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
data class EasyStaffStudyProgram(
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
    val years: List<EasyStaffStudyProgramYear> = emptyList(),

    @SerialName("periodi")
    val teachingPeriods: List<EasyStaffStudyProgramTeachingPeriod> = emptyList(),

    @SerialName("pub_type")
    val displayMode: String
)

/**
 * Detailed year of study including available subjects.
 *
 * @property code The API value (e.g., "GGG|1")
 * @property year The year index (e.g. 1)
 * @property name The display label
 * @property trackName The curriculum/track name
 * @property subjects Subjects available in this year
 */
@Serializable
data class EasyStaffStudyProgramYear(
    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val name: String,

    @SerialName("order_lbl")
    val trackName: String,

    @SerialName("elenco_insegnamenti")
    val subjects: List<EasyStaffStudyProgramSubject> = emptyList()
) {
    val year: Int = code.substringAfter("|").toInt()
}

/**
 * Represents a teaching period (semester) within an academic year.
 *
 * @property id The period ID
 * @property code The period code (e.g., "S1", "S2")
 * @property name The display label (e.g., "Primo semestre (S1)")
 */
@Serializable
data class EasyStaffStudyProgramTeachingPeriod(
    @SerialName("id")
    val id: String,

    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val name: String
) {
    override fun toString(): String = name
}