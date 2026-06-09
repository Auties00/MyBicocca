package it.attendance100.mybicocca.domain.model.library

import java.time.LocalDate
import java.time.LocalTime

// What a zone accepts for booking on a given reference day: which days are open, which start
// times, and which durations. Sourced from the reservation filters endpoint.
data class LibraryBookingConstraints(
    val openDays: List<LocalDate>,
    val openHours: List<LocalTime>,
    val durationsMinutes: List<Int>,
)
