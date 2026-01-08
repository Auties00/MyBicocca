package it.attendance100.mybicocca.data.dto.easystaff

import java.time.LocalDate
import java.time.LocalTime

/**
 * Represents a time slot in the room occupation grid.
 *
 * @property startTime The start time of the slot
 * @property endTime The end time of the slot
 * @property event The event occupying this slot, or null if free
 */
data class EasyStaffRoomTimeSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val event: EasyStaffRoomOccupationEvent?
) {
    /**
     * Whether this time slot is free (no event).
     */
    val isFree: Boolean get() = event == null
}

/**
 * Represents an event occupying a room.
 *
 * @property title The event title
 * @property startTime The start time
 * @property endTime The end time
 * @property eventType The type of event
 * @property organizer The organizer/teacher name
 * @property studyProgram The study program if applicable
 */
data class EasyStaffRoomOccupationEvent(
    val title: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val eventType: EasyStaffEventType,
    val organizer: String?,
    val studyProgram: String?
)

/**
 * Daily occupation schedule for a room.
 *
 * @property room The room
 * @property building The building
 * @property date The date of the schedule
 * @property timeSlots All time slots for the day
 * @property gridStartTime The start time of the occupation grid
 * @property gridEndTime The end time of the occupation grid
 */
data class EasyStaffRoomDailyOccupation(
    val room: EasyStaffRoom,
    val building: EasyStaffBuilding,
    val date: LocalDate,
    val timeSlots: List<EasyStaffRoomTimeSlot>,
    val gridStartTime: LocalTime,
    val gridEndTime: LocalTime
) {
    /**
     * Gets all events for the day.
     */
    val events: List<EasyStaffRoomOccupationEvent>
        get() = timeSlots.mapNotNull { it.event }.distinctBy { it.title to it.startTime }

    /**
     * Checks if the room is free at a specific time.
     *
     * @param time The time to check
     * @return True if the room is free at that time
     */
    fun isFreeAt(time: LocalTime): Boolean {
        return timeSlots.find { time >= it.startTime && time < it.endTime }?.isFree ?: true
    }
}

/**
 * Daily occupation schedule for a building (all rooms).
 *
 * @property building The building
 * @property date The date of the schedule
 * @property rooms Occupation data for each room
 * @property gridStartTime The start time of the occupation grid
 * @property gridEndTime The end time of the occupation grid
 */
data class EasyStaffBuildingDailyOccupation(
    val building: EasyStaffBuilding,
    val date: LocalDate,
    val rooms: List<EasyStaffRoomDailyOccupation>,
    val gridStartTime: LocalTime,
    val gridEndTime: LocalTime
)

/**
 * Parameters for querying room occupation.
 *
 * @property buildingCode The building code
 * @property roomCode The room code (null for all rooms in building)
 * @property date The date to query
 * @property eventTypes Event types to include (empty for all)
 */
data class EasyStaffRoomOccupationQuery(
    val buildingCode: String,
    val roomCode: String? = null,
    val date: LocalDate,
    val eventTypes: List<EasyStaffEventType> = emptyList()
)

/**
 * Options available for room queries, loaded from the server.
 *
 * @property buildings Available buildings
 */
data class EasyStaffRoomSearchOptions(
    val buildings: List<EasyStaffBuilding>
)

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
data class EasyStaffRoomShowcaseDetails(
    val code: String,
    val name: String,
    val building: EasyStaffBuilding,
    val capacity: Int?,
    val examCapacity: Int?,
    val floor: String?,
    val isAccessible: Boolean,
    val hasVideo: Boolean,
    val hasMicrophone: Boolean,
    val hasProjector: Boolean,
    val hasComputer: Boolean,
    val hasWhiteboard: Boolean,
    val hasBlackboard: Boolean,
    val otherEquipment: List<String>,
    val notes: String?,
    val imageUrl: String?,
    val mapsUrl: String?
)

/**
 * Results from a room showcase query.
 *
 * @property rooms The list of rooms with their details
 * @property building The building being queried (if filtering by building)
 */
data class RoomShowcaseResults(
    val rooms: List<EasyStaffRoomShowcaseDetails>,
    val building: EasyStaffBuilding?
)
