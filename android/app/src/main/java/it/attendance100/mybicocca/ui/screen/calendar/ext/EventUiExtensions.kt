package it.attendance100.mybicocca.ui.screen.calendar.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.screen.calendar.theme.EventColorVariant
import it.attendance100.mybicocca.ui.screen.calendar.theme.EventPalette
import it.attendance100.mybicocca.ui.screen.calendar.theme.LocalEventPalette
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * True when the event occupies a single instant, not an interval — deadlines are the case
 * in point. Cards and the detail sheet render one time for such events, never a
 * fabricated range.
 */
val CalendarEvent.isPointInTime: Boolean
    get() = start == end

/** True while [now] falls within the event's span on the event's own date. */
fun CalendarEvent.isInProgress(now: LocalDateTime): Boolean {
    if (now.toLocalDate() != date) return false
    val t = now.toLocalTime()
    return !t.isBefore(start) && t.isBefore(end)
}

/** Scheduled length in whole minutes, floored at zero. */
fun CalendarEvent.durationMinutes(): Int {
    val s = start.hour * 60 + start.minute
    val e = end.hour * 60 + end.minute
    return (e - s).coerceAtLeast(0)
}

/**
 * Building and room joined as "U6 · U6-15", a single part when the other is missing, and
 * the empty string when the event carries no location.
 */
fun CalendarEvent.locationLine(): String {
    val loc = location ?: return ""
    return listOfNotNull(loc.building?.takeIf { it.isNotBlank() }, loc.room?.takeIf { it.isNotBlank() })
        .joinToString(" · ")
}

/** Comma-separated teacher names for lessons; empty for kinds that carry no people. */
fun CalendarEvent.peopleLine(): String = when (this) {
    is CalendarEvent.Lesson -> teachers.joinToString(", ")
    else -> ""
}

/** Dense-cell text: the precomputed short label when usable, the first four title characters uppercased as fallback. */
fun CalendarEvent.compactLabel(): String =
    shortLabel?.takeIf { it.isNotBlank() } ?: title.take(4).uppercase()

/** Hash key into the lesson color pool: the normalized title, so recurring lessons of one course share a swatch; the id when blank. */
private fun CalendarEvent.colorVariantKey(): String =
    title.trim().lowercase().ifBlank { id.value }

/** Deterministic pool pick: hashes [key] onto [EventPalette.variants], so equal keys always land on the same swatch. */
fun EventPalette.colorVariantFor(key: String): EventColorVariant {
    val idx = (key.hashCode() and 0x7FFFFFFF) % variants.size
    return variants[idx]
}

/**
 * Resolves the event's swatch for the given clock time. In-progress events take the
 * dedicated in-progress variant; otherwise lessons rotate through the hash pool for
 * variety while every other kind keeps its fixed identity color, recognizable across the
 * whole calendar.
 */
@Composable
@ReadOnlyComposable
fun CalendarEvent.colorsFor(now: LocalDateTime): EventColorVariant {
    val palette = LocalEventPalette.current
    if (isInProgress(now)) return palette.inProgress
    return when (this) {
        is CalendarEvent.Lesson -> palette.colorVariantFor(colorVariantKey())
        is CalendarEvent.Exam -> palette.exam
        is CalendarEvent.AssignmentDeadline -> palette.deadline
        is CalendarEvent.Appointment -> palette.appointment
        is CalendarEvent.LibraryReservation -> palette.library
    }
}

/** "HH:MM–HH:MM", collapsed to a single "HH:MM" for point-in-time events such as deadlines. */
fun CalendarEvent.formatTimeRange(): String =
    if (isPointInTime) start.formatHm()
    else "%s–%s".format(start.formatHm(), end.formatHm())

private fun LocalTime.formatHm(): String = "%02d:%02d".format(hour, minute)
