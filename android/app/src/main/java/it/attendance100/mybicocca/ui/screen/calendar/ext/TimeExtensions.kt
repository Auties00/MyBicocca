package it.attendance100.mybicocca.ui.screen.calendar.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/**
 * Ticking clock: recomposes readers every [refreshIntervalMs] with the current local
 * datetime. Drives whatever must track wall time — in-progress event styling,
 * remaining-time copy and the timeline's now indicator.
 */
@Composable
fun rememberCurrentTime(refreshIntervalMs: Long = 60_000L): State<LocalDateTime> {
    val state = remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(refreshIntervalMs) {
        while (true) {
            state.value = LocalDateTime.now()
            delay(refreshIntervalMs)
        }
    }
    return state
}

/** Monday on or before [date]: the calendar treats weeks as ISO, starting on Monday. */
fun weekStartFor(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

/** The seven days, Monday through Sunday, of the week starting at [weekStart]. */
fun visibleWeekDays(weekStart: LocalDate): List<LocalDate> =
    (0L..6L).map { weekStart.plusDays(it) }

/**
 * Cells of a Monday-first month grid. Days of the leading week that fall before the month
 * come through as nulls the grid renders as blanks; the list stops at the month's last
 * day, so a partial final week is simply shorter.
 */
fun monthGridCells(yearMonth: YearMonth): List<LocalDate?> {
    val first = yearMonth.atDay(1)
    val firstWeekStart = weekStartFor(first)
    val lastDay = yearMonth.atEndOfMonth()
    return generateSequence(firstWeekStart) { it.plusDays(1) }
        .takeWhile { it <= lastDay }
        .map { if(it.month == first.month) it else null }
        .toList()
}
