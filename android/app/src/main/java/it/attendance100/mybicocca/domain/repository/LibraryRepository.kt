package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

/**
 * Raised when a reservations call needs a logged-in Affluences session and there is none, or the
 * stored one has expired.
 */
class LibraryNotLoggedInException : Exception("Not logged in to Affluences")

/**
 * Library seat booking for the Bicocca libraries, backed by Affluences.
 *
 * Catalog and availability data (libraries, status, hours, zones, constraints, seats,
 * agreements) is volatile and network-only — never cached. Reservations are server-owned and
 * listed via the account session; Room only caches the last synced list so it still shows
 * offline. Calls that need the account session throw [LibraryNotLoggedInException] when no valid
 * session exists.
 */
interface LibraryRepository {

    /** The bookable Bicocca libraries — children of the Affluences root site that accept booking. */
    suspend fun getLibraries(): List<Library>

    /** A library's full live status, including the hourly occupancy forecast. */
    suspend fun getLiveStatus(libraryId: String): LibraryLiveStatus

    /** A library's opening hours for the week [weekOffset] weeks after the current one. */
    suspend fun getWeekHours(libraryId: String, weekOffset: Int = 0): LibraryWeekHours

    /** A library's bookable seating zones. */
    suspend fun getZones(libraryId: String): List<LibraryZone>

    /** The open days, start times, and durations a zone accepts around [date]. */
    suspend fun getBookingConstraints(libraryId: String, zoneId: Int, date: LocalDate): LibraryBookingConstraints

    /**
     * The zone's seats free on [date] for [durationMinutes]; a non-null [startTime] keeps only
     * seats bookable at exactly that time, otherwise seats with any open slot are returned.
     */
    suspend fun getAvailableSeats(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
        durationMinutes: Int,
        startTime: LocalTime?,
    ): List<LibrarySeat>

    /** The library's terms-of-use agreements to display for consent before booking. */
    suspend fun getAgreements(libraryId: String): List<LibraryAgreement>

    /**
     * Creates a booking authenticated with the logged-in session, so the server confirms it
     * immediately with no e-mail step, then re-syncs the cached reservation list. Returns the new
     * reservation's identifier, when the server issues one.
     */
    suspend fun bookSeat(
        library: Library,
        zone: LibraryZone,
        seat: LibrarySeat,
        date: LocalDate,
        startTime: LocalTime,
        durationMinutes: Int,
        email: String,
        note: String?,
    ): String?

    /** The validated e-mail, or `null` when not logged in. Hot — emits on login and logout. */
    fun observeLinkedEmail(): Flow<String?>

    /** Whether a validated e-mail and session token are stored. */
    suspend fun isLoggedIn(): Boolean

    /**
     * Registers the device if needed and asks the server to e-mail a validation link. Returns
     * the request id used to poll for completion.
     */
    suspend fun requestEmailValidation(email: String): String

    /**
     * Polls the validation request. Returns `true` once the user has opened the e-mailed link —
     * storing the session and syncing reservations — and `false` while still pending.
     */
    suspend fun confirmEmailValidation(email: String, requestUuid: String): Boolean

    /** Clears the stored session and the cached reservation list. */
    suspend fun logout()

    /** Streams the cached reservation list — the last server sync, kept in Room. */
    fun observeReservations(): Flow<List<LibraryReservation>>

    /**
     * Pulls the server's "my reservations" list into the Room cache. Throws
     * [LibraryNotLoggedInException] when there is no valid session, including when the stored
     * token has expired.
     */
    suspend fun refreshReservations()

    /**
     * Cancels the reservation through its own cancellation token, then re-syncs the cached list;
     * a reservation without a cancellation token is left untouched.
     */
    suspend fun cancelReservation(reservation: LibraryReservation)

    /**
     * Validates on-site presence with a code (scanned QR or typed) against the logged-in e-mail.
     * Returns `true` when accepted and `false` when the code is invalid.
     */
    suspend fun verifyPresence(code: String): Boolean

    /**
     * Cancels a booking from the reservation token in its e-mailed cancellation link (a deep
     * link the app opens), then re-syncs so the change is reflected.
     */
    suspend fun cancelByToken(token: String)
}
