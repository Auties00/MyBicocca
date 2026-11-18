package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.credentials.LibraryAccountStore
import it.attendance100.mybicocca.data.local.library.LibraryReservationDao
import it.attendance100.mybicocca.data.mapper.library.LIBRARY_ROOT_ID
import it.attendance100.mybicocca.data.mapper.library.toAgreement
import it.attendance100.mybicocca.data.mapper.library.toConstraints
import it.attendance100.mybicocca.data.mapper.library.toDomain
import it.attendance100.mybicocca.data.mapper.library.toEntity
import it.attendance100.mybicocca.data.mapper.library.toLibrary
import it.attendance100.mybicocca.data.mapper.library.toLiveStatus
import it.attendance100.mybicocca.data.mapper.library.toSeat
import it.attendance100.mybicocca.data.mapper.library.toWeekHours
import it.attendance100.mybicocca.data.mapper.library.toZone
import it.attendance100.mybicocca.data.remote.affluences.api.AffluencesApi
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAccountAuth
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCheckinRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesDeviceAuth
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailTokenRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationState
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesValidateReservationRequest
import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.library.Library
import it.attendance100.mybicocca.domain.model.library.LibraryAgreement
import it.attendance100.mybicocca.domain.model.library.LibraryBookingConstraints
import it.attendance100.mybicocca.domain.model.library.LibraryLiveStatus
import it.attendance100.mybicocca.domain.model.library.LibraryReservation
import it.attendance100.mybicocca.domain.model.library.LibrarySeat
import it.attendance100.mybicocca.domain.model.library.LibraryWeekHours
import it.attendance100.mybicocca.domain.model.library.LibraryZone
import it.attendance100.mybicocca.domain.model.library.isBookableAt
import it.attendance100.mybicocca.domain.repository.LibraryNotLoggedInException
import it.attendance100.mybicocca.domain.repository.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Affluences-backed implementation of the library repository.
 *
 * Catalog and availability read straight from the Affluences API: the bookable libraries are
 * the children of the Bicocca root site, and status, timetable, zones, constraints, seats, and
 * agreements come from the per-site live-data, week-timetable, and reservation endpoints.
 * Reservations sync from the account "my reservations" endpoint into the Room cache the UI
 * reads. The account session — per-install device api key minted via check-in, plus the auth
 * token and e-mail of the validated address — lives in the encrypted account store. Failures
 * are recognized by the error code or HTTP status of [AffluencesException].
 */
