package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.runtime.Immutable
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import java.time.LocalDateTime

enum class EventStatus {
    CANCELLED,
    ENDED,
    IN_PROGRESS,
    UPCOMING
}

enum class EmptyStateType {
    DAY, WEEK, MONTH, SEARCH
}

@Immutable
data class BuildingWithRooms(
    val buildingName: String,
    val rooms: List<String>,
)

@Immutable
data class OverlapGroup(
    val events: List<CalendarEvent>,
    val groupStartMinutes: Int,
    val groupEndMinutes: Int,
) {
    val visibleEvents: List<CalendarEvent> by lazy {
        if (events.size > 2) listOf(events.first()) else events
    }

    companion object {
        fun create(
            events: List<CalendarEvent>,
            groupStartMinutes: Int,
            groupEndMinutes: Int,
        ) = OverlapGroup(events, groupStartMinutes, groupEndMinutes)
    }
}

fun CalendarEvent.resolveStatus(now: LocalDateTime = LocalDateTime.now()): EventStatus = when {
    now.isAfter(endDateTime) -> EventStatus.ENDED
    now.isAfter(startDateTime) && now.isBefore(endDateTime) -> EventStatus.IN_PROGRESS
    else -> EventStatus.UPCOMING
}
