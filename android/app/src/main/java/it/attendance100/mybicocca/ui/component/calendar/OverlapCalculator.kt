package it.attendance100.mybicocca.ui.component.calendar

import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.ui.component.calendar.OverlapGroup
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object OverlapCalculator {

    fun calculateOverlapGroups(
        events: List<CalendarEvent>,
        startHour: Int
    ): List<OverlapGroup> {
        if (events.isEmpty()) return emptyList()

        val sortedEvents = events.sortedBy { it.startDateTime }
        val groups = mutableListOf<OverlapGroup>()

        var currentGroupEvents = mutableListOf<CalendarEvent>()
        var currentGroupEnd = Int.MIN_VALUE

        sortedEvents.forEach { event ->
            val st = event.startDateTime
            val eventStartMinutes = (st.hour - startHour) * 60 + st.minute
            val durationMinutes = ChronoUnit.MINUTES.between(event.startDateTime, event.endDateTime).toInt()
            val eventEndMinutes = eventStartMinutes +
                    maxOf(durationMinutes, CalendarConfig.Dimensions.MIN_EVENT_VISUAL_DURATION_MINUTES)

            if (currentGroupEvents.isEmpty()) {
                currentGroupEvents.add(event)
                currentGroupEnd = eventEndMinutes
            } else if (eventStartMinutes < currentGroupEnd) {
                currentGroupEvents.add(event)
                currentGroupEnd = maxOf(currentGroupEnd, eventEndMinutes)
            } else {
                groups.add(createGroup(currentGroupEvents, startHour, currentGroupEnd))
                currentGroupEvents = mutableListOf(event)
                currentGroupEnd = eventEndMinutes
            }
        }

        if (currentGroupEvents.isNotEmpty()) {
            groups.add(createGroup(currentGroupEvents, startHour, currentGroupEnd))
        }

        return groups
    }

    private fun createGroup(
        events: List<CalendarEvent>,
        startHour: Int,
        groupEnd: Int
    ): OverlapGroup {
        val first = events.first()
        val st = first.startDateTime
        val groupStart = (st.hour - startHour) * 60 + st.minute
        return OverlapGroup.create(events, groupStart, groupEnd)
    }

    fun getEarliestEvent(events: List<CalendarEvent>): CalendarEvent {
        require(events.isNotEmpty()) { "Cannot get earliest event from empty list" }
        return events.minBy { it.startDateTime }
    }

    fun getLatestEndingEvent(events: List<CalendarEvent>): CalendarEvent {
        require(events.isNotEmpty()) { "Cannot get latest event from empty list" }
        return events.maxBy { it.endDateTime }
    }

    fun getGroupStartTime(group: OverlapGroup): LocalTime =
        getEarliestEvent(group.events).startTime ?: LocalTime.MIDNIGHT

    fun getGroupEndTime(group: OverlapGroup): LocalTime =
        getLatestEndingEvent(group.events).endTime ?: LocalTime.MIDNIGHT

    fun formatGroupTimeSpan(group: OverlapGroup): String {
        val start = getGroupStartTime(group).format(CalendarUtils.timeFormatter)
        val end = getGroupEndTime(group).format(CalendarUtils.timeFormatter)
        return "$start - $end"
    }
}
