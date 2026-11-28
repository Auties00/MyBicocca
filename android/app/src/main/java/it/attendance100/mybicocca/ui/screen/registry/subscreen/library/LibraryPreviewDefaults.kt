package it.attendance100.mybicocca.ui.screen.registry.subscreen.library

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryCrowd
import it.attendance100.mybicocca.domain.model.library.LibraryCrowdingSlot
import it.attendance100.mybicocca.domain.model.library.LibraryDayHours
import it.attendance100.mybicocca.domain.model.library.LibraryForecastPoint
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryOpeningRange
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryService
import it.attendance100.mybicocca.domain.model.library.LibraryServiceType
import it.attendance100.mybicocca.domain.model.library.LibrarySlot
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowding
import it.attendance100.mybicocca.domain.model.library.LibraryWeekdayCrowdingPage
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.domain.model.library.buildWeeklyCrowdingChart
import it.attendance100.mybicocca.domain.model.library.toCrowdingSlot
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** Shared theme wrapper and synthetic fixtures for Biblioteca component `@Preview`s. */
@Composable
internal fun LibraryPreview(
    dark: Boolean,
    content: @Composable () -> Unit,
) {
    BicoccaTheme(dark = dark) {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
            content()
        }
    }
}

internal object LibraryPreviewDefaults {
    const val Email = "m.rossi99@campus.unimib.it"

    val Today: LocalDate = LocalDate.of(2026, 6, 18)
    val NowTime: LocalTime = LocalTime.of(10, 30)

    val LiveStatusOpen = LibraryLiveStatus(
        isOpen = true,
        statusText = "Aperta fino alle 22:00",
        occupancyPercentage = 64,
        crowdLevel = LibraryCrowd.Moderate,
        todayForecast = listOf(
            LibraryForecastPoint(LocalTime.of(8, 30), 22, rising = true, endTime = LocalTime.of(9, 30)),
            LibraryForecastPoint(LocalTime.of(9, 30), 40, rising = true, endTime = LocalTime.of(10, 0)),
            LibraryForecastPoint(LocalTime.of(10, 0), 54, rising = true, endTime = LocalTime.of(10, 30)),
            LibraryForecastPoint(LocalTime.of(10, 30), 64, rising = true, endTime = LocalTime.of(11, 0)),
            LibraryForecastPoint(LocalTime.of(11, 0), 72, rising = true, endTime = LocalTime.of(12, 30)),
            LibraryForecastPoint(LocalTime.of(12, 30), 52, rising = false, endTime = LocalTime.of(14, 0)),
            LibraryForecastPoint(LocalTime.of(14, 0), 68, rising = true, endTime = LocalTime.of(15, 30)),
            LibraryForecastPoint(LocalTime.of(15, 30), 80, rising = true, endTime = LocalTime.of(17, 0)),
            LibraryForecastPoint(LocalTime.of(17, 0), 66, rising = false, endTime = LocalTime.of(18, 30)),
            LibraryForecastPoint(LocalTime.of(18, 30), 48, rising = false, endTime = LocalTime.of(20, 0)),
            LibraryForecastPoint(LocalTime.of(20, 0), 26, rising = false, endTime = LocalTime.of(21, 30)),
        ),
        forecastDate = Today,
    )

    val LiveStatusClosed = LibraryLiveStatus(
        isOpen = false,
        statusText = "Apre alle 08:30",
        occupancyPercentage = null,
        crowdLevel = null,
        todayForecast = LiveStatusOpen.todayForecast,
        forecastDate = Today,
    )

    val LiveStatusUnknownOccupancy = LibraryLiveStatus(
        isOpen = true,
        statusText = "Aperta fino alle 22:00",
        occupancyPercentage = null,
        crowdLevel = null,
        todayForecast = LiveStatusOpen.todayForecast,
        forecastDate = Today,
    )

    val Libraries = listOf(
        Library(
            id = LibraryCatalog.CENTRALE_ID,
            slug = "universita-bicocca-biblioteca-centrale",
            name = "Biblioteca Centrale",
            secondaryName = "Ed. U6 Agorà (Unimib)",
            address = "Piazza dell'Ateneo Nuovo 1, 20126 Milano MI, Italie",
            latitude = LibraryCatalog.Centrale.latitude,
            longitude = LibraryCatalog.Centrale.longitude,
            pictureUrl = null,
            phone = "+390264486251",
            email = "biblioteca.centrale@unimib.it",
            websiteUrl = "https://www.biblio.unimib.it/it/chi-siamo/sedi-e-orari",
            bookable = true,
            liveStatus = LiveStatusOpen,
        ),
        Library(
            id = LibraryCatalog.MEDICINA_ID,
            slug = "universita-bicocca-biblioteca-di-medicina",
            name = "Biblioteca di Medicina",
            secondaryName = "Ed. U8 Asclepio (Unimib)",
            address = "Via Cadore 48, 20900 Monza MB, Italia",
            latitude = LibraryCatalog.Medicina.latitude,
            longitude = LibraryCatalog.Medicina.longitude,
            pictureUrl = null,
            phone = "+390264488001",
            email = "biblioteca.medicina@unimib.it",
            websiteUrl = "https://www.biblio.unimib.it/it/chi-siamo/sedi-e-orari",
            bookable = true,
            liveStatus = LiveStatusClosed,
        ),
    )

