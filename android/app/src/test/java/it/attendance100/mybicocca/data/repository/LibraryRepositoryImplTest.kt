package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.data.local.credentials.LibraryAccountStore
import it.attendance100.mybicocca.data.local.library.LibraryReservationDao
import it.attendance100.mybicocca.data.local.library.LibraryReservationEntity
import it.attendance100.mybicocca.data.remote.affluences.api.AffluencesApi
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesBookingInfo
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCheckin
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailLinkRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailValidation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMyReservation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMyReservations
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationState
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationValidation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesSiteCard
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibraryReservationState
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

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
    private val appScope: CoroutineScope = CoroutineScope(UnconfinedTestDispatcher())

    private lateinit var repository: LibraryRepositoryImpl

    @Before
    fun setUp() {
        coEvery { accountStore.email() } returns null
        repository = LibraryRepositoryImpl(api, reservationDao, accountStore, appScope)
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
        coEvery { api.account.checkIn(deviceId = "device-1", deviceLang = "it") } returns
            AffluencesCheckin(apiKey = "minted-key")
        coEvery { accountStore.setApiKey("minted-key") } just Runs
        coEvery { api.account.requestEmailValidation("minted-key", "mario@campus.unimib.it") } returns
            AffluencesEmailLinkRequest(requestUuid = "req-uuid")

        val uuid = repository.requestEmailValidation("mario@campus.unimib.it")

        assertThat(uuid).isEqualTo("req-uuid")
        coVerify(exactly = 1) { accountStore.setApiKey("minted-key") }
    }

    @Test
    fun `confirmEmailValidation returns false while still pending`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("key", "req") } throws RuntimeException("does_not_exist")

        val confirmed = repository.confirmEmailValidation("mario@campus.unimib.it", "req")

        assertThat(confirmed).isFalse()
        coVerify(exactly = 0) { accountStore.setSession(any(), any()) }
    }

    @Test
    fun `confirmEmailValidation rethrows a non-pending failure`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("key", "req") } throws RuntimeException("500 server error")

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.confirmEmailValidation("m@x.it", "req") }
        }
    }

    @Test
    fun `confirmEmailValidation returns false when the token is blank`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("key", "req") } returns AffluencesEmailValidation(authToken = "")

        assertThat(repository.confirmEmailValidation("m@x.it", "req")).isFalse()
    }

    @Test
    fun `confirmEmailValidation stores the session and syncs on success`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { api.account.pollEmailValidation("key", "req") } returns AffluencesEmailValidation(authToken = "auth")
        coEvery { accountStore.setSession("m@x.it", "auth") } just Runs
        coEvery { accountStore.authToken() } returns "auth"
        coEvery { api.account.getMyReservations("key", "auth") } returns AffluencesMyReservations(results = emptyList())
        coEvery { reservationDao.replaceAll(any()) } just Runs

        val confirmed = repository.confirmEmailValidation("m@x.it", "req")

        assertThat(confirmed).isTrue()
        coVerify(exactly = 1) { accountStore.setSession("m@x.it", "auth") }
        coVerify(exactly = 1) { reservationDao.replaceAll(any()) }
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
        val upcoming = myReservation(1, AffluencesReservationState.UPCOMING)
        val cancelled = myReservation(2, AffluencesReservationState.CANCELLED)
        coEvery { api.account.getMyReservations("key", "auth") } returns
            AffluencesMyReservations(results = listOf(upcoming, cancelled))
        val written = slot<List<LibraryReservationEntity>>()
        coEvery { reservationDao.replaceAll(capture(written)) } just Runs

        repository.refreshReservations()

        assertThat(written.captured).hasSize(1)
        assertThat(written.captured.single().reservationId).isEqualTo(1)
    }

    @Test
    fun `refreshReservations logs out and throws on an expired token`() = runTest {
        coEvery { accountStore.apiKey() } returns "key"
        coEvery { accountStore.authToken() } returns "auth"
        coEvery { api.account.getMyReservations("key", "auth") } throws RuntimeException("missing_auth_token")
        coEvery { accountStore.clearSession() } just Runs
        coEvery { reservationDao.clear() } just Runs

        assertThrows(LibraryNotLoggedInException::class.java) {
            runBlocking { repository.refreshReservations() }
        }

        coVerify(exactly = 1) { accountStore.clearSession() }
        coVerify(exactly = 1) { reservationDao.clear() }
    }

    @Test
    fun `logout clears the session and the cache`() = runTest {
        coEvery { accountStore.clearSession() } just Runs
        coEvery { reservationDao.clear() } just Runs

        repository.logout()

        coVerify(exactly = 1) { accountStore.clearSession() }
        coVerify(exactly = 1) { reservationDao.clear() }
    }

    @Test
    fun `cancelReservation skips reservations without a cancellation token`() = runTest {
        val reservation = reservation(cancellationToken = null)

        repository.cancelReservation(reservation)

        coVerify(exactly = 0) { api.reservations.cancelReservation(any()) }
    }

    @Test
    fun `cancelReservation tolerates a reservation already gone server-side`() = runTest {
        coEvery { api.reservations.cancelReservation("ct") } throws RuntimeException("not found")
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
        coEvery { api.reservations.validateReservation("code", "m@x.it") } throws
            RuntimeException("invalid_validation_code")

        assertThat(repository.verifyPresence("code")).isFalse()
    }

    @Test
    fun `verifyPresence returns true on a successful validation`() = runTest {
        coEvery { accountStore.email() } returns "m@x.it"
        coEvery { api.reservations.validateReservation("code", "m@x.it") } returns
            AffluencesReservationValidation()
        coEvery { accountStore.apiKey() } returns null

        assertThat(repository.verifyPresence("code")).isTrue()
    }

    @Test
    fun `verifyPresence rethrows an unrelated failure`() = runTest {
        coEvery { accountStore.email() } returns "m@x.it"
        coEvery { api.reservations.validateReservation("code", "m@x.it") } throws
            RuntimeException("503 unavailable")

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.verifyPresence("code") }
        }
    }

    @Test
    fun `cancelByToken tolerates a reservation already gone server-side`() = runTest {
        coEvery { api.reservations.cancelReservation("tok") } throws RuntimeException("not_found")
        coEvery { accountStore.apiKey() } returns null

        repository.cancelByToken("tok")

        coVerify(exactly = 1) { api.reservations.cancelReservation("tok") }
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
}
