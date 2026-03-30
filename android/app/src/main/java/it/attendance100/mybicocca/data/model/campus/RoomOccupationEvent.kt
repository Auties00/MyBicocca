package it.attendance100.mybicocca.data.model.campus

import java.time.LocalDateTime

data class RoomOccupationEvent(
    val id: String,
    val title: String,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val roomName: String,
    val roomCode: String,
    val buildingName: String,
    val buildingCode: String,
    val eventType: String,
    val teachers: List<String>,
)
