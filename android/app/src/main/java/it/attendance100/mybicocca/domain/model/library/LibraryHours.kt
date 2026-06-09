package it.attendance100.mybicocca.domain.model.library

import java.time.LocalDate
import java.time.LocalTime

data class LibraryWeekHours(
    val days: List<LibraryDayHours>,
)

data class LibraryDayHours(
    val day: LocalDate,
    val isToday: Boolean,
    // Empty when the library is closed that day.
    val ranges: List<LibraryOpeningRange>,
)

data class LibraryOpeningRange(
    val open: LocalTime,
    val close: LocalTime,
)
