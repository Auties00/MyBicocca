package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoStudent
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
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Three libraries at different occupancy, already linked to the student's e-mail, with the seat
 * the demo calendar shows for this afternoon and one past reservation.
 */
@Singleton
class DemoLibraryRepository @Inject constructor() : LibraryRepository {

    private val today: LocalDate = DemoClock.now.toLocalDate()

    private data class Site(val id: String, val name: String, val secondary: String, val address: String, val occupancy: Int)

    private val sites = listOf(
        Site("lib-u6", "Biblioteca di Ateneo", "Sede Centrale U6", "Piazza dell'Ateneo Nuovo 1, Milano", 64),
        Site("lib-u2", "Biblioteca di Ateneo", "Sede di Scienze U2", "Piazza della Scienza 3, Milano", 38),
        Site("lib-u8", "Biblioteca di Ateneo", "Sede di Medicina U8", "Via Cadore 48, Monza", 87),
    )

    private val reservations = MutableStateFlow(
        listOf(
            LibraryReservation(
                reservationId = 9001,
                libraryName = sites[0].name,
                librarySecondaryName = sites[0].secondary,
                seatName = "Posto 42",
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

    override suspend fun getLibraries(): List<Library> = sites.map { site ->
        Library(
            id = site.id,
            slug = site.id,
            name = site.name,
            secondaryName = site.secondary,
            address = site.address,
            latitude = null,
            longitude = null,
            pictureUrl = null,
            phone = "02 6448 0000",
            email = "biblioteca@unimib.it",
            websiteUrl = "https://www.biblio.unimib.it",
            bookable = true,
            liveStatus = liveStatus(site),
        )
    }

    override suspend fun getLiveStatus(libraryId: String): LibraryLiveStatus = liveStatus(sites.first { it.id == libraryId })

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
                        DayOfWeek.SATURDAY -> listOf(LibraryOpeningRange(LocalTime.of(9, 0), LocalTime.of(13, 0)))
                        else -> listOf(LibraryOpeningRange(OPENS, CLOSES))
                    },
                )
            },
        )
    }

    override suspend fun getZones(libraryId: String): List<LibraryZone> = listOf(
        LibraryZone(1, "Sala lettura", "Piano terra, 120 posti", null, LibraryZoneColor.Verde),
        LibraryZone(2, "Sala silenziosa", "Primo piano, 60 posti", null, LibraryZoneColor.Blu),
        LibraryZone(3, "Sala gruppi", "Secondo piano, 24 posti", null, LibraryZoneColor.Arancio),
    )

    override suspend fun getBookingConstraints(libraryId: String, zoneId: Int, date: LocalDate): LibraryBookingConstraints =
        LibraryBookingConstraints(
            openDays = (0L..13L).map(today::plusDays).filter { it.dayOfWeek != DayOfWeek.SUNDAY },
            openHours = (OPENS.hour until CLOSES.hour).map { LocalTime.of(it, 0) },
            durationsMinutes = listOf(60, 120, 180, 240),
        )

    override suspend fun getAvailableSeats(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
        durationMinutes: Int,
        startTime: LocalTime?,
    ): List<LibrarySeat> = (1..18).map { number ->
        val seat = zoneId * 100 + number
        LibrarySeat(
            resourceId = seat,
            name = "Posto $seat",
            shortName = seat.toString(),
            hasPowerOutlet = number % 2 == 0,
            description = if (number % 2 == 0) "Con presa elettrica" else null,
            noteRequired = false,
            noteLabel = null,
            slots = (OPENS.hour until CLOSES.hour).map { hour ->
                LibrarySlot(LocalTime.of(hour, 0), durationMinutes, placesBookable = if ((hour + number) % 4 == 0) 0 else 1)
            },
        )
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

    override fun observeLinkedEmail(): Flow<String?> = flowOf(DemoStudent.USERNAME)

    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun requestEmailValidation(email: String): String = "demo-request"

    override suspend fun confirmEmailValidation(email: String, requestUuid: String): Boolean = true

    override suspend fun logout() = Unit

    override fun observeReservations(): Flow<List<LibraryReservation>> = reservations

    override suspend fun refreshReservations() = Unit

    override suspend fun cancelReservation(reservation: LibraryReservation) = markCancelled { it.reservationId == reservation.reservationId }

    override suspend fun verifyPresence(code: String): Boolean = true

    override suspend fun cancelByToken(token: String) = markCancelled { it.cancellationToken == token }

    private fun markCancelled(matches: (LibraryReservation) -> Boolean) {
        reservations.update { all -> all.map { if (matches(it)) it.copy(state = LibraryReservationState.Cancelled) else it } }
    }

    private fun liveStatus(site: Site) = LibraryLiveStatus(
        isOpen = true,
        statusText = "Aperta fino alle ${CLOSES.hour}:00",
        occupancyPercentage = site.occupancy,
        crowdLevel = when {
            site.occupancy < 45 -> LibraryCrowd.Fluid
            site.occupancy < 75 -> LibraryCrowd.Moderate
            else -> LibraryCrowd.Dense
        },
        // Rises through the morning, dips at lunch, peaks mid-afternoon.
        todayForecast = listOf(18, 35, 52, 64, 58, 49, 66, 78, 71, 55, 34).mapIndexed { index, base ->
            val scaled = (base * site.occupancy / 64).coerceIn(5, 98)
            LibraryForecastPoint(LocalTime.of(OPENS.hour + index, 0), scaled, rising = index in listOf(0, 1, 2, 6))
        },
    )

    private companion object {
        val OPENS: LocalTime = LocalTime.of(9, 0)
        val CLOSES: LocalTime = LocalTime.of(20, 0)
    }
}
