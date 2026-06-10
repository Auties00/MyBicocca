package it.attendance100.mybicocca.ui.screen.calendar.state

import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent

/**
 * A [CalendarEvent] placed in a day column: horizontal geometry as fractions of the
 * column width, vertical extent as minutes of the day.
 */
data class LaidOutEvent(
    val event: CalendarEvent,
    /** Fractional X offset within the day column, in `[0f..1f)`. */
    val laneStart: Float,
    /** Fractional width within the day column, in `(0f..1f]`. */
    val laneWidth: Float,
    /** Index of the lane the event was assigned within its overlap cluster. */
    val lane: Int,
    /** Identifier of the overlap cluster the event belongs to. */
    val cluster: Int,
    /** Placement start, padded to the minimum readable block, so it can differ from the event's actual start. */
    val startMinute: Int,
    /** Placement end, padded to the minimum readable block, so it can differ from the event's actual end. */
    val endMinute: Int,
)

/**
 * Solved arrangement of one day's events, plus the minute window (inclusive [startMinute],
 * exclusive [endMinute]) the timeline must span vertically — auto-fitted to the events it
 * holds.
 */
data class DayLayout(
    val items: List<LaidOutEvent>,
    val maxLane: Int,
    val startMinute: Int,
    val endMinute: Int,
) {
    val totalMinutes: Int get() = (endMinute - startMinute).coerceAtLeast(1)
}
