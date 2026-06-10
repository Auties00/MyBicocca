package it.attendance100.mybicocca.domain.model.library

import java.time.LocalDateTime

/**
 * A seat booking as reported by the Affluences "my reservations" list. Cached in Room so it
 * shows offline, but the server is the source of truth — the cache is re-synced when the
 * Biblioteca flow opens with a logged-in session. Listed on the Biblioteca landing page.
 *
 * @property reservationId Server-issued numeric reservation id.
 * @property libraryName Primary name of the library the seat belongs to.
 * @property librarySecondaryName Secondary library name; null when the site has none.
 * @property seatName Label of the booked seat.
 * @property start Reservation start, as local wall-clock time in the site's zone.
 * @property end Reservation end, as local wall-clock time in the site's zone.
 * @property note User-entered booking note; null when none.
 * @property reservationCode Short human-readable code shown to the user; null when the server
 * issues none.
 * @property cancellationToken Token that authorizes cancellation; present for every
 * server-listed reservation.
 * @property state Lifecycle state of the reservation.
 */
data class LibraryReservation(
    val reservationId: Int,
    val libraryName: String,
    val librarySecondaryName: String?,
    val seatName: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val note: String?,
    val reservationCode: String?,
    val cancellationToken: String?,
    val state: LibraryReservationState,
)

/** Lifecycle state of a reservation; [Other] covers server states the app does not recognize. */
enum class LibraryReservationState { Upcoming, AwaitingConfirmation, Ongoing, Past, Cancelled, Other }
