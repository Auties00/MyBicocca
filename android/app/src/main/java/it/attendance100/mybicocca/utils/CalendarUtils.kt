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
