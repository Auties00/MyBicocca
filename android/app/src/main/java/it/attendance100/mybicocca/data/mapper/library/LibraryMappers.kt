package it.attendance100.mybicocca.data.mapper.library

import it.attendance100.mybicocca.data.local.library.LibraryReservationEntity
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAgreement
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAvailableResource
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCrowdLevel
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEvolution
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesLiveData
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMyReservation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationState
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceTypeFilters
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceTypeInfo
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSite
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteCard
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesWeekTimetable
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryCrowd
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryForecastPoint
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryOpeningRange
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibrarySlot
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The Affluences library-system root site; its children are the two bookable Bicocca libraries. */
const val LIBRARY_ROOT_ID = "61aa6184-f707-4dbd-9acb-45ff4e20c6b7"

/** The Bicocca libraries publish times local to Europe/Rome; all wire date-time strings are in it. */
val LibraryZoneId: ZoneId = ZoneId.of("Europe/Rome")

private val DateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val DateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val TimeFormat = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Maps a children-listing card to a library. The card carries only a light status (open flag
 * plus occupancy), enough for the landing cards, and omits contact/address fields. It also
 * omits the booking flag, which defaults to bookable — both Bicocca children accept booking.
 */
fun AffluencesSiteCard.toLibrary(): Library = Library(
    id = id,
    slug = slug,
    name = primaryName,
    secondaryName = secondaryName,
    address = null,
    latitude = null,
    longitude = null,
    pictureUrl = pictures.firstOrNull(),
    phone = null,
    email = null,
    websiteUrl = null,
    bookable = booking?.hasBooking ?: true,
    liveStatus = status?.let { s ->
        LibraryLiveStatus(
            isOpen = s.isOpen,
            statusText = s.closingText ?: s.openingText,
            occupancyPercentage = liveAttendance?.occupancyPercentage,
            crowdLevel = liveAttendance?.flow?.toCrowd(),
            todayForecast = emptyList(),
        )
    },
)

/** Maps the richer per-site payload, which fills the contact and address fields the card lacks. */
fun AffluencesSite.toLibrary(liveStatus: LibraryLiveStatus?): Library = Library(
    id = id,
    slug = slug,
    name = primaryName,
    secondaryName = secondaryName,
    address = address,
    latitude = latitude,
    longitude = longitude,
    pictureUrl = pictures.firstOrNull(),
    phone = phone,
    email = email,
    websiteUrl = websiteUrl,
    bookable = booking?.hasBooking ?: true,
    liveStatus = liveStatus,
)

/**
 * Maps live data to the full status. The forecast curve comes from the day bucket whose offset
 * is today, falling back to the remaining-day list when that bucket is absent; entries without
 * an hour range or occupancy are dropped, which also drops the bucket's leading whole-day
 * summary entry.
 */
fun AffluencesLiveData.toLiveStatus(): LibraryLiveStatus = LibraryLiveStatus(
    isOpen = status.isOpen,
    statusText = if (status.isOpen) status.closingText else status.openingText,
    occupancyPercentage = liveAttendance?.occupancyPercentage,
    crowdLevel = liveAttendance?.flow?.toCrowd(),
    todayForecast = (forecasts.firstOrNull { it.todayOffset == 0 }?.forecasts ?: todayForecasts)
        .mapNotNull { slot ->
            val start = slot.hourRange.firstOrNull()?.let(::parseLocalTime) ?: return@mapNotNull null
            val occupancy = slot.occupancyPercentage ?: return@mapNotNull null
            LibraryForecastPoint(
                time = start,
                occupancyPercentage = occupancy,
                rising = when (slot.occupancyEvolution) {
                    AffluencesEvolution.INCREASE -> true
                    AffluencesEvolution.DECREASE -> false
                    null -> null
                },
            )
        },
)

/** Maps the week timetable; opening ranges with unparseable times are dropped. */
fun AffluencesWeekTimetable.toWeekHours(): LibraryWeekHours = LibraryWeekHours(
    days = entries.map { day ->
        LibraryDayHours(
            day = LocalDate.parse(day.day, DateFormat),
            isToday = day.isToday,
            ranges = day.openingHours.mapNotNull { range ->
                val open = parseLocalTime(range.openingHour) ?: return@mapNotNull null
                val close = parseLocalTime(range.closingHour) ?: return@mapNotNull null
                LibraryOpeningRange(open, close)
            },
        )
    },
)

/**
 * Maps a resource type to a zone; a blank description degrades to the generic "Posto" name and
 * the zone color is recognized from keywords in the description.
 */
fun AffluencesResourceTypeInfo.toZone(): LibraryZone {
    val label = description?.trim().orEmpty()
    return LibraryZone(
        resourceTypeId = resourceTypeId,
        name = label.ifBlank { "Posto" },
        subdescription = subdescription?.trim()?.takeIf { it.isNotBlank() },
        imageUrl = imageUrl,
        color = zoneColorOf(label),
    )
}

/** Maps the reservation filters; unparseable dates and hours are dropped. */
fun AffluencesResourceTypeFilters.toConstraints(): LibraryBookingConstraints = LibraryBookingConstraints(
    openDays = openDays.mapNotNull { runCatching { LocalDate.parse(it, DateFormat) }.getOrNull() },
    openHours = openHours.mapNotNull(::parseLocalTime),
    durationsMinutes = durations,
)