@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val api: AffluencesApi,
    private val reservationDao: LibraryReservationDao,
    private val accountStore: LibraryAccountStore,
    @ApplicationScope appScope: CoroutineScope,
) : LibraryRepository {

    private val linkedEmail = MutableStateFlow<String?>(null)

    init {
        appScope.launch { linkedEmail.value = accountStore.email() }
    }

    override suspend fun getLibraries(): List<Library> =
        api.sites.getChildren(LIBRARY_ROOT_ID).map { it.toLibrary() }.filter { it.bookable }

    override suspend fun getLiveStatus(libraryId: String): LibraryLiveStatus =
        api.sites.getLiveData(libraryId).toLiveStatus()

    override suspend fun getWeekHours(libraryId: String, weekOffset: Int): LibraryWeekHours =
        api.sites.getWeekTimetable(libraryId, weekOffset).toWeekHours()

    override suspend fun getZones(libraryId: String): List<LibraryZone> =
        api.reservations.getReservationInfo(libraryId).types.map { it.toZone() }

    override suspend fun getBookingConstraints(libraryId: String, zoneId: Int, date: LocalDate): LibraryBookingConstraints =
        api.reservations.getResourceTypeFilters(libraryId, zoneId, date).toConstraints()

    override suspend fun getAvailableSeats(
        libraryId: String,
        zoneId: Int,
        date: LocalDate,
        durationMinutes: Int,
        startTime: LocalTime?,
    ): List<LibrarySeat> =
        api.reservations.getAvailableResources(
            siteIdentifier = libraryId,
            date = date,
            resourceTypeId = zoneId,
            durationMinutes = durationMinutes,
        )
            .map { it.toSeat() }
            .filter { seat -> if (startTime != null) seat.isBookableAt(startTime) else seat.slots.isNotEmpty() }

    override suspend fun getAgreements(libraryId: String): List<LibraryAgreement> =
        api.reservations.getAgreements(libraryId).map { it.toAgreement() }

    /**
     * Creates the reservation authenticated as the logged-in mobile-app account — device api key
     * plus session token, like the "my reservations" calls — so the server recognizes the
     * trusted device/e-mail and confirms immediately, with no e-mail-validation step. A
     * best-effort re-sync then pulls the new booking into the cache.
     */
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
        val endTime = LocalDateTime.of(date, startTime).plusMinutes(durationMinutes.toLong()).toLocalTime()
        val result = api.reservations.createReservation(
            resourceId = seat.resourceId,
            body = AffluencesReservationRequest(
                email = email,
                date = date,
                startTime = startTime,
                endTime = endTime,
                personCount = 1,
                note = note,
            ),
            auth = accountAuth(),
        )
        runCatching { refreshReservations() }
        return result.reservationId?.toString()
    }

    override fun observeLinkedEmail(): Flow<String?> = linkedEmail.asStateFlow()

    override suspend fun isLoggedIn(): Boolean =
        accountStore.authToken() != null && accountStore.email() != null

    override suspend fun requestEmailValidation(email: String): String {
        val apiKey = ensureApiKey()
        val request = api.account.requestEmailValidation(AffluencesEmailTokenRequest(email), AffluencesDeviceAuth(apiKey))
        return request.requestUuid ?: error("Missing request_uuid from validation request")
    }

    /**
     * Polls the validation request; the does_not_exist answer the server gives until the user
     * opens the e-mailed link maps to a pending `false`. Once a token arrives, the session is
     * stored and the reservation cache syncs.
     */
    override suspend fun confirmEmailValidation(email: String, requestUuid: String): Boolean {
        val apiKey = ensureApiKey()
        val token = try {
            api.account.pollEmailValidation(requestUuid, AffluencesDeviceAuth(apiKey)).authToken
        } catch (cause: Exception) {
            if (cause.isNotValidatedYet()) return false else throw cause
        }
        if (token.isNullOrBlank()) return false
        accountStore.setSession(email, token)
        linkedEmail.value = email
        refreshReservations()
        return true
    }

    override suspend fun logout() {
        accountStore.clearSession()
        linkedEmail.value = null
        reservationDao.clear()
    }

    override fun observeReservations(): Flow<List<LibraryReservation>> =
        reservationDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    /**
     * Mirrors the server's "my reservations" list into the Room cache, leaving cancelled
     * bookings out so they are neither cached nor shown. An expired or rejected token drops the
     * whole session — so the UI prompts a fresh login — and surfaces as
     * [LibraryNotLoggedInException].
     */
    override suspend fun refreshReservations() {
        val auth = accountAuth() ?: throw LibraryNotLoggedInException()
        val list = try {
            api.account.getMyReservations(auth)
        } catch (cause: Exception) {
            if (cause.isSessionExpired()) {
                logout()
                throw LibraryNotLoggedInException()
            }
            throw cause
        }
        reservationDao.replaceAll(
            list.results
                .filterNot { it.state == AffluencesReservationState.Cancelled }
                .map { it.toEntity() }
        )
    }

    /**
     * Cancels through the reservation's own cancellation token; a reservation already gone
     * server-side counts as cancelled. Reservations without a token are left untouched.
     */
    override suspend fun cancelReservation(reservation: LibraryReservation) {
        val token = reservation.cancellationToken ?: return
        runCatching { api.reservations.cancelReservation(token) }
            .onFailure { if (!it.isReservationGone()) throw it }
        runCatching { refreshReservations() }
    }

    /**
     * Validates the presence code against the logged-in e-mail; an invalid-code answer maps to
     * `false`, and a successful validation triggers a best-effort cache re-sync.
     */
    override suspend fun verifyPresence(code: String): Boolean {
        val email = accountStore.email() ?: throw LibraryNotLoggedInException()
        return try {
            api.reservations.validateReservation(AffluencesValidateReservationRequest(validationCode = code, email = email))
            runCatching { refreshReservations() }
            true
        } catch (cause: Exception) {
            if (cause.isInvalidCode()) false else throw cause
        }
    }

    /**
     * Cancels with the reservation token carried by the e-mailed cancellation link; a
     * reservation already gone server-side counts as cancelled.
     */
    override suspend fun cancelByToken(token: String) {
        runCatching { api.reservations.cancelReservation(token) }
            .onFailure { if (!it.isReservationGone()) throw it }
        runCatching { refreshReservations() }
    }

    /** Returns the stored device api key, registering the device via check-in once to mint it. */
    private suspend fun ensureApiKey(): String =
        accountStore.apiKey() ?: run {
            val checkin = api.account.checkIn(AffluencesCheckinRequest(deviceId = accountStore.deviceId(), deviceLang = "it"))
            val key = checkin.apiKey ?: error("Check-in did not return an api key")
            accountStore.setApiKey(key)
            key
        }

    /** The account credentials, once the e-mail validation has stored a session token. */
    private suspend fun accountAuth(): AffluencesAccountAuth? {
        val apiKey = accountStore.apiKey() ?: return null
        val token = accountStore.authToken() ?: return null
        return AffluencesAccountAuth(apiKey = apiKey, authToken = token)
    }

    /** The does_not_exist answer (HTTP 404) given until the e-mailed link is opened. */
    private fun Throwable.isNotValidatedYet(): Boolean =
        (this as? AffluencesException)?.errorCode == AffluencesException.NOT_VALIDATED_YET

    /** An expired or rejected auth token. */
    private fun Throwable.isSessionExpired(): Boolean =
        this is AffluencesException && (statusCode == 401 || errorCode == "missing_auth_token")

    /** A reservation that no longer exists server-side. */
    private fun Throwable.isReservationGone(): Boolean =
        this is AffluencesException && (statusCode == 404 || errorCode?.contains("not_found") == true)

    /** A rejected presence-validation code. */
    private fun Throwable.isInvalidCode(): Boolean =
        (this as? AffluencesException)?.errorCode == AffluencesException.INVALID_VALIDATION_CODE
}
