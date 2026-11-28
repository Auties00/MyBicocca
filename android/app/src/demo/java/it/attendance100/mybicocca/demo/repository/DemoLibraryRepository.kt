package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryCrowd
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryForecastPoint
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryOpeningRange
import it.attendance100.mybicocca.domain.model.library.LibraryPendingValidation
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryService
import it.attendance100.mybicocca.domain.model.library.LibraryServiceType
import it.attendance100.mybicocca.domain.model.library.LibrarySlot
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The two real Bicocca libraries (Centrale and Medicina) with null photos so the demo makes no
 * network calls, realistic seat names, opening hours, and sample bookings around [DemoClock.now].
 */
@Singleton
class DemoLibraryRepository @Inject constructor() : LibraryRepository {

    private val today: LocalDate = DemoClock.now.toLocalDate()

    private data class Site(
        val id: String,
        val slug: String,
        val name: String,
        val secondary: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val phone: String,
        val email: String,
        val occupancy: Int,
    )

    private val sites = listOf(
        Site(
            id = LibraryCatalog.CENTRALE_ID,
            slug = "universita-bicocca-biblioteca-centrale",
            name = "Biblioteca Centrale",
            secondary = "Ed. U6 Agorà (Unimib)",
            address = "Piazza dell'Ateneo Nuovo 1, 20126 Milano MI",
            latitude = LibraryCatalog.Centrale.latitude,
            longitude = LibraryCatalog.Centrale.longitude,
            phone = "+390264486251",
            email = "biblioteca.centrale@unimib.it",
            occupancy = 64,
        ),
        Site(
            id = LibraryCatalog.MEDICINA_ID,
            slug = "universita-bicocca-biblioteca-di-medicina",
            name = "Biblioteca di Medicina",
            secondary = "Ed. U8 Asclepio (Unimib)",
            address = "Via Cadore 48, 20900 Monza MB",
            latitude = LibraryCatalog.Medicina.latitude,
            longitude = LibraryCatalog.Medicina.longitude,
            phone = "+390264488001",
            email = "biblioteca.medicina@unimib.it",
            occupancy = 38,
        ),
    )

    private val linkedEmail = MutableStateFlow<String?>(DemoStudent.USERNAME)
    private var storedPendingValidation: LibraryPendingValidation? = null
    private var storedBookingDraft: LibraryBookingDraft? = null
    private var lastUsedId: String? = null

    private val reservations = MutableStateFlow(
        listOf(
            LibraryReservation(
                reservationId = 9001,
                libraryName = sites[0].name,
                librarySecondaryName = sites[0].secondary,
                seatName = "R 42 - Posto con presa elettrica",
                start = today.atTime(16, 0),
                end = today.atTime(18, 30),
                note = null,
                reservationCode = "A7K2",
                cancellationToken = "demo-token-9001",
                state = LibraryReservationState.Upcoming,
            ),
            LibraryReservation(
                reservationId = 8990,
                libraryName = sites[1].name,
                librarySecondaryName = sites[1].secondary,
                seatName = "Posto 17",
                start = today.minusDays(2).atTime(9, 0),
                end = today.minusDays(2).atTime(13, 0),
                note = null,
                reservationCode = "M3P9",
                cancellationToken = null,
                state = LibraryReservationState.Past,
            ),
        ),
    )

    override suspend fun getLibraries(): List<Library> = sites.map(::toDomain)

    override suspend fun getLibrary(libraryId: String): Library =
        toDomain(sites.first { it.id == libraryId })

    override suspend fun getLibraryServices(libraryId: String): List<LibraryService> = when (libraryId) {
        LibraryCatalog.MEDICINA_ID -> listOf(
            LibraryService(LibraryServiceType.Accessibility, "accessibility", "Accessibilità"),
            LibraryService(LibraryServiceType.Power, "power", "Prese elettriche"),
            LibraryService(LibraryServiceType.Wifi, "wifi", "Wi-Fi"),
            LibraryService(LibraryServiceType.AirConditioning, "ac", "Aria condizionata"),
        )
        else -> listOf(
            LibraryService(LibraryServiceType.Accessibility, "accessibility", "Accessibilità"),
            LibraryService(LibraryServiceType.Power, "power", "Prese elettriche"),
            LibraryService(LibraryServiceType.Wifi, "wifi", "Wi-Fi"),
            LibraryService(LibraryServiceType.AirConditioning, "ac", "Aria condizionata"),
            LibraryService(LibraryServiceType.StudyRoom, "study_room", "Sale studio"),
        )
    }

