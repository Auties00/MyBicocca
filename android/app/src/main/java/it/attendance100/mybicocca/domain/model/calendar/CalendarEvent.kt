package it.attendance100.mybicocca.domain.model.calendar

import it.attendance100.mybicocca.domain.model.career.CareerId
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * One entry of the unified university calendar. Five sources feed it, each with its own
 * subtype: EasyStaff timetable lessons, Esse3 booked exams, Moodle assignment deadlines,
 * planning-portal segreteria appointments and Affluences library seat reservations.
 *
 * Consumed by the calendar tab (month/week/day views, agenda sheet and event detail) and
 * indexed by the global search.
 */
sealed interface CalendarEvent {
    /** Source-namespaced stable identity, unique across all sources. */
    val id: CalendarEventId

    /** Career the event is attributed to; every calendar query is career-scoped. */
    val careerId: CareerId

    /** Upstream feed that produced the event; fixed per subtype. */
    val source: EventSource

    /** Day the event occurs on. */
    val date: LocalDate

    /** Start time within [date]. */
    val start: LocalTime

    /** End time within [date]; equals [start] for point-in-time entries such as deadlines. */
    val end: LocalTime

    /** Human-readable headline: course name, exam activity, service or library name. */
    val title: String

    /**
     * Compact label (typically 3-5 characters, e.g. "ASD" for "Algoritmi e Strutture Dati")
     * precomputed at mapping time for dense month-grid cells; null when no usable label
     * could be derived from the title.
     */
    val shortLabel: String?

    /** Venue of the event; null when the source provides no location information. */
    val location: EventLocation?

    /** Scheduling status; only lessons can be cancelled. */
    val status: EventStatus

    /**
     * Free-form auxiliary text: the curriculum path for lessons, the student note for exams,
     * the reservation note for library seats. Null when absent or unsupported by the source.
     */
    val notes: String?

    /**
     * University activity code (e.g. "E3101Q123") of the course this event belongs to.
     * Matches the e-learning course code, so the UI can jump from an event to its course.
     * Null for events with no course, or when the code could not be resolved.
     */
    val activityCode: String?

    /**
     * Timetable lesson from the EasyStaff weekly schedule grid.
     *
     * @property subjectCode EasyStaff-internal subject code of the planned course the lesson
     *   was requested for; null when the row was persisted without one.
     * @property teachers Teacher display names as listed in the timetable; empty when none
     *   are listed.
     * @property cfu Credit value of the course; the EasyStaff feed does not carry credits,
     *   so this is populated only if another source supplies it.
     */
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

    /**
     * A booked Esse3 exam (prenotazione), not a generic scheduled exam: only calls the
     * student is actually enrolled in appear on the calendar. Esse3 gives no end time, so
     * [end] is a nominal block synthesized at mapping time.
     *
     * @property examTypeLabel Italian display label describing the call (written/oral mode,
     *   plus a partial-exam marker); null when neither is known.
     * @property bookingPosition Position assigned by Esse3 in the call's booking list; null
     *   when Esse3 omits it.
     * @property bookedAt Moment the booking was placed; null when unknown.
     * @property cancellableUntil Last day Esse3 accepts a booking cancellation
     *   (dataFineIscr); null when not provided.
     */
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
        val examTypeLabel: String?,
        val bookingPosition: Int?,
        val bookedAt: LocalDateTime?,
        val cancellableUntil: LocalDate?,
    ) : CalendarEvent {
        override val source: EventSource get() = EventSource.EXAM
    }

    /**
     * Assignment due date from the Moodle e-learning calendar. A point in time: [start]
     * equals [end].
     *
     * @property courseId Moodle course id, for deep-linking into the owning course.
     * @property assignmentId Moodle assignment instance id (already translated from the
     *   course-module id the calendar web service exposes), keying the assignment detail.
     */
    data class AssignmentDeadline(
        override val id: CalendarEventId,
        override val careerId: CareerId,
        override val date: LocalDate,
        override val start: LocalTime,
        override val end: LocalTime,
        override val title: String,
        override val shortLabel: String?,
        override val status: EventStatus,
        override val activityCode: String?,
        val courseId: Int,
        val assignmentId: Int,
    ) : CalendarEvent {
        override val source: EventSource get() = EventSource.DEADLINE
        override val location: EventLocation? get() = null
        override val notes: String? get() = null
    }

    /**
     * Segreteria appointment booked through the planning portal. Reservations live in a
     * device-local store rather than a per-account server profile.
     *
     * @property serviceGroup Portal macro-section the booked service belongs to (e.g.
     *   "Carriere Studenti"); null when the portal omits it.
     * @property webConferenceUrl Meeting link for appointments held remotely; null for
     *   in-person appointments.
     */
    data class Appointment(
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
        val serviceGroup: String?,
        val webConferenceUrl: String?,
    ) : CalendarEvent {
        override val source: EventSource get() = EventSource.APPOINTMENT
        override val activityCode: String? get() = null
    }

    /**
     * Library seat reservation from Affluences. Shown only when a library account is linked;
     * the local store is empty otherwise.
     *
     * @property seatName Reserved seat label, also mirrored into [location] as the room.
     */
    data class LibraryReservation(
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
        val seatName: String,
    ) : CalendarEvent {
        override val source: EventSource get() = EventSource.LIBRARY
        override val activityCode: String? get() = null
    }
}
