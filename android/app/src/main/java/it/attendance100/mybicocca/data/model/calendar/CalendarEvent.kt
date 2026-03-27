package it.attendance100.mybicocca.data.model.calendar

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey val id: String, // "{source}_{originalId}"
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val location: String? = null,
    val source: EventSource,
    val eventType: EventType = EventType.OTHER,
    val isOverdue: Boolean = false,
    val actionUrl: String? = null,
    val courseId: Int? = null,
    val teacherName: String? = null,
    val buildingCode: String? = null,
    val subjectCode: String? = null,
)

enum class EventSource { EASYSTAFF, ELEARNING, ESSE3 }

enum class EventType { LECTURE, LAB, EXAM, OTHER }

/** Combines [date] + [startTime] into a [LocalDateTime], defaulting to midnight if time is null. */
val CalendarEvent.startDateTime: LocalDateTime
    get() = date.atTime(startTime ?: LocalTime.MIDNIGHT)

/** Combines [date] + [endTime] into a [LocalDateTime], defaulting to [startDateTime] if null. */
val CalendarEvent.endDateTime: LocalDateTime
    get() = date.atTime(endTime ?: startTime ?: LocalTime.MIDNIGHT)
