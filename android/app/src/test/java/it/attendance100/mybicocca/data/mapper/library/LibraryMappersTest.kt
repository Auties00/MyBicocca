package it.attendance100.mybicocca.data.mapper.library

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.library.LibraryReservationEntity
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAgreement
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAvailableResource
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMyReservation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationState
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceTypeFilters
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceTypeInfo
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesTimeSlot
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Covers the Affluences library mappers: reservation cache round-trip (state name, epoch
 * fallback, cancelled mapped not dropped), reservation-state and crowd-level enum mapping, zone
 * colour keyword recognition, seat short-name / power-outlet derivation, and the constraint and
 * agreement mappings.
 */
class LibraryMappersTest {

    @Test
    fun `reservation maps every wire state to its domain counterpart`() {
        assertThat(reservation(AffluencesReservationState.UPCOMING).toEntity().state)
            .isEqualTo(LibraryReservationState.Upcoming.name)
        assertThat(reservation(AffluencesReservationState.AWAITING_CONFIRMATION).toEntity().state)
            .isEqualTo(LibraryReservationState.AwaitingConfirmation.name)
        assertThat(reservation(AffluencesReservationState.ONGOING).toEntity().state)
            .isEqualTo(LibraryReservationState.Ongoing.name)
        assertThat(reservation(AffluencesReservationState.PAST).toEntity().state)
            .isEqualTo(LibraryReservationState.Past.name)
        assertThat(reservation(AffluencesReservationState.UNKNOWN).toEntity().state)
            .isEqualTo(LibraryReservationState.Other.name)
    }

    @Test
    fun `cancelled reservation is mapped to its own state not dropped`() {
        val entity = reservation(AffluencesReservationState.CANCELLED).toEntity()
        assertThat(entity.state).isEqualTo(LibraryReservationState.Cancelled.name)
        assertThat(entity.toDomain().state).isEqualTo(LibraryReservationState.Cancelled)
    }

    @Test
    fun `reservation entity carries the human code and cancellation token apart`() {
        val entity = reservation(
            AffluencesReservationState.UPCOMING,
            reservationToken = "ABCD",
            cancellationToken = "cancel-uuid",
        ).toEntity()
        assertThat(entity.reservationCode).isEqualTo("ABCD")
        assertThat(entity.cancellationToken).isEqualTo("cancel-uuid")
    }

    @Test
    fun `reservation entity blanks an empty reservation token and library secondary name`() {
        val entity = reservation(
            AffluencesReservationState.UPCOMING,
            reservationToken = "   ",
            siteSecondaryName = "  ",
        ).toEntity()
        assertThat(entity.reservationCode).isNull()
        assertThat(entity.librarySecondaryName).isNull()
    }

    @Test
    fun `reservation entity degrades unparseable instants to epoch zero`() {
        val entity = reservation(
            AffluencesReservationState.UPCOMING,
            startDateTime = "not-a-date",
            endDateTime = null,
        ).toEntity()
        assertThat(entity.startEpochSeconds).isEqualTo(0L)
        assertThat(entity.endEpochSeconds).isEqualTo(0L)
    }

    @Test
    fun `reservation entity parses ISO UTC instants to epoch seconds`() {
        val expected = Instant.parse("2024-06-15T08:00:00Z").epochSecond
        val entity = reservation(
            AffluencesReservationState.UPCOMING,
            startDateTime = "2024-06-15T08:00:00Z",
        ).toEntity()
        assertThat(entity.startEpochSeconds).isEqualTo(expected)
    }

    @Test
    fun `cached row renders the stored epoch in the Europe Rome site zone`() {
        val entity = LibraryReservationEntity(
            reservationId = 1,
            libraryName = "Biblioteca",
            librarySecondaryName = null,
            seatName = "R 1",
            startEpochSeconds = Instant.parse("2024-06-15T08:00:00Z").epochSecond,
            endEpochSeconds = Instant.parse("2024-06-15T10:00:00Z").epochSecond,
            note = null,
            reservationCode = "A1",
            cancellationToken = "tok",
            state = LibraryReservationState.Upcoming.name,
        )
        val domain = entity.toDomain()
        assertThat(domain.start).isEqualTo(LocalDateTime.of(2024, 6, 15, 10, 0))
        assertThat(domain.end).isEqualTo(LocalDateTime.of(2024, 6, 15, 12, 0))
    }

