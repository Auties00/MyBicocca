package it.attendance100.mybicocca.domain.model.calendar

/**
 * Upstream feed a [CalendarEvent] originates from. The calendar aggregates five sources into
 * one stream; this tag drives per-source refresh, replacement and styling.
 *
 * @property code Stable string persisted in Room rows and sync stamps and used as the
 *   namespace prefix of [CalendarEventId]; changing a code orphans persisted data.
 */
enum class EventSource(val code: String) {
    /** EasyStaff timetable lessons. */
    LESSON("lesson"),

    /** Esse3 exam calls the student is booked into. */
    EXAM("exam"),

    /** Assignment deadlines from the Moodle e-learning calendar. */
    DEADLINE("deadline"),

    /** Segreteria appointments booked through the planning portal. */
    APPOINTMENT("appointment"),

    /** Library seat reservations from Affluences. */
    LIBRARY("library"),
}