    val LibraryUnknownOccupancy: Library = Libraries.first().copy(liveStatus = LiveStatusUnknownOccupancy)

    val Services = listOf(
        LibraryService(LibraryServiceType.Accessibility, "accessibility", "Accessibilità"),
        LibraryService(LibraryServiceType.Power, "power", "Prese elettriche"),
        LibraryService(LibraryServiceType.Wifi, "wifi", "Wi-Fi"),
        LibraryService(LibraryServiceType.AirConditioning, "ac", "Aria condizionata"),
        LibraryService(LibraryServiceType.StudyRoom, "study_room", "Sale studio"),
    )

    val WeekHours = LibraryWeekHours(
        days = (0L..6L).map { offset ->
            val day = Today.plusDays(offset)
            LibraryDayHours(
                day = day,
                isToday = offset == 0L,
                ranges = when (day.dayOfWeek) {
                    DayOfWeek.SUNDAY -> emptyList()
                    DayOfWeek.SATURDAY -> listOf(LibraryOpeningRange(LocalTime.of(9, 0), LocalTime.of(13, 45)))
                    else -> listOf(LibraryOpeningRange(LocalTime.of(8, 30), LocalTime.of(22, 0)))
                },
            )
        },
    )

    private val PreviewHistory: Map<DayOfWeek, LibraryWeekdayCrowding> = mapOf(
        DayOfWeek.MONDAY to LibraryWeekdayCrowding(
            weekday = DayOfWeek.MONDAY,
            date = Today.minusDays(3),
            slots = LiveStatusOpen.todayForecast.map { it.toCrowdingSlot() },
            isClosed = false,
        ),
        DayOfWeek.TUESDAY to LibraryWeekdayCrowding(
            weekday = DayOfWeek.TUESDAY,
            date = Today.minusDays(2),
            slots = LiveStatusOpen.todayForecast.map {
                LibraryCrowdingSlot(it.time, it.endTime, (it.occupancyPercentage + 6).coerceAtMost(95))
            },
            isClosed = false,
        ),
        DayOfWeek.WEDNESDAY to LibraryWeekdayCrowding(
            weekday = DayOfWeek.WEDNESDAY,
            date = Today.minusDays(1),
            slots = LiveStatusOpen.todayForecast.map {
                LibraryCrowdingSlot(it.time, it.endTime, (it.occupancyPercentage - 8).coerceAtLeast(10))
            },
            isClosed = false,
        ),
        DayOfWeek.THURSDAY to LibraryWeekdayCrowding(
            weekday = DayOfWeek.THURSDAY,
            date = Today,
            slots = LiveStatusOpen.todayForecast.map { it.toCrowdingSlot() },
            isClosed = false,
        ),
        DayOfWeek.SATURDAY to LibraryWeekdayCrowding(
            weekday = DayOfWeek.SATURDAY,
            date = Today.minusDays(5),
            slots = listOf(
                LibraryCrowdingSlot(LocalTime.of(9, 0), LocalTime.of(10, 30), 28),
                LibraryCrowdingSlot(LocalTime.of(10, 30), LocalTime.of(12, 0), 46),
                LibraryCrowdingSlot(LocalTime.of(12, 0), LocalTime.of(13, 45), 30),
            ),
            isClosed = false,
        ),
        DayOfWeek.SUNDAY to LibraryWeekdayCrowding(
            weekday = DayOfWeek.SUNDAY,
            date = Today.minusDays(4),
            slots = emptyList(),
            isClosed = true,
        ),
    )

    val CrowdingWeekOpen: List<LibraryWeekdayCrowdingPage> = buildWeeklyCrowdingChart(
        today = Today,
        nowTime = NowTime,
        liveStatus = LiveStatusOpen,
        weekHours = WeekHours,
        history = PreviewHistory,
    )

    val CrowdingWeekClosedNow: List<LibraryWeekdayCrowdingPage> = buildWeeklyCrowdingChart(
        today = Today,
        nowTime = LocalTime.of(7, 30),
        liveStatus = LiveStatusClosed,
        weekHours = WeekHours,
        history = PreviewHistory,
    )

    val Zones = listOf(
        LibraryZone(1, "Zona Rossa", "II piano, 48 posti", null, LibraryZoneColor.Rossa),
        LibraryZone(2, "Zona Gialla", "II piano, 42 posti", null, LibraryZoneColor.Gialla),
        LibraryZone(3, "Zona Verde", "II piano, 64 posti", null, LibraryZoneColor.Verde),
        LibraryZone(4, "Zona Lilla", "II piano, 36 posti", null, LibraryZoneColor.Lilla),
        LibraryZone(5, "Zona Blu", "II piano, 40 posti", null, LibraryZoneColor.Blu),
        LibraryZone(6, "Zona Arancio", "II piano, 28 posti", null, LibraryZoneColor.Arancio),
        LibraryZone(7, "Carrels", "Postazioni individuali, 12 posti", null, LibraryZoneColor.Carrels),
    )

