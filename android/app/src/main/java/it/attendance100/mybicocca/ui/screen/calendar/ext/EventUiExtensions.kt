package it.attendance100.mybicocca.ui.screen.calendar.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import it.attendance100.mybicocca.data.mapper.calendar.shortLabelFor
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.ui.screen.calendar.theme.EventColorVariant
import it.attendance100.mybicocca.ui.screen.calendar.theme.EventPalette
import it.attendance100.mybicocca.ui.screen.calendar.theme.LocalEventPalette
import java.time.LocalDateTime
import java.time.LocalTime

enum class EventKind { LECTURE, EXAM }

val CalendarEvent.kind: EventKind
    get() = when (this) {
        is CalendarEvent.Lesson -> EventKind.LECTURE
        is CalendarEvent.Exam -> EventKind.EXAM
    }

fun CalendarEvent.isInProgress(now: LocalDateTime): Boolean {
    if (now.toLocalDate() != date) return false
    val t = now.toLocalTime()
    return !t.isBefore(start) && t.isBefore(end)
}

fun CalendarEvent.progressFraction(now: LocalDateTime): Float {
    if (!isInProgress(now)) return 0f
    val total = (end.toSecondOfDay() - start.toSecondOfDay()).coerceAtLeast(1)
    val elapsed = now.toLocalTime().toSecondOfDay() - start.toSecondOfDay()
    return (elapsed.toFloat() / total).coerceIn(0f, 1f)
}

fun CalendarEvent.minutesRemaining(now: LocalDateTime): Int {
    if (!isInProgress(now)) return 0
    val secs = end.toSecondOfDay() - now.toLocalTime().toSecondOfDay()
    return (secs / 60).coerceAtLeast(0)
}

fun CalendarEvent.durationMinutes(): Int {
    val s = start.hour * 60 + start.minute
    val e = end.hour * 60 + end.minute
    return (e - s).coerceAtLeast(0)
}

// "U6 · U6-15" or just one part if the other is missing. Empty string when no location.
fun CalendarEvent.locationLine(): String {
    val loc = location ?: return ""
    return listOfNotNull(loc.building?.takeIf { it.isNotBlank() }, loc.room?.takeIf { it.isNotBlank() })
        .joinToString(" · ")
}

fun CalendarEvent.peopleLine(): String = when (this) {
    is CalendarEvent.Lesson -> teachers.joinToString(", ")
    is CalendarEvent.Exam -> examiners.joinToString(", ")
}

fun CalendarEvent.compactLabel(): String {
    shortLabel?.takeIf { it.isNotBlank() }?.let { return it }
    return shortLabelFor(title) ?: title.take(4).uppercase()
}

private fun CalendarEvent.colorVariantKey(): String =
    title.trim().lowercase().ifBlank { id.value }

fun EventPalette.colorVariantFor(key: String): EventColorVariant {
    val idx = (key.hashCode() and 0x7FFFFFFF) % variants.size
    return variants[idx]
}

@Composable
@ReadOnlyComposable
fun CalendarEvent.colorsFor(now: LocalDateTime): EventColorVariant {
    val palette = LocalEventPalette.current
    return if (isInProgress(now)) palette.inProgress
    else palette.colorVariantFor(colorVariantKey())
}

// "HH:MM–HH:MM"
fun CalendarEvent.formatTimeRange(): String =
    "%s–%s".format(start.formatHm(), end.formatHm())

private fun LocalTime.formatHm(): String = "%02d:%02d".format(hour, minute)
