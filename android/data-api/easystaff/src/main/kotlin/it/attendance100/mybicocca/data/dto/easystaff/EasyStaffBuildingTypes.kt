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
 * Represents the comprehensive details of a university room as extracted from the
 * EasyStaff/Esse3 system.
 *
 * @param name The name of the building or site where the room is located (e.g., "Sede Centrale").
 * @param address The physical street address of the location.
 * @param googleMapsLink The URL for the Google Maps iframe or external navigation link.
 * @param interactive360Link The URL for the 360-degree interactive panoramic view of the room.
 * @param description A brief textual description of the room's intended use or characteristics.
 * @param capacity The maximum number of students the room can accommodate.
 * @param roomType The category of the room (e.g., "Aula Magna", "Laboratorio Informatica").
 * @param floor The numerical floor level, where 0 represents the ground floor (Piano Terra).
 * @param isAccessible Indicates whether the room is officially marked as accessible for individuals with disabilities.
 * @param equipment A list of available facilities and tools found within the room.
 */
data class EasyStaffRoomDetails(
    val name: String,
    val address: String?,
    val googleMapsLink: String?,
    val interactive360Link: String?,
    val description: String?,
    val capacity: Int?,
    val roomType: String?,
    val floor: Int?,
    val isAccessible: Boolean,
    val equipment: List<Esse3RoomEquipment>
)

/**
 * Represents the various types of equipment and facilities available within a university room
 * as defined in the Esse3 system.
 */
sealed interface Esse3RoomEquipment {

    /**
     * Standard non-movable installations within the room.
     */
    data object FixedEquipment : Esse3RoomEquipment

    /**
     * General portable tools or equipment not categorized elsewhere.
     */
    data object MobileEquipment : Esse3RoomEquipment

    /**
     * The primary desk designated for the instructor or lecturer.
     */
    data object TeacherDesk : Esse3RoomEquipment

    /**
     * Windows equipped with curtains or blinds capable of fully blocking external light.
     */
    data object BlackoutWindows : Esse3RoomEquipment

    /**
     * Stepped or stadium-style floor levels for improved visibility (Gradoni).
     */
    data object TieredSeating : Esse3RoomEquipment

    /**
     * Height-adjustable or specifically designed desks for students with disabilities.
     */
    data object AccessibleTable : Esse3RoomEquipment

    /**
     * A raised platform or stand for a speaker.
     */
    data object Podium : Esse3RoomEquipment

    /**
     * A large pad of paper sheets fixed to the upper edge of a whiteboard or tripod.
     */
    data object FlipChart : Esse3RoomEquipment

    /**
     * A smooth, white surface for non-permanent markers.
     */
    data object Whiteboard : Esse3RoomEquipment

    /**
     * A traditional dark stone writing surface for use with chalk.
     */
    data object SlateBlackboard : Esse3RoomEquipment

    /**
     * A device that projects images from transparent sheets onto a screen or wall.
     */
    data object OverheadProjector : Esse3RoomEquipment

    /**
     * A large writing surface that can be moved vertically via a pulley or sliding system.
     */
    data object SlidingBlackboard : Esse3RoomEquipment

    /**
     * Built-in speakers and amplifiers for voice and media playback.
     */
    data object AudioSystem : Esse3RoomEquipment

    /**
     * A standardized hardware bundle optimized for recording and streaming lectures.
     */
    data object StandardVideoLessonKit : Esse3RoomEquipment

    /**
     * A flat-panel display (LCD or Plasma) for high-definition video output.
     */
    data object LcdPlasmaMonitor : Esse3RoomEquipment

    /**
     * Digital camera hardware for capturing the room or the lecturer.
     */
    data object Camera : Esse3RoomEquipment

    /**
     * A permanent ceiling-mounted projector paired with a retractable projection screen.
     */
    data object CeilingProjectorAndScreen : Esse3RoomEquipment

    /**
     * A dedicated computer terminal located at the teacher's desk.
     */
    data object TeacherPc : Esse3RoomEquipment

    /**
     * High-speed internet access via physical Ethernet ports.
     */
    data object WiredNetwork : Esse3RoomEquipment

    /**
     * Wireless local area network access.
     */
    data object WifiNetwork : Esse3RoomEquipment

    /**
     * Electrical outlets integrated directly into student workstations or desks.
     */
    data object StudentPowerSocket : Esse3RoomEquipment

    /**
     * Dedicated hardware or software setup for capturing audio/video for archival.
     */
    data object RecordingSystem : Esse3RoomEquipment

    /**
     * High-end hardware solutions for professional remote video communication.
     */
    data object VideoConference : Esse3RoomEquipment

    /**
     * Support for browser-based or software-based remote meetings (e.g., Zoom, Teams).
     */
    data object WebConference : Esse3RoomEquipment

    /**
     * Medical or clinical examination tables, typically for health science laboratories.
     */
    data object ExaminationBeds : Esse3RoomEquipment

    /**
     * Non-movable seating permanently attached to a shared long desk.
     */
    data object FixedChairsWithDesks : Esse3RoomEquipment

    /**
     * Non-movable seating featuring an integrated, individual fold-down writing surface.
     */
    data object FixedChairsWithTabletArms : Esse3RoomEquipment

    /**
     * Portable chairs equipped with an integrated fold-down writing surface.
     */
    data object MobileChairsWithTabletArms : Esse3RoomEquipment

    /**
     * Basic portable chairs without any integrated writing surface.
     */
    data object MobileChairsWithoutTabletArms : Esse3RoomEquipment

    /**
     * A custom equipment type for items not covered by the standard enumeration.
     *
     * @property name The descriptive name of the specific equipment.
     */
    data class Other(val name: String) : Esse3RoomEquipment
}

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
