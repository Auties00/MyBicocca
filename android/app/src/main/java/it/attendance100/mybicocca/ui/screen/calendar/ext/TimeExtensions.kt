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

// Recomposes every refreshIntervalMs with the current local datetime. Used by carousel
// "X min rimasti", in-progress chips, and the timeline "now" line.
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

// ISO Monday — design's day strip starts on Monday, omits Sunday.
fun weekStartFor(date: LocalDate): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

// Mon..Sun (7 days).
fun visibleWeekDays(weekStart: LocalDate): List<LocalDate> =
    (0L..6L).map { weekStart.plusDays(it) }

// 7-column month grid (Mon..Sun). Returns nullable cells so leading/trailing padding cells
// outside the month appear as nulls — UI renders them as blanks.
fun monthGridCells(yearMonth: YearMonth): List<LocalDate?> {
    val first = yearMonth.atDay(1)
    val firstWeekStart = weekStartFor(first)
    val lastDay = yearMonth.atEndOfMonth()
    return generateSequence(firstWeekStart) { it.plusDays(1) }
        .takeWhile { it <= lastDay }
        .map { if(it.month == first.month) it else null }
        .toList()
}
