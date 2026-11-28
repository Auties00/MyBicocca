package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.time.AppClock
import it.attendance100.mybicocca.data.local.credentials.LibraryAccountStore
import it.attendance100.mybicocca.data.local.library.LibraryReservationDao
import it.attendance100.mybicocca.data.local.library.LibraryReservationEntity
import it.attendance100.mybicocca.data.local.settings.LibraryPreferencesStore
import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesValidateReservationRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailTokenRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesDeviceAuth
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCheckinRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAccountAuth
import it.attendance100.mybicocca.data.remote.affluences.api.AffluencesApi
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesBookingInfo
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCheckin
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailLinkRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailValidation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMyReservation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMyReservations
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationResult
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationState
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationValidation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSite
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteCard
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteDetails
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteService
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryBookingDraft
import it.attendance100.mybicocca.domain.model.library.LibraryCatalog
import it.attendance100.mybicocca.domain.model.library.LibraryPendingValidation
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryServiceType
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.LibraryZoneColor
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Affluences-backed library repository policy: catalog booking-flag filtering, the
 * email-validation handshake (pending vs. confirmed), the "my reservations" sync that drops
 * cancelled bookings and logs out on an expired token, and the error-key sniffers that map
 * a gone reservation / invalid presence code to graceful outcomes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryRepositoryImplTest {

    private val api: AffluencesApi = mockk(relaxed = true)
    private val reservationDao: LibraryReservationDao = mockk(relaxed = true)
    private val accountStore: LibraryAccountStore = mockk(relaxed = true)
    private val preferencesStore: LibraryPreferencesStore = mockk(relaxed = true)
    private val appScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher())

    private lateinit var repository: LibraryRepositoryImpl

    @Before
    fun setUp() {
        AppClock.clock = Clock.fixed(Instant.parse("2026-06-15T10:00:00Z"), ZoneOffset.UTC)
        coEvery { accountStore.email() } returns null
        repository = LibraryRepositoryImpl(api, reservationDao, accountStore, preferencesStore, appScope)
    }

    @After
    fun tearDown() {
        AppClock.clock = Clock.systemDefaultZone()
    }

    @Test
    fun `getLibraries keeps only bookable children`() = runTest {
        val bookable = AffluencesSiteCard(id = "a", slug = "lib-a", primaryName = "Biblioteca A")
        val notBookable = AffluencesSiteCard(
            id = "b",
            slug = "lib-b",
            primaryName = "Biblioteca B",
            booking = AffluencesBookingInfo(hasBooking = false),
        )
        coEvery { api.sites.getChildren(any()) } returns listOf(bookable, notBookable)

        val libraries = repository.getLibraries()

        assertThat(libraries).hasSize(1)
        assertThat(libraries.single().id).isEqualTo("a")
        assertThat(libraries.single().bookable).isTrue()
    }

    @Test
    fun `isLoggedIn is true only when both token and email are stored`() = runTest {
        coEvery { accountStore.authToken() } returns "tok"
        coEvery { accountStore.email() } returns "mario@campus.unimib.it"

        assertThat(repository.isLoggedIn()).isTrue()
    }

    @Test
    fun `isLoggedIn is false without a token`() = runTest {
        coEvery { accountStore.authToken() } returns null
        coEvery { accountStore.email() } returns "mario@campus.unimib.it"

        assertThat(repository.isLoggedIn()).isFalse()
    }

    @Test
    fun `requestEmailValidation mints an api key via check-in when none is stored`() = runTest {
        coEvery { accountStore.apiKey() } returns null
        coEvery { accountStore.deviceId() } returns "device-1"
        coEvery { api.account.checkIn(AffluencesCheckinRequest(deviceId = "device-1", deviceLang = "it")) } returns
            AffluencesCheckin(apiKey = "minted-key")
        coEvery { accountStore.setApiKey("minted-key") } just Runs
        coEvery { api.account.requestEmailValidation(AffluencesEmailTokenRequest("mario@campus.unimib.it"), AffluencesDeviceAuth("minted-key")) } returns
            AffluencesEmailLinkRequest(requestUuid = "req-uuid")

        val uuid = repository.requestEmailValidation("mario@campus.unimib.it")

        assertThat(uuid).isEqualTo("req-uuid")
        coVerify(exactly = 1) { accountStore.setApiKey("minted-key") }
        coVerify(exactly = 1) {
            accountStore.setPendingValidation(
                LibraryPendingValidation(
                    email = "mario@campus.unimib.it",
                    requestUuid = "req-uuid",
                    createdAt = Instant.parse("2026-06-15T10:00:00Z"),
                ),
            )
        }
    }

    @Test
    fun `confirmEmailValidation returns false while still pending`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("req", AffluencesDeviceAuth("key")) } throws AffluencesException(404, AffluencesException.NOT_VALIDATED_YET, "Not validated yet")

        val confirmed = repository.confirmEmailValidation("mario@campus.unimib.it", "req")

        assertThat(confirmed).isFalse()
        coVerify(exactly = 0) { accountStore.setSession(any(), any()) }
    }

    @Test
    fun `confirmEmailValidation rethrows a non-pending failure`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("req", AffluencesDeviceAuth("key")) } throws RuntimeException("500 server error")

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.confirmEmailValidation("m@x.it", "req") }
        }
    }

    @Test
    fun `confirmEmailValidation returns false when the token is blank`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("req", AffluencesDeviceAuth("key")) } returns AffluencesEmailValidation(authToken = "")

        assertThat(repository.confirmEmailValidation("m@x.it", "req")).isFalse()
    }

    @Test
    fun `confirmEmailValidation stores the session and syncs on success`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("req", AffluencesDeviceAuth("key")) } returns AffluencesEmailValidation(authToken = "auth")
        coEvery { accountStore.setSession("m@x.it", "auth") } just Runs
        coEvery { accountStore.authToken() } returns "auth"
        coEvery { api.account.getMyReservations(AffluencesAccountAuth("key", "auth")) } returns AffluencesMyReservations(results = emptyList())
        coEvery { reservationDao.syncWithServer(any(), any(), any()) } just Runs

        val confirmed = repository.confirmEmailValidation("m@x.it", "req")

        assertThat(confirmed).isTrue()
        coVerify(exactly = 1) { accountStore.setSession("m@x.it", "auth") }
        coVerify(exactly = 1) { accountStore.clearPendingValidation() }
        coVerify(exactly = 1) { reservationDao.syncWithServer(any(), any(), any()) }
    }

    @Test
    fun `refreshReservations throws when not logged in`() = runTest {
        coEvery { accountStore.apiKey() } returns null

        assertThrows(LibraryNotLoggedInException::class.java) {
            runBlocking { repository.refreshReservations() }
        }
    }

    @Test
    fun `refreshReservations caches the server list dropping cancelled bookings`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { accountStore.authToken() } returns "auth"
        val upcoming = myReservation(1, AffluencesReservationState.Upcoming)
        val cancelled = myReservation(2, AffluencesReservationState.Cancelled)
        coEvery { api.account.getMyReservations(AffluencesAccountAuth("key", "auth")) } returns
            AffluencesMyReservations(results = listOf(upcoming, cancelled))
        val written = slot<List<LibraryReservationEntity>>()
        val cancelledIds = slot<List<Int>>()
        coEvery {
            reservationDao.syncWithServer(capture(written), capture(cancelledIds), any())
        } just Runs

        repository.refreshReservations()

        assertThat(written.captured).hasSize(1)
        assertThat(written.captured.single().reservationId).isEqualTo(1)
        assertThat(cancelledIds.captured).containsExactly(2)
    }

    @Test
    fun `refreshReservations logs out and throws on an expired token`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { accountStore.authToken() } returns "auth"
        coEvery { api.account.getMyReservations(AffluencesAccountAuth("key", "auth")) } throws AffluencesException(401, "missing_auth_token", "Unauthorized")
        coEvery { accountStore.clearSession() } just Runs
        coEvery { reservationDao.clear() } just Runs

        assertThrows(LibraryNotLoggedInException::class.java) {
            runBlocking { repository.refreshReservations() }
        }

        coVerify(exactly = 1) { accountStore.clearSession() }
        coVerify(exactly = 1) { reservationDao.clear() }
    }

    @Test
    fun `logout clears the session, pending validation, draft and cache`() = runTest {
        coEvery { accountStore.clearSession() } just Runs
        coEvery { accountStore.clearPendingValidation() } just Runs
        coEvery { accountStore.clearBookingDraft() } just Runs
        coEvery { reservationDao.clear() } just Runs

        repository.logout()

        coVerify(exactly = 1) { accountStore.clearSession() }
        coVerify(exactly = 1) { accountStore.clearPendingValidation() }
        coVerify(exactly = 1) { accountStore.clearBookingDraft() }
        coVerify(exactly = 1) { reservationDao.clear() }
    }

    @Test
    fun `bookSeat throws LibraryNotLoggedInException without calling the booking API when unauthenticated`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { accountStore.authToken() } returns null

        assertThrows(LibraryNotLoggedInException::class.java) {
            runBlocking {
                repository.bookSeat(
                    library = library(),
                    zone = zone(),
                    seat = seat(),
                    date = LocalDate.of(2026, 6, 16),
                    startTime = LocalTime.of(10, 0),
                    durationMinutes = 60,
                    email = "student@campus.unimib.it",
                    note = null,
                )
            }
        }

        coVerify(exactly = 0) { api.reservations.createReservation(any(), any(), any()) }
    }

    @Test
    fun `pending validation and booking draft methods delegate to account store`() = runTest {
        val pending = LibraryPendingValidation("student@campus.unimib.it", "req-1", Instant.parse("2026-06-15T10:00:00Z"))
        val draft = LibraryBookingDraft(
            libraryId = "lib-1",
            zoneId = 1,
            date = LocalDate.of(2026, 6, 16),
            durationMinutes = 120,
            startTime = LocalTime.of(9, 0),
            seatResourceId = 10,
            note = "Note",
            consentAccepted = true,
            savedAt = Instant.parse("2026-06-15T10:00:00Z"),
        )
        coEvery { accountStore.pendingValidation() } returns pending
        coEvery { accountStore.bookingDraft() } returns draft

        assertThat(repository.pendingValidation()).isEqualTo(pending)
        assertThat(repository.bookingDraft()).isEqualTo(draft)

        repository.saveBookingDraft(draft)
        repository.clearBookingDraft()
        repository.clearPendingValidation()

        coVerify(exactly = 1) { accountStore.setBookingDraft(draft) }
        coVerify(exactly = 1) { accountStore.clearBookingDraft() }
        coVerify(exactly = 1) { accountStore.clearPendingValidation() }
    }

    @Test
    fun `cancelReservation skips reservations without a cancellation token`() = runTest {
        val reservation = reservation(cancellationToken = null)

        repository.cancelReservation(reservation)

        coVerify(exactly = 0) { api.reservations.cancelReservation(any()) }
    }

    @Test
    fun `cancelReservation tolerates a reservation already gone server-side`() = runTest {
        coEvery { api.reservations.cancelReservation("ct") } throws AffluencesException(404, null, "Not found")
        coEvery { accountStore.apiKey() } returns null

        repository.cancelReservation(reservation(cancellationToken = "ct"))

        coVerify(exactly = 1) { api.reservations.cancelReservation("ct") }
    }

    @Test
    fun `cancelReservation rethrows an unrelated cancellation failure`() = runTest {
        coEvery { api.reservations.cancelReservation("ct") } throws RuntimeException("500 server error")

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.cancelReservation(reservation(cancellationToken = "ct")) }
        }
    }

    @Test
    fun `verifyPresence throws when not logged in`() = runTest {
        coEvery { accountStore.email() } returns null

        assertThrows(LibraryNotLoggedInException::class.java) {
            runBlocking { repository.verifyPresence("code") }
        }
    }

    @Test
    fun `verifyPresence maps an invalid code to false`() = runTest {
        coEvery { accountStore.email() } returns "m@x.it"
        coEvery { api.reservations.validateReservation(AffluencesValidateReservationRequest("code", "m@x.it")) } throws
            AffluencesException(400, AffluencesException.INVALID_VALIDATION_CODE, "Invalid code")

        assertThat(repository.verifyPresence("code")).isFalse()
    }

    @Test
    fun `verifyPresence returns true on a successful validation`() = runTest {
        coEvery { accountStore.email() } returns "m@x.it"
        coEvery { api.reservations.validateReservation(AffluencesValidateReservationRequest("code", "m@x.it")) } returns
            AffluencesReservationValidation()
        coEvery { accountStore.apiKey() } returns null

        assertThat(repository.verifyPresence("code")).isTrue()
    }

    @Test
    fun `verifyPresence rethrows an unrelated failure`() = runTest {
        coEvery { accountStore.email() } returns "m@x.it"
        coEvery { api.reservations.validateReservation(AffluencesValidateReservationRequest("code", "m@x.it")) } throws
            RuntimeException("503 unavailable")

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.verifyPresence("code") }
        }
    }

    @Test
    fun `cancelByToken tolerates a reservation already gone server-side`() = runTest {
        coEvery { api.reservations.cancelReservation("tok") } throws AffluencesException(404, "not_found", "Not found")
        coEvery { accountStore.apiKey() } returns null

        repository.cancelByToken("tok")

        coVerify(exactly = 1) { api.reservations.cancelReservation("tok") }
    }

    @Test
    fun `getLibrary returns full site details with live photo override`() = runTest {
        coEvery { api.sites.getSite(LibraryCatalog.CENTRALE_ID) } returns AffluencesSite(
            id = LibraryCatalog.CENTRALE_ID,
            slug = "centrale",
            primaryName = "Biblioteca Centrale",
            secondaryName = "Ed. U6 Agorà (Unimib)",
            address = "Piazza dell'Ateneo Nuovo 1, Milano, Italie",
            phone = "+390264486251",
            email = "biblioteca.centrale@unimib.it",
            pictures = listOf("https://ignored.example/old.jpg"),
        )

        val result = repository.getLibrary(LibraryCatalog.CENTRALE_ID)

        assertThat(result.address).isEqualTo("Piazza dell'Ateneo Nuovo 1, Milano, Italie")
        assertThat(result.phone).isEqualTo("+390264486251")
        assertThat(result.email).isEqualTo("biblioteca.centrale@unimib.it")
        assertThat(result.pictureUrl).isEqualTo(LibraryCatalog.Centrale.photoUrl)
    }

    @Test
    fun `getLibraryServices maps details services and drops blank unknown entries`() = runTest {
        coEvery { api.sites.getDetails(LibraryCatalog.CENTRALE_ID) } returns AffluencesSiteDetails(
            services = listOf(
                AffluencesSiteService("wifi", "Wi-Fi"),
                AffluencesSiteService("study_room", "Sale studio"),
                AffluencesSiteService("custom", "Armadietti"),
                AffluencesSiteService("empty_unknown", "   "),
            ),
        )

        val services = repository.getLibraryServices(LibraryCatalog.CENTRALE_ID)

        assertThat(services.map { it.type }).containsExactly(
            LibraryServiceType.Wifi,
            LibraryServiceType.StudyRoom,
            LibraryServiceType.Other,
        ).inOrder()
        assertThat(services.last().name).isEqualTo("Armadietti")
    }

    @Test
    fun `bookSeat persists lastUsedLibraryId on success`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { accountStore.authToken() } returns "auth"
        coEvery { api.reservations.createReservation(any(), any(), any()) } returns AffluencesReservationResult(reservationId = 42)
        coEvery { api.account.getMyReservations(any()) } returns AffluencesMyReservations(results = emptyList())

        repository.bookSeat(
            library = library().copy(id = LibraryCatalog.MEDICINA_ID),
            zone = zone(),
            seat = seat(),
            date = LocalDate.of(2026, 6, 16),
            startTime = LocalTime.of(10, 0),
            durationMinutes = 60,
            email = "student@campus.unimib.it",
            note = null,
        )

        coVerify(exactly = 1) { preferencesStore.setLastUsedLibraryId(LibraryCatalog.MEDICINA_ID) }
    }

    @Test
    fun `lastUsedLibraryId and recordLastUsedLibrary delegate to preferencesStore`() = runTest {
        coEvery { preferencesStore.lastUsedLibraryId() } returns LibraryCatalog.CENTRALE_ID

        assertThat(repository.lastUsedLibraryId()).isEqualTo(LibraryCatalog.CENTRALE_ID)
        repository.recordLastUsedLibrary(LibraryCatalog.MEDICINA_ID)

        coVerify(exactly = 1) { preferencesStore.setLastUsedLibraryId(LibraryCatalog.MEDICINA_ID) }
    }

    private fun myReservation(id: Int, state: AffluencesReservationState) = AffluencesMyReservation(
        reservationId = id,
        resourceName = "Posto $id",
        siteName = "Biblioteca",
        reservationToken = "code-$id",
        cancellationToken = "cancel-$id",
        startDateTime = "2026-06-15T08:00:00Z",
        endDateTime = "2026-06-15T10:00:00Z",
        state = state,
    )

    private fun reservation(cancellationToken: String?) = LibraryReservation(
        reservationId = 1,
        libraryName = "Biblioteca",
        librarySecondaryName = null,
        seatName = "Posto 1",
        start = LocalDateTime.of(2026, 6, 15, 8, 0),
        end = LocalDateTime.of(2026, 6, 15, 10, 0),
        note = null,
        reservationCode = "code",
        cancellationToken = cancellationToken,
        state = LibraryReservationState.Upcoming,
    )

    private fun library() = Library(
        id = "lib-1",
        slug = "lib-1",
        name = "Biblioteca",
        secondaryName = null,
        address = null,
        latitude = null,
        longitude = null,
        pictureUrl = null,
        phone = null,
        email = null,
        websiteUrl = null,
        bookable = true,
        liveStatus = null,
    )

    private fun zone() = LibraryZone(
        resourceTypeId = 1,
        name = "Sala lettura",
        subdescription = null,
        imageUrl = null,
        color = LibraryZoneColor.Verde,
    )

    private fun seat() = LibrarySeat(
        resourceId = 10,
        name = "Posto 10",
        shortName = "10",
        hasPowerOutlet = false,
        description = null,
        noteRequired = false,
        noteLabel = null,
        slots = emptyList(),
    )
}
