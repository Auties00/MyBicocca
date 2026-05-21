package it.attendance100.mybicocca.ui.screen.calendar.state

import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent

data class LaidOutEvent(
    val event: CalendarEvent,
    // Fractional X offset within the day column [0f..1f).
    val laneStart: Float,
    // Fractional width within the day column (0f..1f].
    val laneWidth: Float,
    val lane: Int,
    val cluster: Int,
    val startMinute: Int,
    val endMinute: Int,
)

data class DayLayout(
    val items: List<LaidOutEvent>,
    val maxLane: Int,
    // Inclusive..exclusive minute window on the y-axis. Auto-fits the events.
    val startMinute: Int,
    val endMinute: Int,
) {
    val totalMinutes: Int get() = (endMinute - startMinute).coerceAtLeast(1)
}
