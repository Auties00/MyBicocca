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

// "adesso" → "x min fa" → "x ore fa" → "ieri" → "x giorni fa" → "12 nov" → "12 nov 2024"
fun relativeTimeLabel(instant: Instant, now: Instant = Instant.now()): String {
    val elapsed = Duration.between(instant, now)
    val minutes = elapsed.toMinutes()
    val hours = elapsed.toHours()
    val days = elapsed.toDays()
    val zone = ZoneId.systemDefault()
    return when {
        minutes < 1 -> "adesso"
        minutes < 60 -> "$minutes min fa"
        hours < 24 -> if (hours == 1L) "1 ora fa" else "$hours ore fa"
        days == 1L -> "ieri"
        days < 7 -> "$days giorni fa"
        instant.atZone(zone).year == now.atZone(zone).year -> DayMonthFmt.format(instant)
        else -> DayMonthYearFmt.format(instant)
    }
}
