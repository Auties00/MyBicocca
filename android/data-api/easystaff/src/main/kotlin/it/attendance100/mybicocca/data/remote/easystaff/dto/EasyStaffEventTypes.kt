package it.attendance100.mybicocca.data.remote.easystaff.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Type of event
 *
 * @param code the code of the event type
 * @param name the name of the event type
 */
@Serializable
data class EasyStaffEventType(
    @SerialName("valore")
    val code: Int,

    @SerialName("label")
    val name: String,
)

/**
 * Status of a booking/event.
 */
@Serializable
enum class EasyStaffBookingStatus(val value: String) {
    @SerialName("0")
    CONFIRMED("0"),

    @SerialName("1")
    CANCELLED("1")
}

@Serializable
internal data class EasyStaffEventsResponse(
    @SerialName("events")
    val events: List<EasyStaffEvent> = emptyList()
)

/**
 * A scheduled event.
 *
 * @property id The event ID
 * @property title The event title (usually the subject name)
 * @property date The date of the event
 * @property startDateTime The start time as LocalDateTime
 * @property endDateTime The end time as LocalDateTime
 * @property roomName The room where the event takes place
 * @property facultyId The faculty ID
 * @property eventType The type of event
 * @property isPast Whether the event is in the past
 * @property isUniversityEvent Whether this is a university-wide event
 * @property teachersList List of teachers for this event
 * @property activities Educational activities linked to this event
 */
@Serializable
data class EasyStaffEvent(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val title: String,

    @SerialName("Giorno")
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,

    @SerialName("timestamp_from")
    @Serializable(with = LocalDateTimeSerializer::class)
    val startDateTime: LocalDateTime,

    @SerialName("timestamp_to")
    @Serializable(with = LocalDateTimeSerializer::class)
    val endDateTime: LocalDateTime,

    @SerialName("NomeAula")
    val roomName: String,

    @SerialName("faculty")
    val facultyId: String,

    @SerialName("Annullato")
    val status: EasyStaffBookingStatus,

    @SerialName("type_id")
    val eventType: String,

    @SerialName("passato")
    @Serializable(with = StringBooleanSerializer::class)
    val isPast: Boolean,

    @SerialName("EventoAteneo")
    @Serializable(with = IntBooleanSerializer::class)
    val isUniversityEvent: Boolean,

    @SerialName("Utenti")
    val teachersList: List<EasyStaffEventTeacher> = emptyList(),

    @SerialName("attivita_didattiche")
    val activities: List<EasyStaffEventActivity> = emptyList()
)

/**
 * Represents teacher contact and identity information associated with a specific event or lesson.
 * @param name The teacher's first name.
 * @param surname The teacher's last name.
 * @param email The official institutional email address of the teacher.
 * @param code The unique alphanumeric identifier or staff code for the teacher.
 */
@Serializable
data class EasyStaffEventTeacher(
    @SerialName("Nome")
    val name: String,

    @SerialName("Cognome")
    val surname: String,

    @SerialName("Mail")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val email: String?,

    @SerialName("Codice")
    @Serializable(with = EmptyStringAsNullSerializer::class)
    val code: String?
)

/**
 * Represents an educational activity (insegnamento/attività didattica).
 *
 * @property id The activity ID
 * @property code The activity code
 * @property name The activity name
 */
@Serializable
data class EasyStaffEventActivity(
    @SerialName("ID")
    val id: String,

    @SerialName("Codice")
    val code: String,

    @SerialName("Nome")
    val name: String
)