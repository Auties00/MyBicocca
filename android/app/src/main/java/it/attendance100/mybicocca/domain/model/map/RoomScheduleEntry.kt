package it.attendance100.mybicocca.domain.model.map

import java.time.LocalDateTime

/**
 * One confirmed occupation slot of a room, from EasyStaff's daily occupation grid of a building.
 * Fetched live for the day selected in the map tab's building detail; never cached.
 *
 * @property roomCode Code of the occupied room.
 * @property title Event title (course or activity name).
 * @property kind Event type label; null when blank.
 * @property teacher Full name of the first listed teacher; null when none.
 * @property start Slot start, local wall-clock time.
 * @property end Slot end, local wall-clock time.
 */
data class RoomScheduleEntry(
    val roomCode: RoomCode,
    val title: String,
    val kind: String?,
    val teacher: String?,
    val start: LocalDateTime,
    val end: LocalDateTime,
)
