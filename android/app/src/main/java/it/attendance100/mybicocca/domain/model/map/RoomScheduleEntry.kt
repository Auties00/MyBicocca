package it.attendance100.mybicocca.domain.model.map

import java.time.LocalDateTime

// One confirmed occupation slot of a room, from the building's daily occupation grid.
// Not cached — fetched live for the selected building's current day.
data class RoomScheduleEntry(
    val roomCode: RoomCode,
    val title: String,
    val kind: String?,
    val teacher: String?,
    val start: LocalDateTime,
    val end: LocalDateTime,
)