    @Test
    fun `cached row with unrecognized state degrades to Other`() {
        val entity = LibraryReservationEntity(
            reservationId = 1,
            libraryName = "Biblioteca",
            librarySecondaryName = null,
            seatName = "R 1",
            startEpochSeconds = 0L,
            endEpochSeconds = 0L,
            note = null,
            reservationCode = null,
            cancellationToken = null,
            state = "Garbage",
        )
        assertThat(entity.toDomain().state).isEqualTo(LibraryReservationState.Other)
    }

    @Test
    fun `seat strips the trailing note and detects the power outlet`() {
        val seat = AffluencesAvailableResource(
            resourceId = 5,
            resourceName = "R 100 - Posto con presa elettrica",
            hours = listOf(AffluencesTimeSlot(hour = "09:00", granularityMinutes = 60, placesBookable = 1)),
        ).toSeat()
        assertThat(seat.name).isEqualTo("R 100 - Posto con presa elettrica")
        assertThat(seat.shortName).isEqualTo("R 100")
        assertThat(seat.hasPowerOutlet).isTrue()
    }

    @Test
    fun `seat without a note keeps the full name as short name`() {
        val seat = AffluencesAvailableResource(resourceId = 1, resourceName = "R 7").toSeat()
        assertThat(seat.shortName).isEqualTo("R 7")
        assertThat(seat.hasPowerOutlet).isFalse()
    }

    @Test
    fun `seat slot defaults granularity to sixty minutes and drops unparseable hours`() {
        val seat = AffluencesAvailableResource(
            resourceId = 1,
            resourceName = "R 1",
            hours = listOf(
                AffluencesTimeSlot(hour = "10:30", granularityMinutes = null, placesBookable = 2),
                AffluencesTimeSlot(hour = "bad", granularityMinutes = 30, placesBookable = 1),
            ),
        ).toSeat()
        assertThat(seat.slots).hasSize(1)
        assertThat(seat.slots.first().start).isEqualTo(LocalTime.of(10, 30))
        assertThat(seat.slots.first().durationMinutes).isEqualTo(60)
        assertThat(seat.slots.first().placesBookable).isEqualTo(2)
    }

    @Test
    fun `zone uses Posto fallback for a blank description and recognizes the color keyword`() {
        val blank = AffluencesResourceTypeInfo(resourceTypeId = 1, description = "   ").toZone()
        assertThat(blank.name).isEqualTo("Posto")
        assertThat(blank.color).isEqualTo(LibraryZoneColor.Other)

        val red = AffluencesResourceTypeInfo(resourceTypeId = 2, description = "Zona Rossa").toZone()
        assertThat(red.name).isEqualTo("Zona Rossa")
        assertThat(red.color).isEqualTo(LibraryZoneColor.Rossa)
    }

    @Test
    fun `zone recognizes carrels from the CARREL keyword`() {
        val zone = AffluencesResourceTypeInfo(resourceTypeId = 3, description = "Carrel studio").toZone()
        assertThat(zone.color).isEqualTo(LibraryZoneColor.Carrels)
    }

    @Test
    fun `constraints drop unparseable open days and hours`() {
        val constraints = AffluencesResourceTypeFilters(
            resourceTypeId = 1,
            openDays = listOf("2024-06-15", "garbage"),
            openHours = listOf("09:00", "bad"),
            durations = listOf(60, 120),
        ).toConstraints()
        assertThat(constraints.openDays).containsExactly(LocalDate.of(2024, 6, 15))
        assertThat(constraints.openHours).containsExactly(LocalTime.of(9, 0))
        assertThat(constraints.durationsMinutes).containsExactly(60, 120).inOrder()
    }

    @Test
    fun `agreement maps id name url and mandatory consent`() {
        val agreement = AffluencesAgreement(
            agreementId = 8,
            name = "Termini",
            url = "https://example.com",
            mandatoryConsent = true,
        ).toAgreement()
        assertThat(agreement.id).isEqualTo(8)
        assertThat(agreement.name).isEqualTo("Termini")
        assertThat(agreement.url).isEqualTo("https://example.com")
        assertThat(agreement.mandatory).isTrue()
    }

    private fun reservation(
        state: AffluencesReservationState,
        reservationToken: String? = "CODE",
        cancellationToken: String? = "cancel",
        siteSecondaryName: String? = "Sede",
        startDateTime: String? = "2024-06-15T08:00:00Z",
        endDateTime: String? = "2024-06-15T10:00:00Z",
    ) = AffluencesMyReservation(
        reservationId = 1,
        resourceName = "R 1",
        siteName = "Biblioteca",
        siteSecondaryName = siteSecondaryName,
        reservationToken = reservationToken,
        cancellationToken = cancellationToken,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        state = state,
    )
}
