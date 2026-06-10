package it.attendance100.mybicocca.core.search

import android.content.Context
import it.attendance100.mybicocca.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * A date recognized inside a search query by [parseDateQuery].
 *
 * @property date The resolved calendar day.
 * @property label Display text ready for the result row title: "Domani", "Lunedì 15
 *   giugno"...
 */
data class DateQueryMatch(
    val date: LocalDate,
    val label: String,
)

/**
 * Recognizes date expressions inside a search query so "domani", "lunedì", "tomorrow",
 * "monday" or "22/06" can land straight on that calendar day. Dictionary + regex only —
 * full natural language parsing is overkill for a search box. Operates on the normalized
 * query, so unaccented forms ("lunedi") match too.
 *
 * Three expression families are recognized, resolved relative to [today]:
 * - relative day words: Italian ("oggi", "domani", "dopodomani") and English ("today", "tomorrow")
 * - weekday names in Italian or English, resolved to the next occurrence with today included
 * - numeric dates, day/month separated by '/' or '.' with an optional 2- or 4-digit year;
 *   2-digit years are 2000-based, and a bare day/month already in the past resolves to
 *   next year's occurrence rather than the elapsed one
 *
 * Display labels are produced in [locale] (defaults to [Locale.getDefault]).
 */
fun parseDateQuery(
    query: String,
    today: LocalDate,
    context: Context,
    locale: Locale = Locale.getDefault(),
): DateQueryMatch? {
    val normalized = normalizeForSearch(query)
    if (normalized.isEmpty()) return null

    relativeDays[normalized]?.let { offset ->
        val date = today.plusDays(offset)
        return DateQueryMatch(date, relativeLabel(offset, context))
    }

    weekdays[normalized]?.let { target ->
        val delta = (target.value - today.dayOfWeek.value + 7) % 7
        val date = today.plusDays(delta.toLong())
        return DateQueryMatch(date, date.weekdayDateLabel(locale))
    }

    NumericDate.matchEntire(normalized)?.let { match ->
        val day = match.groupValues[1].toInt()
        val month = match.groupValues[2].toInt()
        if (day !in 1..31 || month !in 1..12) return null
        val rawYear = match.groupValues[3]
        val year = when {
            rawYear.isEmpty() -> today.year
            rawYear.length == 2 -> 2000 + rawYear.toInt()
            else -> rawYear.toInt()
        }
        val date = runCatching { LocalDate.of(year, month, day) }.getOrNull() ?: return null
        val resolved = if (rawYear.isEmpty() && date.isBefore(today)) date.plusYears(1) else date
        return DateQueryMatch(resolved, resolved.weekdayDateLabel(locale))
    }

    return null
}

private fun LocalDate.weekdayDateLabel(locale: Locale): String {
    val weekday = dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.titlecase(locale) }
    val month = month.getDisplayName(TextStyle.FULL, locale)
    return "$weekday $dayOfMonth $month"
}

private fun relativeLabel(offset: Long, context: Context): String =
    when (offset) {
        0L -> context.getString(R.string.relative_day_today)
        1L -> context.getString(R.string.relative_day_tomorrow)
        else -> context.getString(R.string.relative_day_after_tomorrow)
    }

private val relativeDays = mapOf(
    // Italian
    "oggi" to 0L,
    "domani" to 1L,
    "dopodomani" to 2L,
    // English
    "today" to 0L,
    "tomorrow" to 1L,
)

private val weekdays = mapOf(
    // Italian
    "lunedi" to DayOfWeek.MONDAY,
    "martedi" to DayOfWeek.TUESDAY,
    "mercoledi" to DayOfWeek.WEDNESDAY,
    "giovedi" to DayOfWeek.THURSDAY,
    "venerdi" to DayOfWeek.FRIDAY,
    "sabato" to DayOfWeek.SATURDAY,
    "domenica" to DayOfWeek.SUNDAY,
    // English
    "monday" to DayOfWeek.MONDAY,
    "tuesday" to DayOfWeek.TUESDAY,
    "wednesday" to DayOfWeek.WEDNESDAY,
    "thursday" to DayOfWeek.THURSDAY,
    "friday" to DayOfWeek.FRIDAY,
    "saturday" to DayOfWeek.SATURDAY,
    "sunday" to DayOfWeek.SUNDAY,
)

private val NumericDate = Regex("""(\d{1,2})[/.](\d{1,2})(?:[/.](\d{2}|\d{4}))?""")