    val Constraints = LibraryBookingConstraints(
        openDays = listOf(Today, Today.plusDays(1), Today.plusDays(2), Today.plusDays(4), Today.plusDays(5)),
        openHours = listOf(LocalTime.of(8, 30), LocalTime.of(10, 30), LocalTime.of(14, 0), LocalTime.of(16, 0)),
        durationsMinutes = listOf(60, 120, 180, 240),
    )

    val Seats = listOf(
        LibrarySeat(
            resourceId = 101,
            name = "R 42 - Posto con presa elettrica",
            shortName = "R 42",
            hasPowerOutlet = true,
            description = "Vicino alla finestra",
            noteRequired = false,
            noteLabel = null,
            slots = listOf(LibrarySlot(LocalTime.of(14, 0), 120, 1)),
        ),
        LibrarySeat(
            resourceId = 102,
            name = "G 11 - Posto con presa elettrica",
            shortName = "G 11",
            hasPowerOutlet = true,
            description = "Tavolo centrale",
            noteRequired = false,
            noteLabel = null,
            slots = listOf(LibrarySlot(LocalTime.of(14, 0), 120, 1)),
        ),
        LibrarySeat(
            resourceId = 103,
            name = "Carrel 12",
            shortName = "Carrel 12",
            hasPowerOutlet = false,
            description = null,
            noteRequired = false,
            noteLabel = null,
            slots = listOf(LibrarySlot(LocalTime.of(14, 0), 120, 1)),
        ),
    )

    val Agreement = LibraryAgreement(
        id = 1,
        name = "Regolamento della biblioteca",
        url = "https://www.biblio.unimib.it",
        mandatory = true,
    )

    val Reservations = listOf(
        LibraryReservation(
            reservationId = 9001,
            libraryName = "Biblioteca Centrale",
            librarySecondaryName = "Ed. U6 Agorà (Unimib)",
            seatName = "R 42 - Posto con presa elettrica",
            start = LocalDateTime.of(Today, LocalTime.of(16, 0)),
            end = LocalDateTime.of(Today, LocalTime.of(18, 30)),
            note = null,
            reservationCode = "A7K2",
            cancellationToken = "preview-token-1",
            state = LibraryReservationState.Upcoming,
        ),
        LibraryReservation(
            reservationId = 9002,
            libraryName = "Biblioteca Centrale",
            librarySecondaryName = "Ed. U6 Agorà (Unimib)",
            seatName = "V 17",
            start = LocalDateTime.of(Today.plusDays(1), LocalTime.of(9, 0)),
            end = LocalDateTime.of(Today.plusDays(1), LocalTime.of(13, 0)),
            note = null,
            reservationCode = "B4M9",
            cancellationToken = "preview-token-2",
            state = LibraryReservationState.Upcoming,
        ),
        LibraryReservation(
            reservationId = 9003,
            libraryName = "Biblioteca di Medicina",
            librarySecondaryName = "Ed. U8 Asclepio (Unimib)",
            seatName = "Posto 101",
            start = LocalDateTime.of(Today.plusDays(3), LocalTime.of(15, 0)),
            end = LocalDateTime.of(Today.plusDays(3), LocalTime.of(18, 0)),
            note = null,
            reservationCode = "C9X1",
            cancellationToken = "preview-token-3",
            state = LibraryReservationState.Upcoming,
        ),
    )

    val LiveReservation = Reservations.first().copy(
        start = LocalDateTime.of(Today, LocalTime.of(10, 0)),
        end = LocalDateTime.of(Today, LocalTime.of(13, 0)),
        state = LibraryReservationState.Ongoing,
    )

    val PastReservations = listOf(
        LibraryReservation(
            reservationId = 8990,
            libraryName = "Biblioteca di Medicina",
            librarySecondaryName = "Ed. U8 Asclepio (Unimib)",
            seatName = "Posto 17",
            start = LocalDateTime.of(Today.minusDays(2), LocalTime.of(9, 0)),
            end = LocalDateTime.of(Today.minusDays(2), LocalTime.of(13, 0)),
            note = null,
            reservationCode = "M3P9",
            cancellationToken = null,
            state = LibraryReservationState.Past,
        ),
        LibraryReservation(
            reservationId = 8988,
            libraryName = "Biblioteca Centrale",
            librarySecondaryName = "Ed. U6 Agorà (Unimib)",
            seatName = "Carrel 12",
            start = LocalDateTime.of(Today.minusDays(5), LocalTime.of(14, 0)),
            end = LocalDateTime.of(Today.minusDays(5), LocalTime.of(17, 0)),
            note = null,
            reservationCode = null,
            cancellationToken = null,
            state = LibraryReservationState.Past,
        ),
    )
}
