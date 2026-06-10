package it.attendance100.mybicocca.domain.model.calendar

/**
 * Scheduling status of a [CalendarEvent].
 *
 * Only the EasyStaff lesson feed distinguishes cancelled slots; events from every other
 * source always map to [CONFIRMED] (cancelled library reservations are filtered out before
 * reaching the calendar rather than surfaced as [CANCELLED]).
 */
enum class EventStatus { CONFIRMED, CANCELLED }