    override suspend fun getLiveStatus(libraryId: String): LibraryLiveStatus =
        liveStatus(sites.first { it.id == libraryId })

    override suspend fun lastUsedLibraryId(): String? = lastUsedId

    override suspend fun recordLastUsedLibrary(libraryId: String) {
        lastUsedId = libraryId
    }

    override suspend fun getWeekHours(libraryId: String, weekOffset: Int): LibraryWeekHours {
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(weekOffset.toLong())
        return LibraryWeekHours(
            days = (0L..6L).map { offset ->
                val day = monday.plusDays(offset)
                LibraryDayHours(
                    day = day,
                    isToday = day == today,
                    ranges = when (day.dayOfWeek) {
                        DayOfWeek.SUNDAY -> emptyList()
                        DayOfWeek.SATURDAY -> listOf(LibraryOpeningRange(SAT_OPENS, SAT_CLOSES))
                        else -> listOf(LibraryOpeningRange(WEEKDAY_OPENS, WEEKDAY_CLOSES))
                    },
                )
            },
        )
    }

    override suspend fun getZones(libraryId: String): List<LibraryZone> = when (libraryId) {
        LibraryCatalog.MEDICINA_ID -> listOf(
            LibraryZone(101, "Sala lettura", "Piano terra, 90 posti", null, LibraryZoneColor.Other),
        )
        else -> listOf(
            LibraryZone(1, "Zona Rossa", "II piano, 48 posti", null, LibraryZoneColor.Rossa),
            LibraryZone(2, "Zona Gialla", "II piano, 42 posti", null, LibraryZoneColor.Gialla),
            LibraryZone(3, "Zona Verde", "II piano, 64 posti", null, LibraryZoneColor.Verde),
            LibraryZone(4, "Zona Lilla", "II piano, 36 posti", null, LibraryZoneColor.Lilla),
            LibraryZone(5, "Zona Blu", "II piano, 40 posti", null, LibraryZoneColor.Blu),
            LibraryZone(6, "Zona Arancio", "II piano, 28 posti", null, LibraryZoneColor.Arancio),
            LibraryZone(7, "Carrels", "Postazioni individuali, 12 posti", null, LibraryZoneColor.Carrels),
        )
    }

    override suspend fun getBookingConstraints(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
    ): LibraryBookingConstraints = LibraryBookingConstraints(
        openDays = (0L..13L).map(today::plusDays).filter { it.dayOfWeek != DayOfWeek.SUNDAY },
        openHours = generateSequence(WEEKDAY_OPENS) { it.plusMinutes(60) }
            .takeWhile { it.isBefore(WEEKDAY_CLOSES) }
            .toList(),
        durationsMinutes = listOf(60, 120, 180, 240),
    )

    override suspend fun getAvailableSeats(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
        durationMinutes: Int,
        startTime: LocalTime?,
    ): List<LibrarySeat> {
        val prefix = seatPrefixFor(libraryId, zoneId)
        val baseNumber = if (libraryId == LibraryCatalog.MEDICINA_ID) 100 else 0
        val slotTimes = generateSequence(WEEKDAY_OPENS) { it.plusMinutes(60) }
            .takeWhile { it.isBefore(WEEKDAY_CLOSES) }
            .toList()
        return (1..18).map { number ->
            val seatNumber = baseNumber + number
            val shortName = "$prefix $seatNumber"
            val hasPower = number % 2 == 0
            val fullName = if (hasPower) "$shortName - Posto con presa elettrica" else shortName
            LibrarySeat(
                resourceId = zoneId * 100 + number,
                name = fullName,
                shortName = shortName,
                hasPowerOutlet = hasPower,
                description = if (hasPower) "Con presa elettrica" else null,
                noteRequired = false,
                noteLabel = null,
                slots = slotTimes.mapIndexed { index, time ->
                    LibrarySlot(
                        start = time,
                        durationMinutes = durationMinutes,
                        placesBookable = if ((index + number) % 4 == 0) 0 else 1,
                    )
                },
            )
        }
    }

    override suspend fun getAgreements(libraryId: String): List<LibraryAgreement> =
        listOf(LibraryAgreement(1, "Regolamento della biblioteca", "https://www.biblio.unimib.it", mandatory = true))

    override suspend fun bookSeat(
        library: Library,
        zone: LibraryZone,
        seat: LibrarySeat,
        date: LocalDate,
        startTime: LocalTime,
        durationMinutes: Int,
        email: String,
        note: String?,
    ): String? {
        if (linkedEmail.value == null) throw LibraryNotLoggedInException()
        lastUsedId = library.id
        val id = 9100 + reservations.value.size
        reservations.update {
            it + LibraryReservation(
                reservationId = id,
                libraryName = library.name,
                librarySecondaryName = library.secondaryName,
                seatName = seat.name,
                start = date.atTime(startTime),
                end = date.atTime(startTime).plusMinutes(durationMinutes.toLong()),
                note = note,
                reservationCode = "D$id",
                cancellationToken = "demo-token-$id",
                state = LibraryReservationState.Upcoming,
            )
        }
        return null
    }

