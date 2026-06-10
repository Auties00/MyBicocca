package it.attendance100.mybicocca.ui.component.time

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DayMonthFmt = DateTimeFormatter
    .ofPattern("d MMM", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())
private val DayMonthYearFmt = DateTimeFormatter
    .ofPattern("d MMM yyyy", Locale.ITALIAN)
    .withZone(ZoneId.systemDefault())

/**
 * Compact Italian relative-time label, tiering with distance from [now]:
 * "adesso" → "x min fa" → "x ore fa" → "ieri" → "x giorni fa" → "12 nov" → "12 nov 2024"
 * (the year appears only once the instant falls outside the current year).
 */
fun relativeTimeLabel(instant: Instant, now: Instant = Instant.now()): String {
    val elapsed = Duration.between(instant, now)
    val minutes = elapsed.toMinutes()
    val hours = elapsed.toHours()
    val days = elapsed.toDays()
    val zone = ZoneId.systemDefault()
    return when {
        minutes < 1 -> "relative_time_now"
        minutes < 60 -> "relative_time_minutes_ago|$minutes"
        hours < 24 -> if (hours == 1L) "relative_time_one_hour_ago" else "relative_time_hours_ago|$hours"
        days == 1L -> "relative_time_yesterday"
        days < 7 -> "relative_time_days_ago|$days"
        instant.atZone(zone).year == now.atZone(zone).year -> DayMonthFmt.format(instant)
        else -> DayMonthYearFmt.format(instant)
    }
}
