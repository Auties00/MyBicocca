package it.attendance100.mybicocca.domain.model.library

import java.time.LocalDate
import java.time.LocalTime

/**
 * What a zone accepts for booking around a given reference day: which days are open, which start
 * times, and which durations. Sourced from the Affluences reservation filters endpoint and used
 * to drive the date/duration/time pickers of the Biblioteca booking flow.
 *
 * @property openDays Bookable dates.
 * @property openHours Allowed start times.
 * @property durationsMinutes Allowed durations, in minutes.
 */
data class LibraryBookingConstraints(
    val openDays: List<LocalDate>,
    val openHours: List<LocalTime>,
    val durationsMinutes: List<Int>,
)
