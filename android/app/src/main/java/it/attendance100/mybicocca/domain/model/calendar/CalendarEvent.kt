package it.attendance100.mybicocca.domain.model.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import java.time.LocalDate
import java.time.LocalTime

sealed interface CalendarEvent {
    val id: CalendarEventId
    val careerId: CareerId
    val source: EventSource
    val date: LocalDate
    val start: LocalTime
    val end: LocalTime
    val title: String
    val shortLabel: String?
    val location: EventLocation?
    val status: EventStatus
    val notes: String?

    // University activity code (e.g. "E3101Q123") of the course this event belongs to.
    // Matches the elearning course code, so the UI can jump from an event to its course.
    val activityCode: String?

    data class Lesson(
        override val id: CalendarEventId,
        override val careerId: CareerId,
        override val date: LocalDate,
        override val start: LocalTime,
        override val end: LocalTime,
        override val title: String,
        override val shortLabel: String?,
        override val location: EventLocation?,
        override val status: EventStatus,
        override val notes: String?,
        override val activityCode: String?,
        val subjectCode: String?,
        val teachers: List<String>,
        val cfu: Int?,
    ) : CalendarEvent {
        override val source: EventSource get() = EventSource.LESSON
    }

    data class Exam(
        override val id: CalendarEventId,
        override val careerId: CareerId,
        override val date: LocalDate,
        override val start: LocalTime,
        override val end: LocalTime,
        override val title: String,
        override val shortLabel: String?,
        override val location: EventLocation?,
        override val status: EventStatus,
        override val notes: String?,
        override val activityCode: String?,
        val examiners: List<String>,
        val examTypeLabel: String?,
    ) : CalendarEvent {
        override val source: EventSource get() = EventSource.EXAM
    }
}
