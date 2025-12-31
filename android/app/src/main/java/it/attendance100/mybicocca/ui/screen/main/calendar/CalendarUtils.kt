package it.attendance100.mybicocca.ui.screen.main.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.domain.model.CalendarEvent
import it.attendance100.mybicocca.domain.model.CalendarEventType
import it.attendance100.mybicocca.ui.theme.EventExamColor
import it.attendance100.mybicocca.ui.theme.EventLabColor
import it.attendance100.mybicocca.ui.theme.EventLectureColor
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Calendar utility functions and constants.
 */
// TODO: Delete me
object CalendarUtils {

    const val BICOCCA_BRAND_COLOR = "#9C0C35"

    // Calendar Screen Constants
    const val PAGER_INITIAL_PAGE_OFFSET = Int.MAX_VALUE / 2
    const val PAGER_PAGE_COUNT = Int.MAX_VALUE
    val DAY_SELECTOR_HEIGHT = 72.dp

    // Day Timeline Constants
    val CURRENT_TIME_DOT_SIZE = 12.dp
    val CURRENT_TIME_LINE_HEIGHT = 2.dp
    val EVENT_CARD_CORNER_RADIUS = 16.dp
    val COLOR_BAR_WIDTH = 4.dp
    const val PULSE_DURATION_MS = 2000
    const val START_HOUR = 7
    const val END_HOUR = 22

    // Week Grid Constants
    val TIME_COLUMN_WIDTH = 50.dp
    const val TOTAL_DAYS = 7

    const val MAX_ZOOM = 1.5f
    const val DEFAULT_ZOOM = 1.0f

    // Stack Constants (per card sovrapposte)
    val STACK_CARD_CORNER_RADIUS = 16.dp
    val STACK_COLOR_BAR_WIDTH = 4.dp
    val STACK_OFFSET_X = 10.dp
    val STACK_OFFSET_Y = 6.dp
    const val SWIPE_THRESHOLD = 0.25f
    const val VELOCITY_THRESHOLD = 400f
    const val MAX_VISIBLE_CARDS = 3

    // Week Grid Time Constants
    val HOUR_SLOT_HEIGHT = 80.dp
    const val WEEK_START_HOUR = 8
    const val WEEK_END_HOUR = 20
    const val WEEK_TOTAL_HOURS = WEEK_END_HOUR - WEEK_START_HOUR

    // Formatters
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun monthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM", locale)

    fun getEventColor(eventType: CalendarEventType, primaryColor: Color): Color = when (eventType) {
        CalendarEventType.LECTURE -> EventLectureColor
        CalendarEventType.LAB -> EventLabColor
        CalendarEventType.EXAM -> EventExamColor
        CalendarEventType.OTHER -> primaryColor
    }

    fun getEventTypeStringRes(eventType: CalendarEventType): Int = when (eventType) {
        CalendarEventType.LECTURE -> R.string.event_type_lecture
        CalendarEventType.LAB -> R.string.event_type_lab
        CalendarEventType.EXAM -> R.string.event_type_exam
        CalendarEventType.OTHER -> R.string.event_type_other
    }

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun formatTime(hour: Int, minute: Int): String =
        String.Companion.format(Locale.getDefault(), "%02d:%02d", hour, minute)

    fun formatDuration(start: LocalDateTime, end: LocalDateTime): String {
        val duration = Duration.between(start, end)
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        return buildString {
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}min")
        }.trim()
    }

    fun formatEventLocation(room: String?, building: String?): String? {
        return when {
            room != null && building != null -> "$room - $building"
            room != null -> room
            building != null -> building
            else -> null
        }
    }

    fun calculateEventProgress(event: CalendarEvent): Float {
        val now = LocalDateTime.now()
        val start = event.startTime
        val end = event.endTime

        if (now.isBefore(start)) return 0f
        if (now.isAfter(end)) return 1f

        val totalDuration = Duration.between(start, end).toMinutes()
        val elapsedDuration = Duration.between(start, now).toMinutes()

        return if (totalDuration > 0) {
            (elapsedDuration.toFloat() / totalDuration).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
}