package it.attendance100.mybicocca.domain.model.library

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Wizard selections persisted before starting e-mail verification from Confirm, so the booking
 * flow can be restored if the process dies while the student is in their mail app.
 */
data class LibraryBookingDraft(
    val libraryId: String,
    val zoneId: Int,
    val date: LocalDate,
    val durationMinutes: Int,
    val startTime: LocalTime,
    val seatResourceId: Int,
    val note: String,
    val consentAccepted: Boolean,
    val savedAt: Instant,
)
