package it.attendance100.mybicocca.data.mapper.calendar

import it.attendance100.mybicocca.data.local.calendar.CalendarEventDiscriminator
import it.attendance100.mybicocca.data.local.calendar.CalendarEventEntity
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Separator used to persist string lists (teacher names) in a single text column. */
private const val LIST_DELIMITER = "\n"

/**
 * Maps a domain event to its Room row, or null for events that are never materialized: only
 * lessons and exams are stored in calendar_events, while the other sources (deadlines,
 * appointments, library seats) are merged in at observe time from their own feature stores,
 * which remain their single source of truth.
 */
internal fun CalendarEvent.toEntity(): CalendarEventEntity? {
    val (discriminator, extras) = when (this) {
        is CalendarEvent.Lesson -> CalendarEventDiscriminator.LESSON to EntityExtras(
            subjectCode = subjectCode,
            teachersCsv = teachers.joinToString(LIST_DELIMITER).takeIf { it.isNotEmpty() },
            cfu = cfu,
        )
        is CalendarEvent.Exam -> CalendarEventDiscriminator.EXAM to EntityExtras(
            examTypeLabel = examTypeLabel,
            bookingPosition = bookingPosition,
            bookedAt = bookedAt?.toString(),
            cancellableUntil = cancellableUntil?.toString(),
        )
        is CalendarEvent.AssignmentDeadline,
        is CalendarEvent.Appointment,
        is CalendarEvent.LibraryReservation,
        -> return null
    }
    return CalendarEventEntity(
        id = id.value,
        careerId = careerId.value,
        source = source.code,
        discriminator = discriminator,
        date = date.toString(),
        startTime = start.toString(),
        endTime = end.toString(),
        title = title,
        shortLabel = shortLabel,
        room = location?.room,
        building = location?.building,
        mapsUrl = location?.mapsUrl,
        status = status.name,
        notes = notes,
        activityCode = activityCode,
        subjectCode = extras.subjectCode,
        teachersCsv = extras.teachersCsv,
        cfu = extras.cfu,
        examTypeLabel = extras.examTypeLabel,
        bookingPosition = extras.bookingPosition,
        bookedAt = extras.bookedAt,
        cancellableUntil = extras.cancellableUntil,
    )
}

/**
 * Rehydrates a Room row into the domain subtype its discriminator selects. Defensive on
 * purpose: rows with unparseable date/time columns or an unknown discriminator yield null
 * and are dropped from the stream instead of crashing it, and an unrecognized status falls
 * back to confirmed.
 */
internal fun CalendarEventEntity.toDomain(): CalendarEvent? {
    val location = if (room != null || building != null || mapsUrl != null) {
        EventLocation(room = room, building = building, mapsUrl = mapsUrl)
    } else null
    val statusEnum = runCatching { EventStatus.valueOf(status) }.getOrDefault(EventStatus.CONFIRMED)
    val date = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
    val start = runCatching { LocalTime.parse(startTime) }.getOrNull() ?: return null
    val end = runCatching { LocalTime.parse(endTime) }.getOrNull() ?: return null
    val careerId = CareerId(careerId)
    val eventId = CalendarEventId(id)
    return when (discriminator) {
        CalendarEventDiscriminator.LESSON -> CalendarEvent.Lesson(
            id = eventId,
            careerId = careerId,
            date = date,
            start = start,
            end = end,
            title = title,
            shortLabel = shortLabel,
            location = location,
            status = statusEnum,
            notes = notes,
            activityCode = activityCode,
            subjectCode = subjectCode,
            teachers = teachersCsv?.split(LIST_DELIMITER).orEmpty().filter { it.isNotBlank() },
            cfu = cfu,
        )
        CalendarEventDiscriminator.EXAM -> CalendarEvent.Exam(
            id = eventId,
            careerId = careerId,
            date = date,
            start = start,
            end = end,
            title = title,
            shortLabel = shortLabel,
            location = location,
            status = statusEnum,
            notes = notes,
            activityCode = activityCode,
            examTypeLabel = examTypeLabel,
            bookingPosition = bookingPosition,
            bookedAt = bookedAt?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() },
            cancellableUntil = cancellableUntil?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        )
        else -> null
    }
}

private data class EntityExtras(
    val subjectCode: String? = null,
    val teachersCsv: String? = null,
    val cfu: Int? = null,
    val examTypeLabel: String? = null,
    val bookingPosition: Int? = null,
    val bookedAt: String? = null,
    val cancellableUntil: String? = null,
)
