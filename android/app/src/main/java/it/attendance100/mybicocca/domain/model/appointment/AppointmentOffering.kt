package it.attendance100.mybicocca.domain.model.appointment

import java.time.LocalTime

/**
 * A physical location ("area") of the Portale Planning portal where desk services are offered.
 *
 * @property id Portal area identifier.
 * @property code Portal area code.
 * @property name Display name of the location.
 * @property address Street address, when the portal provides one.
 */
data class AppointmentArea(
    val id: Int,
    val code: String,
    val name: String,
    val address: String?,
)

/**
 * Where and under which constraints a service can be booked. Every Bicocca desk is offered at
 * exactly one area, but the portal models a many-to-many relation.
 *
 * @property area Location the service is offered at.
 * @property durationSeconds Appointment duration at this area.
 * @property virtual Whether the appointment happens remotely (web conference).
 * @property descriptionHtml Rich-text description specific to this offering, when any.
 * @property minNoticeDays Reservations must be created at least this many days ahead; past the
 *   [noticeDeadline] hour the requirement grows by one day.
 * @property noticeDeadline Cut-off hour after which the minimum notice grows by one day.
 * @property horizonDays How far ahead the calendar is open, or null when unbounded.
 * @property bookingLimitCount Maximum number of bookings allowed within
 *   [bookingLimitIntervalDays], when the portal enforces one.
 * @property bookingLimitIntervalDays Length of the rolling window [bookingLimitCount] applies to.
 */
data class AppointmentOffering(
    val area: AppointmentArea,
    val durationSeconds: Int,
    val virtual: Boolean,
    val descriptionHtml: String?,
    val minNoticeDays: Int,
    val noticeDeadline: LocalTime?,
    val horizonDays: Int?,
    val bookingLimitCount: Int?,
    val bookingLimitIntervalDays: Int?,
)
