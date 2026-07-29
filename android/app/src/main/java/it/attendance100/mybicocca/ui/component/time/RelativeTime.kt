package it.attendance100.mybicocca.ui.component.time

import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.core.text.UiText
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Compact Italian relative-time label, tiering with distance from [now]:
 * "adesso" → "x min fa" → "x ore fa" → "ieri" → "x giorni fa" → "12 nov" → "12 nov 2024"
 * (the year appears only once the instant falls outside the current year).
 */
fun relativeTimeLabel(
    instant: Instant,
    now: Instant = Instant.now(),
    locale: Locale = Locale.getDefault(),
): UiText {
    val zone = ZoneId.systemDefault()
    val elapsed = Duration.between(instant, now)
    val minutes = elapsed.toMinutes()
    val hours = elapsed.toHours()
    val calendarDays =
        ChronoUnit.DAYS.between(instant.atZone(zone).toLocalDate(), now.atZone(zone).toLocalDate())
    return when {
        minutes < 1 -> UiText.StringResource(R.string.relative_time_now)
        minutes < 60 -> UiText.StringResource(R.string.relative_time_minutes_ago, minutes.toInt())
        hours < 24 -> if (hours == 1L) UiText.StringResource(R.string.relative_time_one_hour_ago) else UiText.StringResource(
            R.string.relative_time_hours_ago,
            hours.toInt()
        )

        calendarDays == 1L -> UiText.StringResource(R.string.relative_time_yesterday)
        calendarDays in 2..6 -> UiText.StringResource(
            R.string.relative_time_days_ago,
            calendarDays.toInt()
        )

        instant.atZone(zone).year == now.atZone(zone).year -> UiText.DynamicString(
            DateTimeFormatter.ofPattern("d MMM", locale).withZone(zone).format(instant)
        )

        else -> UiText.DynamicString(
            DateTimeFormatter.ofPattern("d MMM yyyy", locale).withZone(zone).format(instant)
        )
    }
}
