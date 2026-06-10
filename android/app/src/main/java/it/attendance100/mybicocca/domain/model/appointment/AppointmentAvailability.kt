package it.attendance100.mybicocca.domain.model.appointment

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Bookable days of a service/area within one month, from the Portale Planning month schedule.
 * Drives the calendar grid of the "Appuntamenti" booking flow.
 *
 * @property firstAvailable First bookable moment within the queried month, or null when the
 *   month is full.
 * @property availableDays Days that still have at least one free slot.
 */
data class AppointmentMonthAvailability(
    val firstAvailable: LocalDateTime?,
    val availableDays: List<LocalDate>,
)

/**
 * One time slot of a service/area day schedule.
 *
 * @property start Slot start time, local to the campus.
 * @property end Slot end time, local to the campus.
 * @property available Whether the slot can still be booked.
 */
data class AppointmentSlot(
    val start: LocalTime,
    val end: LocalTime,
    val available: Boolean,
)