    override fun observeLinkedEmail(): Flow<String?> = linkedEmail.asStateFlow()

    override suspend fun isLoggedIn(): Boolean = linkedEmail.value != null

    override suspend fun requestEmailValidation(email: String): String {
        val uuid = "demo-request"
        storedPendingValidation = LibraryPendingValidation(
            email = email,
            requestUuid = uuid,
            createdAt = Instant.now(AppClock.clock),
        )
        return uuid
    }

    override suspend fun confirmEmailValidation(email: String, requestUuid: String): Boolean {
        linkedEmail.value = email
        storedPendingValidation = null
        return true
    }

    override suspend fun logout() {
        linkedEmail.value = null
        storedPendingValidation = null
        storedBookingDraft = null
    }

    override suspend fun pendingValidation(): LibraryPendingValidation? = storedPendingValidation

    override suspend fun clearPendingValidation() {
        storedPendingValidation = null
    }

    override suspend fun bookingDraft(): LibraryBookingDraft? = storedBookingDraft

    override suspend fun saveBookingDraft(draft: LibraryBookingDraft) {
        storedBookingDraft = draft
    }

    override suspend fun clearBookingDraft() {
        storedBookingDraft = null
    }

    override fun observeReservations(): Flow<List<LibraryReservation>> = reservations

    override suspend fun refreshReservations() {
        if (linkedEmail.value == null) throw LibraryNotLoggedInException()
    }

    override suspend fun cancelReservation(reservation: LibraryReservation) =
        markCancelled { it.reservationId == reservation.reservationId }

    override suspend fun verifyPresence(code: String): Boolean = true

    override suspend fun cancelByToken(token: String) =
        markCancelled { it.cancellationToken == token }

    private fun markCancelled(matches: (LibraryReservation) -> Boolean) {
        reservations.update { all ->
            all.map { if (matches(it)) it.copy(state = LibraryReservationState.Cancelled) else it }
        }
    }

    private fun toDomain(site: Site) = Library(
        id = site.id,
        slug = site.slug,
        name = site.name,
        secondaryName = site.secondary,
        address = site.address,
        latitude = site.latitude,
        longitude = site.longitude,
        pictureUrl = null,
        phone = site.phone,
        email = site.email,
        websiteUrl = "https://www.biblio.unimib.it/it/chi-siamo/sedi-e-orari",
        bookable = true,
        liveStatus = liveStatus(site),
    )

    private fun liveStatus(site: Site): LibraryLiveStatus {
        val slots = DemoLibraryCrowdingData.weekdaySlots(site.occupancy, dayVariation = 0)
        return LibraryLiveStatus(
            isOpen = true,
            statusText = "Aperta fino alle 22:00",
            occupancyPercentage = site.occupancy,
            crowdLevel = when {
                site.occupancy < 40 -> LibraryCrowd.Fluid
                site.occupancy < 70 -> LibraryCrowd.Moderate
                else -> LibraryCrowd.Dense
            },
            todayForecast = slots.mapIndexed { index, slot ->
                val prev = slots.getOrNull(index - 1)?.occupancyPercentage
                LibraryForecastPoint(
                    time = slot.start,
                    occupancyPercentage = slot.occupancyPercentage,
                    rising = prev?.let { slot.occupancyPercentage > it },
                    endTime = slot.end,
                )
            },
            forecastDate = today,
        )
    }

    private fun seatPrefixFor(libraryId: String, zoneId: Int): String = when {
        libraryId == LibraryCatalog.MEDICINA_ID -> "Posto"
        zoneId == 1 -> "R"
        zoneId == 2 -> "G"
        zoneId == 3 -> "V"
        zoneId == 4 -> "L"
        zoneId == 5 -> "B"
        zoneId == 6 -> "A"
        zoneId == 7 -> "Carrel"
        else -> "Posto"
    }

    companion object {
        val WEEKDAY_OPENS: LocalTime = LocalTime.of(8, 30)
        val WEEKDAY_CLOSES: LocalTime = LocalTime.of(22, 0)
        val SAT_OPENS: LocalTime = LocalTime.of(9, 0)
        val SAT_CLOSES: LocalTime = LocalTime.of(13, 45)
    }
}
