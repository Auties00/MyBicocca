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

// Raised when a reservations call needs a logged-in session and there is none (or it expired).
class LibraryNotLoggedInException : Exception("Not logged in to Affluences")

// Catalog/availability are never cached (volatile). Reservations are server-owned and listed via
// the account session; Room only caches the last synced list so it shows offline.
interface LibraryRepository {

    suspend fun getLibraries(): List<Library>

    suspend fun getLiveStatus(libraryId: String): LibraryLiveStatus

    suspend fun getWeekHours(libraryId: String, weekOffset: Int = 0): LibraryWeekHours

    suspend fun getZones(libraryId: String): List<LibraryZone>

    suspend fun getBookingConstraints(libraryId: String, zoneId: Int, date: LocalDate): LibraryBookingConstraints

    suspend fun getAvailableSeats(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
        durationMinutes: Int,
        startTime: LocalTime?,
    ): List<LibrarySeat>

    suspend fun getAgreements(libraryId: String): List<LibraryAgreement>

    // Creates a booking authenticated with the logged-in session, so the server confirms it
    // immediately (no email step). Returns the new reservation code, when the server issues one.
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

    // --- Account (email-validation login) ---

    // The validated email, or null when not logged in. Hot.
    fun observeLinkedEmail(): Flow<String?>

    suspend fun isLoggedIn(): Boolean

    // Registers the device if needed and asks the server to email a validation link. Returns the
    // request id used to poll for completion.
    suspend fun requestEmailValidation(email: String): String

    // Polls the validation request. Returns true once the user has opened the email link (storing
    // the session and syncing), false while still pending.
    suspend fun confirmEmailValidation(email: String, requestUuid: String): Boolean

    suspend fun logout()

    // --- Reservations (server source of truth, Room cache) ---

    fun observeReservations(): Flow<List<LibraryReservation>>

    // Pulls the server list into the Room cache. Throws [LibraryNotLoggedInException] when there is
    // no valid session (including after the token expired).
    suspend fun refreshReservations()

    suspend fun cancelReservation(reservation: LibraryReservation)

    // Validates on-site presence with a code (scanned QR or typed) against the logged-in email.
    // Returns true when accepted, false when the code is invalid.
    suspend fun verifyPresence(code: String): Boolean

    // Cancel a booking from the token in its cancellation email link (deep link opened in the app),
    // then re-sync so the change is reflected.
    suspend fun cancelByToken(token: String)
}
