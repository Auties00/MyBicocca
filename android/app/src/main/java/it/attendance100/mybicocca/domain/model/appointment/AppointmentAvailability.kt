package it.attendance100.mybicocca.domain.model.appointment

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class AppointmentMonthAvailability(
    // First bookable moment within the queried month, or null when the month is full.
    val firstAvailable: LocalDateTime?,
    val availableDays: List<LocalDate>,
)

data class AppointmentSlot(
    val start: LocalTime,
    val end: LocalTime,
    val available: Boolean,
)
