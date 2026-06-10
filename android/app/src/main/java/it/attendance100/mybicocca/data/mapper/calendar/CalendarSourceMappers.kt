package it.attendance100.mybicocca.data.mapper.calendar

import it.attendance100.mybicocca.data.local.elearning.deadline.DeadlineEntity
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.calendar.CalendarEvent
import it.attendance100.mybicocca.domain.model.calendar.CalendarEventId
import it.attendance100.mybicocca.domain.model.calendar.EventLocation
import it.attendance100.mybicocca.domain.model.calendar.EventSource
import it.attendance100.mybicocca.domain.model.calendar.EventStatus
import it.attendance100.mybicocca.domain.model.career.CareerId
import it.attendance100.mybicocca.domain.model.exam.BookedExam
import it.attendance100.mybicocca.domain.model.exam.ExamCallType
import it.attendance100.mybicocca.domain.model.exam.ExamType
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Esse3 gives the exam's start instant but no duration; a nominal two-hour block keeps the
 * event readable on the timeline without pretending to know the real end.
 */
private const val NOMINAL_EXAM_DURATION_HOURS = 2L

/**
 * Maps a booked Esse3 exam call to a calendar event, or null when Esse3 omits the date-time
 * or any usable title. The id derives from the (course of study, activity, call) key triple;
 * the title prefers the activity description over the call description; the end time is the
 * nominal exam block, clamped to 23:59 when it would spill past midnight.
 *
 * Bookings carry course names but no activity codes, so the code is looked up in
 * [activityCodeByNormalizedName] — a normalized-name join against the cached transcript —
 * which is what lets exam events deep-link to their e-learning course.
 */
internal fun BookedExam.toCalendarEvent(
    careerId: CareerId,
    activityCodeByNormalizedName: Map<String, String>,
): CalendarEvent.Exam? {
    val startDateTime = examDateTime ?: return null
    val title = activityDescription?.takeIf { it.isNotBlank() }
        ?: examCallDescription?.takeIf { it.isNotBlank() }
        ?: return null
    val start = startDateTime.toLocalTime()
    val end = startDateTime.plusHours(NOMINAL_EXAM_DURATION_HOURS)
        .takeIf { it.toLocalDate() == startDateTime.toLocalDate() }
        ?.toLocalTime()
        ?: LocalTime.of(23, 59)
    val location = if (classroomDescription != null || buildingDescription != null) {
        EventLocation(room = classroomDescription, building = buildingDescription)
    } else null
    return CalendarEvent.Exam(
        id = CalendarEventId.of(
            EventSource.EXAM,
            "${key.courseOfStudyId}_${key.activityId}_${key.callId}",
        ),
        careerId = careerId,
        date = startDateTime.toLocalDate(),
        start = start,
        end = end,
        title = title,
        shortLabel = shortLabelFor(title),
        location = location,
        status = EventStatus.CONFIRMED,
        notes = studentNote,
        activityCode = activityCodeByNormalizedName[normalizeSubjectName(title)],
        examTypeLabel = examTypeLabel(),
        bookingPosition = position,
        bookedAt = bookingDate,
        cancellableUntil = cancellableUntil,
    )
}

/**
 * Italian display label for the exam kind: the mode (preferring Esse3's own description over
 * one derived from the exam type) plus a partial-call marker, dot-separated; null when
 * neither is known.
 */
private fun BookedExam.examTypeLabel(): String? {
    val mode = examModeDescription?.takeIf { it.isNotBlank() } ?: when (examType) {
        ExamType.Written -> "Scritto"
        ExamType.Oral -> "Orale"
        ExamType.WrittenAndOralJoint, ExamType.WrittenAndOralSeparate -> "Scritto e orale"
        ExamType.Unknown -> null
    }
    val partial = "Prova parziale".takeIf { callType == ExamCallType.Partial }
    return listOfNotNull(partial, mode).joinToString(" · ").takeIf { it.isNotBlank() }
}

/**
 * Moodle names action events `"<activity> è in scadenza"` / `"<activity> is due"`; the
 * calendar card already says it's a deadline, so the suffix is just noise.
 */
private val DeadlineTitleSuffixes = listOf(" è in scadenza", " is due")

/**
 * Maps a cached e-learning deadline to a calendar event, or null for non-assignment kinds —
 * only assignment deadlines surface on the calendar. The due instant converts to wall-clock
 * time in [zone] and becomes a point-in-time event (start equals end); the Moodle title
 * suffix is stripped unless doing so would leave the title blank.
 */
internal fun DeadlineEntity.toCalendarEvent(careerId: CareerId, zone: ZoneId): CalendarEvent.AssignmentDeadline? {
    if (kind != DeadlineEntity.Kind.ASSIGNMENT) return null
    val dueAt = Instant.ofEpochMilli(dueAtMs).atZone(zone)
    val cleanTitle = DeadlineTitleSuffixes
        .fold(title.trim()) { acc, suffix -> acc.removeSuffix(suffix) }
        .ifBlank { title }
    return CalendarEvent.AssignmentDeadline(
        id = CalendarEventId.of(EventSource.DEADLINE, eventId.toString()),
        careerId = careerId,
        date = dueAt.toLocalDate(),
        start = dueAt.toLocalTime(),
        end = dueAt.toLocalTime(),
        title = cleanTitle,
        shortLabel = shortLabelFor(cleanTitle),
        status = EventStatus.CONFIRMED,
        activityCode = null,
        courseId = courseId,
        assignmentId = instanceId,
    )
}

/**
 * Maps a planning-portal reservation to a calendar event. The reservation store is
 * device-local rather than career-scoped, so the event is attributed to whichever career is
 * being observed. The office doubles as the location: area name as building, street address
 * as room.
 */
internal fun AppointmentReservation.toCalendarEvent(careerId: CareerId): CalendarEvent.Appointment =
    CalendarEvent.Appointment(
        id = CalendarEventId.of(EventSource.APPOINTMENT, code),
        careerId = careerId,
        date = start.toLocalDate(),
        start = start.toLocalTime(),
        end = end.toLocalTime(),
        title = serviceName,
        shortLabel = shortLabelFor(serviceName),
        location = if (areaName != null || areaAddress != null) {
            EventLocation(room = areaAddress, building = areaName)
        } else null,
        status = EventStatus.CONFIRMED,
        notes = null,
        serviceGroup = serviceGroup,
        webConferenceUrl = webConferenceUrl,
    )

/**
 * Maps an Affluences seat reservation to a calendar event, or null for cancelled
 * reservations. Like appointments, the store is account-wide rather than career-scoped, so
 * the event is attributed to the observing career. The library doubles as the location:
 * secondary name as building, seat as room.
 */
internal fun LibraryReservation.toCalendarEvent(careerId: CareerId): CalendarEvent.LibraryReservation? {
    if (state == LibraryReservationState.Cancelled) return null
    return CalendarEvent.LibraryReservation(
        id = CalendarEventId.of(EventSource.LIBRARY, reservationId.toString()),
        careerId = careerId,
        date = start.toLocalDate(),
        start = start.toLocalTime(),
        end = end.toLocalTime(),
        title = libraryName,
        shortLabel = shortLabelFor(libraryName),
        location = EventLocation(room = seatName, building = librarySecondaryName),
        status = EventStatus.CONFIRMED,
        notes = note,
        seatName = seatName,
    )
}
