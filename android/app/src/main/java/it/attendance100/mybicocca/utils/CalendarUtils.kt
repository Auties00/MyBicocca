package it.attendance100.mybicocca.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.attendance100.mybicocca.R
import it.attendance100.mybicocca.data.entities.*
import it.attendance100.mybicocca.ui.theme.*
import java.time.*
import java.time.format.*
import java.util.*

/**
 * Calendar utility functions and constants.
 */
object CalendarUtils {

  const val BICOCCA_BRAND_COLOR = "#9C0C35"
  const val TEST_EVENT_ID_START = 1001L
  const val TEST_EVENT_ID_END = 9999L

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
  const val EVENT_HORIZONTAL_PADDING = 2
  const val TOTAL_DAYS = 7
  val OVERLAP_OFFSET = 4.dp

  // Pinch-to-Zoom Constants
  const val MIN_ZOOM = 0.0f
  const val MAX_ZOOM = 1.5f
  const val DEFAULT_ZOOM = 1.0f
  const val COMPACT_THRESHOLD = 0.3f
  val COMPACT_EVENT_HEIGHT = 28.dp

  // Stack Constants
  val STACK_CARD_CORNER_RADIUS = 16.dp
  val STACK_COLOR_BAR_WIDTH = 4.dp
  val STACK_OFFSET_X = 10.dp
  val STACK_OFFSET_Y = 6.dp
  const val SWIPE_THRESHOLD = 0.25f
  const val VELOCITY_THRESHOLD = 400f
  const val MAX_VISIBLE_CARDS = 3

  val HOUR_SLOT_HEIGHT = 80.dp
  const val WEEK_START_HOUR = 8
  const val WEEK_END_HOUR = 20
  const val WEEK_TOTAL_HOURS = WEEK_END_HOUR - WEEK_START_HOUR

  val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

  fun fullDateFormatter(locale: Locale = Locale.ITALIAN): DateTimeFormatter =
      DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", locale)

  fun monthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter =
      DateTimeFormatter.ofPattern("MMMM", locale)

  fun getEventColor(eventType: EventType, primaryColor: Color): Color = when (eventType) {
    EventType.LECTURE -> EventLectureColor
    EventType.LAB -> EventLabColor
    EventType.EXAM -> EventExamColor
    EventType.OTHER -> primaryColor
  }

  fun getEventTypeStringRes(eventType: EventType): Int = when (eventType) {
    EventType.LECTURE -> R.string.event_type_lecture
    EventType.LAB -> R.string.event_type_lab
    EventType.EXAM -> R.string.event_type_exam
    EventType.OTHER -> R.string.event_type_other
  }

  fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

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

  fun formatEventLocation(room: String?, building: String?): String? = when {
    room != null && building != null -> "$room • $building"
    room != null -> room
    building != null -> building
    else -> null
  }

  fun calculateEventProgress(event: CourseEvent): Float {
    val now = LocalDateTime.now()
    val total = Duration.between(event.startTime, event.endTime).toMinutes().toFloat()
    val elapsed = Duration.between(event.startTime, now).toMinutes().toFloat()
    return (elapsed / total).coerceIn(0f, 1f)
  }

  fun isEventInProgress(event: CourseEvent): Boolean {
    val now = LocalDateTime.now()
    return !event.isCancelled && now.isAfter(event.startTime) && now.isBefore(event.endTime)
  }

  fun isEventEnded(event: CourseEvent): Boolean =
      !event.isCancelled && LocalDateTime.now().isAfter(event.endTime)
}
