package it.attendance100.mybicocca.domain.model.appointment

import java.time.LocalTime

data class AppointmentArea(
    val id: Int,
    val code: String,
    val name: String,
    val address: String?,
)

// Where and under which constraints a service can be booked. Today every Bicocca desk is
// offered at exactly one area, but the portal models a many-to-many relation.
data class AppointmentOffering(
    val area: AppointmentArea,
    val durationSeconds: Int,
    val virtual: Boolean,
    val descriptionHtml: String?,
    // Reservations must be created at least this many days ahead; past the deadline hour
    // the requirement grows by one day.
    val minNoticeDays: Int,
    val noticeDeadline: LocalTime?,
    // How far ahead the calendar is open, or null when unbounded.
    val horizonDays: Int?,
    val bookingLimitCount: Int?,
    val bookingLimitIntervalDays: Int?,
)
