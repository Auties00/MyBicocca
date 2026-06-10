package it.attendance100.mybicocca.core.search

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
 * Recognizes Italian date expressions inside a search query so "domani", "lunedì" or
 * "22/06" can land straight on that calendar day. Dictionary + regex only — full natural
 * language parsing is overkill for a search box. Operates on the normalized query, so
 * unaccented forms ("lunedi") match too.
 *
 * Three expression families are recognized, resolved relative to [today]:
 * - relative day words: "oggi", "domani", "dopodomani"
 * - weekday names, resolved to the next occurrence with today included — asking for
 *   "lunedì" on a Monday means today
 * - numeric dates, day/month separated by '/' or '.' with an optional 2- or 4-digit year;
 *   2-digit years are 2000-based, and a bare day/month already in the past resolves to
 *   next year's occurrence rather than the elapsed one
 */
fun parseDateQuery(query: String, today: LocalDate): DateQueryMatch? {
    val normalized = normalizeForSearch(query)
    if (normalized.isEmpty()) return null

    relativeDays[normalized]?.let { offset ->
        val date = today.plusDays(offset)
        return DateQueryMatch(date, relativeLabels.getValue(normalized))
    }

    weekdays[normalized]?.let { target ->
        val delta = (target.value - today.dayOfWeek.value + 7) % 7
        val date = today.plusDays(delta.toLong())
        return DateQueryMatch(date, date.weekdayDateLabel())
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
        return DateQueryMatch(resolved, resolved.weekdayDateLabel())
    }

    return null
}

private fun LocalDate.weekdayDateLabel(): String {
    val weekday = dayOfWeek.getDisplayName(TextStyle.FULL, Italian)
        .replaceFirstChar { it.titlecase(Italian) }
    val month = month.getDisplayName(TextStyle.FULL, Italian)
    return "$weekday $dayOfMonth $month"
}

private val Italian = Locale.ITALIAN

private val relativeDays = mapOf(
    "oggi" to 0L,
    "domani" to 1L,
    "dopodomani" to 2L,
)

private val relativeLabels = mapOf(
    "oggi" to "Oggi",
    "domani" to "Domani",
    "dopodomani" to "Dopodomani",
)

private val weekdays = mapOf(
    "lunedi" to DayOfWeek.MONDAY,
    "martedi" to DayOfWeek.TUESDAY,
    "mercoledi" to DayOfWeek.WEDNESDAY,
    "giovedi" to DayOfWeek.THURSDAY,
    "venerdi" to DayOfWeek.FRIDAY,
    "sabato" to DayOfWeek.SATURDAY,
    "domenica" to DayOfWeek.SUNDAY,
)

private val NumericDate = Regex("""(\d{1,2})[/.](\d{1,2})(?:[/.](\d{2}|\d{4}))?""")
