package it.attendance100.mybicocca.domain.model.library

import java.time.LocalDateTime

// A seat booking as reported by the server's "my reservations" list. Cached in Room so it shows
// offline, but the server is the source of truth (synced on open when logged in).
data class LibraryReservation(
    val reservationId: Int,
    val libraryName: String,
    val librarySecondaryName: String?,
    val seatName: String,
    // Local wall-clock times in the site's zone.
    val start: LocalDateTime,
    val end: LocalDateTime,
    val note: String?,
    // Short human-readable code shown to the user.
    val reservationCode: String?,
    // Token that authorizes cancellation; present for every server-listed reservation.
    val cancellationToken: String?,
    val state: LibraryReservationState,
)

enum class LibraryReservationState { Upcoming, AwaitingConfirmation, Ongoing, Past, Cancelled, Other }
