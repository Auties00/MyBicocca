package it.attendance100.mybicocca.domain.model.library

import java.time.LocalDate
import java.time.LocalTime

/**
 * A week of library opening hours from the Affluences timetable, shown in the Biblioteca
 * library detail.
 *
 * @property days One entry per day of the requested week.
 */
data class LibraryWeekHours(
    val days: List<LibraryDayHours>,
)

/**
 * The opening hours of a single day.
 *
 * @property day The calendar date.
 * @property isToday Whether the entry is the current day, as flagged by the server.
 * @property ranges The day's opening ranges; empty when the library is closed that day.
 */
data class LibraryDayHours(
    val day: LocalDate,
    val isToday: Boolean,
    val ranges: List<LibraryOpeningRange>,
)

/** A continuous open interval within a day. */
data class LibraryOpeningRange(
    val open: LocalTime,
    val close: LocalTime,
)
