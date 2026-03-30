package it.attendance100.mybicocca.data.model.campus

import java.time.LocalDate
import java.time.LocalDateTime

data class CampusEvent(
    val id: String,
    val title: String,
    val date: LocalDate,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val roomName: String,
    val eventType: String,
    val isPast: Boolean,
    val teachers: List<String>,
)
