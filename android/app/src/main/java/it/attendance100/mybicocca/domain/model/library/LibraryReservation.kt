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

/** Default check-in window before reservation start, in minutes. */
const val DEFAULT_LIBRARY_PRESENCE_WINDOW_MINUTES = 10

private val CentraleZonePrefixRegex = Regex("^([RGVLBA])(?:\\s|\\d|-|$)", RegexOption.IGNORE_CASE)
private val CentraleCarrelPrefixRegex = Regex("^CARREL(?:\\s|\\d|-|$)", RegexOption.IGNORE_CASE)

/** Resolves the catalogued library metadata from the reservation's site names, or null if unknown. */
fun LibraryReservation.matchedMetadata(): LibraryMetadata? =
    LibraryCatalog.matchReservation(libraryName, librarySecondaryName)

/** Seat name without any trailing " - …" annotation (e.g. " - Posto con presa elettrica"). */
fun LibraryReservation.shortSeatName(): String =
    seatName.substringBefore(" -").trim().ifBlank { seatName.trim() }

/** Compact library label ("Centrale", "Medicina"), falling back to [libraryName] when unmatched. */
fun LibraryReservation.compactLibraryName(): String =
    matchedMetadata()?.shortName ?: libraryName

/** Campus map building code ("U06", "U08"), or null when the library is unrecognized. */
fun LibraryReservation.mapBuildingCode(): String? =
    matchedMetadata()?.mapBuildingCode

/**
 * Zone color driving the booking's illustration. Only Centrale seats with a recognized prefix map
 * to a colored chair or carrel; Medicina, unmatched sites, and other prefixes fall back to
 * [LibraryZoneColor.Other] (the desk).
 */
fun LibraryReservation.zoneColor(): LibraryZoneColor {
    if (matchedMetadata()?.id != LibraryCatalog.CENTRALE_ID) return LibraryZoneColor.Other
    val seat = shortSeatName()
    if (CentraleCarrelPrefixRegex.containsMatchIn(seat)) return LibraryZoneColor.Carrels
    val letter = CentraleZonePrefixRegex.find(seat)?.groupValues?.getOrNull(1)?.uppercase()
        ?: return LibraryZoneColor.Other
    return when (letter) {
        "R" -> LibraryZoneColor.Rossa
        "G" -> LibraryZoneColor.Gialla
        "V" -> LibraryZoneColor.Verde
        "L" -> LibraryZoneColor.Lilla
        "B" -> LibraryZoneColor.Blu
        "A" -> LibraryZoneColor.Arancio
        else -> LibraryZoneColor.Other
    }
}

/** Whether the booking has already ended at [now] (by end timestamp or server Past state). */
fun LibraryReservation.isEndedAt(now: LocalDateTime): Boolean =
    !end.isAfter(now) || state == LibraryReservationState.Past

/** Whether the booking is currently underway at [now]. */
fun LibraryReservation.isInProgressAt(now: LocalDateTime): Boolean =
    !isEndedAt(now) && !now.isBefore(start)

/** Whether the on-site presence check-in window is open at [now]. */
fun LibraryReservation.isCheckInOpenAt(
    now: LocalDateTime,
    presenceWindowMinutes: Int = DEFAULT_LIBRARY_PRESENCE_WINDOW_MINUTES,
): Boolean = !isEndedAt(now) && !now.isBefore(start.minusMinutes(presenceWindowMinutes.toLong()))

/** Whether the check-in window is open while the booking has not started yet ("Tra poco"). */
fun LibraryReservation.isUpcomingSoonAt(
    now: LocalDateTime,
    presenceWindowMinutes: Int = DEFAULT_LIBRARY_PRESENCE_WINDOW_MINUTES,
): Boolean = isCheckInOpenAt(now, presenceWindowMinutes) && now.isBefore(start)