/**
 * Maps an available resource to a seat. The short name strips the trailing "- ..." note from the
 * label, whose "presa elettrica" wording also signals the power outlet; slots default to a
 * 60-minute granularity when the payload omits one.
 */
fun AffluencesAvailableResource.toSeat(): LibrarySeat {
    val displayName = resourceName?.trim().orEmpty()
    return LibrarySeat(
        resourceId = resourceId,
        name = displayName,
        shortName = displayName.substringBefore(" -").trim().ifBlank { displayName },
        hasPowerOutlet = displayName.contains("presa elettrica", ignoreCase = true),
        description = description?.trim()?.takeIf { it.isNotBlank() },
        noteRequired = noteRequired,
        noteLabel = noteDescription?.trim()?.takeIf { it.isNotBlank() },
        slots = hours.mapNotNull { slot ->
            val start = parseLocalTime(slot.hour) ?: return@mapNotNull null
            LibrarySlot(
                start = start,
                durationMinutes = slot.granularityMinutes ?: 60,
                placesBookable = slot.placesBookable,
            )
        },
    )
}

fun AffluencesAgreement.toAgreement(): LibraryAgreement = LibraryAgreement(
    id = agreementId,
    name = name,
    url = url,
    mandatory = mandatoryConsent,
)

/**
 * Maps a server reservation into its Room cache row; the cache mirrors the server's "my
 * reservations" list. The wire reservation token is the short human-readable code shown to the
 * user, distinct from the cancellation token; unparseable start/end instants degrade to epoch
 * zero rather than dropping the row.
 */
fun AffluencesMyReservation.toEntity(): LibraryReservationEntity = LibraryReservationEntity(
    reservationId = reservationId,
    libraryName = siteName?.trim().orEmpty(),
    librarySecondaryName = siteSecondaryName?.trim()?.takeIf { it.isNotBlank() },
    seatName = resourceName?.trim().orEmpty(),
    startEpochSeconds = parseInstantSeconds(startDateTime) ?: 0L,
    endEpochSeconds = parseInstantSeconds(endDateTime) ?: 0L,
    note = note?.trim()?.takeIf { it.isNotBlank() },
    reservationCode = reservationToken?.trim()?.takeIf { it.isNotBlank() },
    cancellationToken = cancellationToken,
    state = state.toDomainState().name,
)

/**
 * Maps a cache row back to the domain; the cached epoch instants render as wall-clock times in
 * the Europe/Rome site zone, and an unrecognized stored state degrades to the catch-all entry.
 */
fun LibraryReservationEntity.toDomain(): LibraryReservation = LibraryReservation(
    reservationId = reservationId,
    libraryName = libraryName,
    librarySecondaryName = librarySecondaryName,
    seatName = seatName,
    start = LocalDateTime.ofInstant(Instant.ofEpochSecond(startEpochSeconds), LibraryZoneId),
    end = LocalDateTime.ofInstant(Instant.ofEpochSecond(endEpochSeconds), LibraryZoneId),
    note = note,
    reservationCode = reservationCode,
    cancellationToken = cancellationToken,
    state = runCatching { LibraryReservationState.valueOf(state) }.getOrDefault(LibraryReservationState.Other),
)

/** Server datetimes are ISO-8601 UTC instants; the epoch is cached and rendered in the site zone. */
private fun parseInstantSeconds(iso: String?): Long? =
    iso?.let { runCatching { Instant.parse(it).epochSecond }.getOrNull() }

private fun AffluencesReservationState.toDomainState(): LibraryReservationState = when (this) {
    AffluencesReservationState.UPCOMING -> LibraryReservationState.Upcoming
    AffluencesReservationState.AWAITING_CONFIRMATION -> LibraryReservationState.AwaitingConfirmation
    AffluencesReservationState.ONGOING -> LibraryReservationState.Ongoing
    AffluencesReservationState.PAST -> LibraryReservationState.Past
    AffluencesReservationState.CANCELLED -> LibraryReservationState.Cancelled
    AffluencesReservationState.UNKNOWN -> LibraryReservationState.Other
}

private fun AffluencesCrowdLevel.toCrowd(): LibraryCrowd = when (this) {
    AffluencesCrowdLevel.FLUID -> LibraryCrowd.Fluid
    AffluencesCrowdLevel.MODERATE -> LibraryCrowd.Moderate
    AffluencesCrowdLevel.DENSE -> LibraryCrowd.Dense
}

private fun zoneColorOf(description: String): LibraryZoneColor {
    val upper = description.uppercase()
    return when {
        "ROSSA" in upper -> LibraryZoneColor.Rossa
        "GIALLA" in upper -> LibraryZoneColor.Gialla
        "VERDE" in upper -> LibraryZoneColor.Verde
        "LILLA" in upper -> LibraryZoneColor.Lilla
        "BLU" in upper -> LibraryZoneColor.Blu
        "ARANCIO" in upper -> LibraryZoneColor.Arancio
        "CARREL" in upper -> LibraryZoneColor.Carrels
        else -> LibraryZoneColor.Other
    }
}

/** Accepts both "HH:mm" slot strings and "yyyy-MM-dd HH:mm:ss" date-time strings. */
private fun parseLocalTime(value: String): LocalTime? = runCatching {
    if (value.length <= 5) LocalTime.parse(value, TimeFormat)
    else LocalDateTime.parse(value, DateTimeFormat).toLocalTime()
}.getOrNull()
