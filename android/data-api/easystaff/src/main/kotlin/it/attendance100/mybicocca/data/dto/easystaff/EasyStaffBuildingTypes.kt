package it.attendance100.mybicocca.data.dto.easystaff

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a building/location in the university campus.
 *
 * @property code The building code (e.g., "U01", "U06")
 * @property name The building name (e.g., "ATLAS ex U1", "AGORA' ex U6")
 */
@Serializable
data class EasyStaffBuilding(
    @SerialName("valore")
    val code: String,
    @SerialName("label")
    val name: String
) {
    override fun toString(): String = name

    companion object {
        /**
         * Special value representing all buildings.
         */
        val FILTER_ALLOW_ALL = EasyStaffBuilding("all", "Tutte le sedi")

        /**
         * Special value representing events without a room.
         */
        val FILTER_ALLOW_NO_ROOM = EasyStaffBuilding("senza_aula", "Senza aula")
    }
}

/**
 * Represents a room within a building.
 *
 * @property code The room code (e.g., "U6-22")
 * @property name The room name (e.g., "U6-22 con Podio")
 * @property capacity The seating capacity of the room, or null if unknown
 */
@Serializable
data class EasyStaffRoom(
    @SerialName("valore")
    val code: String,

    @SerialName("label")
    val name: String,

    @SerialName("capacity")
    val capacity: Int? = null
) {
    companion object {
        /**
         * Special value representing all rooms.
         */
        val FILTER_ALLOW_ALL = EasyStaffRoom("all", "Tutte le aule", null)
    }
}

/**
 * Extended room information from the room showcase (vetrina aule).
 *
 * @property code The room code
 * @property name The room name
 * @property building The building
 * @property capacity The seating capacity
 * @property examCapacity The exam seating capacity (may differ from regular)
 * @property floor The floor number/name
 * @property isAccessible Whether the room is wheelchair accessible
 * @property hasVideo Whether the room has video equipment
 * @property hasMicrophone Whether the room has a microphone system
 * @property hasProjector Whether the room has a projector
 * @property hasComputer Whether the room has a computer/podium
 * @property hasWhiteboard Whether the room has a whiteboard
 * @property hasBlackboard Whether the room has a blackboard
 * @property otherEquipment List of other available equipment
 * @property notes Additional notes about the room
 * @property imageUrl URL to the room image if available
 * @property mapsUrl URL to Google Maps location
 */
@Serializable
data class EasyStaffRoomDetails(
    @SerialName("code")
    val code: String,

    @SerialName("name")
    val name: String,

    @SerialName("building")
    val building: EasyStaffBuilding,

    @SerialName("capacity")
    val capacity: Int?,

    @SerialName("examCapacity")
    val examCapacity: Int?,

    @SerialName("floor")
    val floor: String?,

    @SerialName("isAccessible")
    val isAccessible: Boolean,

    @SerialName("hasVideo")
    val hasVideo: Boolean,

    @SerialName("hasMicrophone")
    val hasMicrophone: Boolean,

    @SerialName("hasProjector")
    val hasProjector: Boolean,

    @SerialName("hasComputer")
    val hasComputer: Boolean,

    @SerialName("hasWhiteboard")
    val hasWhiteboard: Boolean,

    @SerialName("hasBlackboard")
    val hasBlackboard: Boolean,

    @SerialName("otherEquipment")
    val otherEquipment: List<String> = emptyList(),

    @SerialName("notes")
    val notes: String?,

    @SerialName("imageUrl")
    val imageUrl: String?,

    @SerialName("mapsUrl")
    val mapsUrl: String?
)

/**
 * Response wrapper for room occupation API (rooms_call.php).
 */
@Serializable
internal data class EasyStaffRoomOccupationResponse(
    @SerialName("events")
    val events: List<EasyStaffRoomOccupationEvent> = emptyList(),
)

/**
 * A room occupation event from rooms_call.php.
 *
 * This is a specialized event type for the room occupation API which has a different
 * structure than the general events API (bookings_call.php).
 *
 * Key differences from [EasyStaffEvent]:
 * - `eventType` is a String (the type label) instead of [EasyStaffEventType]
 * - Does not have `isPast` field
 * - Does not have `activities` field
 *
 * @property id The event ID
 * @property title The event title
 * @property date The date of the event
 * @property startDateTime The start time as LocalDateTime
 * @property endDateTime The end time as LocalDateTime
 * @property roomName The room where the event takes place
 * @property roomCode The room code
 * @property buildingName The building name
 * @property buildingCode The building code
 * @property facultyId The faculty ID
 * @property status The booking status (confirmed/cancelled)
 * @property eventType The type of event as a string label
 * @property isUniversityEvent Whether this is a university-wide event
 * @property teachersList List of teachers for this event
 */
@Serializable
data class EasyStaffRoomOccupationEvent(
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

    @SerialName("CodiceAula")
    val roomCode: String,

    @SerialName("NomeSede")
    val buildingName: String,

    @SerialName("CodiceSede")
    val buildingCode: String,

    @SerialName("faculty")
    val facultyId: String,

    @SerialName("Annullato")
    val status: EasyStaffBookingStatus,

    @SerialName("type")
    val eventType: String,

    @SerialName("EventoAteneo")
    @Serializable(with = IntBooleanSerializer::class)
    val isUniversityEvent: Boolean,

    @SerialName("Utenti")
    val teachersList: List<EasyStaffEventTeacher> = emptyList()
)
