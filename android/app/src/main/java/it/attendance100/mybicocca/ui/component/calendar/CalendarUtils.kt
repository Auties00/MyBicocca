package it.attendance100.mybicocca.ui.component.calendar

import androidx.compose.ui.graphics.Color
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.model.calendar.CalendarEvent
import it.attendance100.mybicocca.data.model.calendar.EventType
import it.attendance100.mybicocca.data.model.calendar.endDateTime
import it.attendance100.mybicocca.data.model.calendar.startDateTime
import it.attendance100.mybicocca.ui.component.calendar.BuildingWithRooms
import it.attendance100.mybicocca.ui.screen.calendar.LocationFilter
import it.attendance100.mybicocca.ui.screen.calendar.TimeRange
import it.attendance100.mybicocca.ui.theme.EventExamColor
import it.attendance100.mybicocca.ui.theme.EventLabColor
import it.attendance100.mybicocca.ui.theme.EventLectureColor
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object CalendarUtils {

    // FORMATTERS

    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun fullDateFormatter(locale: Locale = Locale.ITALIAN): DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)

    fun monthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM", locale)

    fun dayOfWeekFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE", locale)

    fun dateWithYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", locale)

    // FORMATTING FUNCTIONS

    fun formatTime(hour: Int, minute: Int): String =
        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

    fun formatDuration(start: LocalDateTime, end: LocalDateTime): String {
        val duration = Duration.between(start, end)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return buildString {
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}min")
        }.trim()
    }

    fun formatEventLocation(location: String?, buildingCode: String?): String? = when {
        location != null && buildingCode != null -> "$location - $buildingCode"
        location != null -> location
        buildingCode != null -> buildingCode
        else -> null
    }

    fun formatSelectedDate(date: LocalDate, locale: Locale = Locale.getDefault()): String {
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            .replaceFirstChar { it.uppercase() }
        val day = date.dayOfMonth
        val month = date.month.getDisplayName(TextStyle.FULL, locale)
        return "$dayOfWeek $day $month"
    }

    // MAPPING FUNCTIONS

    fun getEventColor(eventType: EventType, fallbackColor: Color): Color = when (eventType) {
        EventType.LECTURE -> EventLectureColor
        EventType.LAB -> EventLabColor
        EventType.EXAM -> EventExamColor
        EventType.OTHER -> fallbackColor
    }

    fun getEventTypeStringRes(eventType: EventType): Int = when (eventType) {
        EventType.LECTURE -> R.string.event_type_lecture
        EventType.LAB -> R.string.event_type_lab
        EventType.EXAM -> R.string.event_type_exam
        EventType.OTHER -> R.string.event_type_other
    }

    // DATE UTILITIES

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun calculateEventProgress(event: CalendarEvent, now: LocalDateTime = LocalDateTime.now()): Float {
        val start = event.startDateTime
        val end = event.endDateTime

        if (now.isBefore(start)) return 0f
        if (now.isAfter(end)) return 1f

        val totalMinutes = Duration.between(start, end).toMinutes()
        val elapsedMinutes = Duration.between(start, now).toMinutes()

        return if (totalMinutes > 0) {
            (elapsedMinutes.toFloat() / totalMinutes).coerceIn(0f, 1f)
        } else 0f
    }

    fun groupEventsByDay(
        events: List<CalendarEvent>,
        days: List<LocalDate>
    ): Map<LocalDate, List<CalendarEvent>> = days.associateWith { date ->
        events.filter { it.date == date }.sortedBy { it.startDateTime }
    }

    // FILTERING FUNCTIONS

    fun filterEvents(
        events: List<CalendarEvent>,
        activeFilters: Set<EventType>,
        timeRange: TimeRange?,
        locationFilter: LocationFilter
    ): List<CalendarEvent> {
        val hasFilters = activeFilters.isNotEmpty() ||
                (timeRange != null && !timeRange.isDefault) ||
                !locationFilter.isEmpty

        if (!hasFilters) return events

        return events.filter { event ->
            val matchesType = activeFilters.isEmpty() || event.eventType in activeFilters
            val matchesTime = timeRange?.let { event.startTime?.let { t -> timeRange.contains(t) } ?: true } ?: true
            val matchesLocation = locationFilter.isEmpty ||
                    event.location?.let { it in locationFilter.selectedRooms } ?: false
            matchesType && matchesTime && matchesLocation
        }
    }

    fun extractBuildingsWithRooms(events: List<CalendarEvent>): List<BuildingWithRooms> =
        events
            .filter { it.buildingCode != null }
            .groupBy { it.buildingCode!! }
            .map { (building, buildingEvents) ->
                BuildingWithRooms(
                    buildingName = building,
                    rooms = buildingEvents.mapNotNull { it.location }.distinct().sorted()
                )
            }
            .sortedBy { it.buildingName }
}
