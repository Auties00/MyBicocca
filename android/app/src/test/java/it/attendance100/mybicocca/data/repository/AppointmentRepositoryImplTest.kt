package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.text.StringResolver
import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationDao
import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationEntity
import it.attendance100.mybicocca.data.mapper.appointment.toDomain
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffPlanningApi
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffPlanningCatalogApi
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffPlanningPortalsApi
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffPlanningReservationsApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningArea
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDetailedService
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningEntryFilters
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldType
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningGeneralSettings
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningIdReference
import it.attendance100.mybicocca.data.easystaff.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortalConfig
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortalSettings
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationDeletion
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationRequest
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import it.attendance100.mybicocca.data.remote.easystaff.exception.EasyStaffPlanningException
import it.attendance100.mybicocca.domain.model.appointment.AppointmentArea
import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Appointment booking policy against the Portale Planning desks: catalog filtering, the
 * two-step create/confirm hold (force-deleting the hold on a confirm failure), the
 * device-local reservation store written on booking, and the prune-on-refresh / cancel
 * cleanup that tolerates a reservation already gone server-side.
 */
class AppointmentRepositoryImplTest {

    private val stringResolver: StringResolver = mockk(relaxed = true)
    private val planningApi: EasyStaffPlanningApi = mockk(relaxed = true)
    private val catalog: EasyStaffPlanningCatalogApi = mockk(relaxed = true)
    private val portals: EasyStaffPlanningPortalsApi = mockk(relaxed = true)
    private val reservations: EasyStaffPlanningReservationsApi = mockk(relaxed = true)
    private val reservationDao: AppointmentReservationDao = mockk(relaxed = true)

    private lateinit var repository: AppointmentRepositoryImpl

    @Before
    fun setUp() {
        every { planningApi.catalog } returns catalog
        every { planningApi.portals } returns portals
        every { planningApi.reservations } returns reservations
        coEvery { portals.getServiceForm(7) } returns wireForm()
        coEvery { portals.getPortal() } returns portalConfig(captchaRequired = false)
        repository = AppointmentRepositoryImpl(stringResolver, planningApi, reservationDao)
    }

    private fun service() = AppointmentService(
        id = 7,
        name = "Ritiro Badge",
        group = "Carriere Studenti",
        descriptionHtml = null,
        durationSeconds = 900,
    )

    private fun offering() = AppointmentOffering(
        area = AppointmentArea(id = 3, code = "U17", name = "Edificio U17", address = "Via X 1"),
        durationSeconds = 900,
        virtual = false,
        descriptionHtml = null,
        minNoticeDays = 1,
        noticeDeadline = null,
        horizonDays = null,
        bookingLimitCount = null,
        bookingLimitIntervalDays = null,
    )

    private fun form() = AppointmentForm(
        fields = listOf(
            AppointmentFormField.Email(
                code = "email",
                label = "Email",
                required = true,
                placeholder = null,
                primary = true,
            ),
            AppointmentFormField.Text(
                code = "nome",
                label = "Nome",
                required = true,
                placeholder = null,
            ),
            AppointmentFormField.Text(
                code = "matricola",
                label = "Matricola",
                required = false,
                placeholder = null,
            ),
            AppointmentFormField.Consent(
                code = "gdpr",
                label = "Consenso",
                required = true,
                policyUrl = null,
            ),
        ),
    )

    private fun wireForm() = listOf(
        wireField("email", EasyStaffPlanningFieldType.Email, EasyStaffPlanningFieldPosition.User, primary = true),
        wireField("nome", EasyStaffPlanningFieldType.Text, EasyStaffPlanningFieldPosition.User),
        wireField("matricola", EasyStaffPlanningFieldType.Text, EasyStaffPlanningFieldPosition.Service),
        wireField("gdpr", EasyStaffPlanningFieldType.Checkbox, EasyStaffPlanningFieldPosition.Gdpr),
    )

    private fun wireField(
        code: String,
        type: EasyStaffPlanningFieldType,
        position: EasyStaffPlanningFieldPosition,
        primary: Boolean = false,
    ) = EasyStaffPlanningFormField(
        id = code.hashCode(),
        label = code,
        code = code,
        type = type,
        portalId = EasyStaffPlanningPortal.INFORMATION_DESKS.id,
        primary = primary,
        position = position,
    )

    private fun portalConfig(captchaRequired: Boolean) = EasyStaffPlanningPortalConfig(
        id = EasyStaffPlanningPortal.INFORMATION_DESKS.id,
        name = "Sportelli",
        code = EasyStaffPlanningPortal.INFORMATION_DESKS.code,
        settings = EasyStaffPlanningPortalSettings(
            general = EasyStaffPlanningGeneralSettings(captchaRequired = captchaRequired),
        ),
    )

    private fun createResult(
        reservationCode: String? = "ABC123",
        entryId: Int? = 555,
    ) = EasyStaffPlanningReservationResult(
        reservationCode = reservationCode,
        entry = entryId?.let(::JsonPrimitive),
        startTimeEpochSeconds = null,
        endTimeEpochSeconds = null,
    )

    @Test
    fun `getServices maps every wire service`() = runTest {
        coEvery { catalog.getServices() } returns listOf(
            EasyStaffPlanningService(id = 1, name = "  Ritiro Badge  ", durationSeconds = 600),
            EasyStaffPlanningService(id = 2, name = "Consegna", durationSeconds = 300),
        )

        val services = repository.getServices()

        assertThat(services).hasSize(2)
        assertThat(services[0].id).isEqualTo(1)
        assertThat(services[0].name).isEqualTo("Ritiro Badge")
    }

    @Test
    fun `getOfferings drops public-hidden areas and picks the requested service`() = runTest {
        val matching = EasyStaffPlanningDetailedService(id = 7, portalId = 2, name = "Badge", durationSeconds = 900)
        val visibleArea = EasyStaffPlanningArea(
            id = 3,
            portalId = 2,
            code = "U17",
            name = "Edificio U17",
            publicHidden = false,
            services = listOf(matching),
        )
        val hiddenArea = EasyStaffPlanningArea(
            id = 4,
            portalId = 2,
            code = "U6",
            name = "Edificio U6",
            publicHidden = true,
            services = listOf(matching),
        )
        val areaWithoutService = EasyStaffPlanningArea(
            id = 5,
            portalId = 2,
            code = "U1",
            name = "Edificio U1",
            publicHidden = false,
            services = emptyList(),
        )
        coEvery { catalog.getAreas(EasyStaffPlanningEntryFilters(service = EasyStaffPlanningIdReference(7))) } returns listOf(visibleArea, hiddenArea, areaWithoutService)

        val offerings = repository.getOfferings(7)

        assertThat(offerings).hasSize(1)
        assertThat(offerings.single().area.id).isEqualTo(3)
    }

    @Test
    fun `bookAppointment without an email value throws`() = runTest {
        val values = emptyMap<String, String>()

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                repository.bookAppointment(
                    service = service(),
                    offering = offering(),
                    form = form(),
                    slotStart = LocalDateTime.of(2026, 6, 15, 10, 0),
                    values = values,
                )
            }
        }
    }

    @Test
    fun `bookAppointment confirms the hold and persists the reservation`() = runTest {
        val request = slot<EasyStaffPlanningReservationRequest>()
        coEvery { reservations.createReservation(capture(request)) } returns createResult()
        coEvery { reservations.confirmReservation(555) } returns EasyStaffPlanningResult()
        val entity = slot<AppointmentReservationEntity>()
        coEvery { reservationDao.upsert(capture(entity)) } just Runs

        val values = mapOf(
            "email" to "mario@campus.unimib.it",
            "nome" to "Mario",
            "matricola" to "123456",
            "gdpr" to "true",
        )
        val reservation = repository.bookAppointment(
            service = service(),
            offering = offering(),
            form = form(),
            slotStart = LocalDateTime.of(2026, 6, 15, 10, 0),
            values = values,
        )

        coVerify(exactly = 1) { reservations.confirmReservation(555) }
        coVerify(exactly = 1) { reservationDao.upsert(any()) }
        assertThat(reservation.code).isEqualTo("ABC123")
        assertThat(reservation.email).isEqualTo("mario@campus.unimib.it")
        assertThat(entity.captured.code).isEqualTo("ABC123")
    }

    @Test
    fun `bookAppointment builds the request from the current form and leaves out the consent`() = runTest {
        val request = slot<EasyStaffPlanningReservationRequest>()
        coEvery { reservations.createReservation(capture(request)) } returns createResult()
        coEvery { reservations.confirmReservation(any()) } returns EasyStaffPlanningResult()

        repository.bookAppointment(
            service = service(),
            offering = offering(),
            form = form(),
            slotStart = LocalDateTime.of(2026, 6, 15, 10, 0),
            values = mapOf(
                "email" to "mario@campus.unimib.it",
                "nome" to "Mario",
                "matricola" to "123456",
                "gdpr" to "true",
            ),
        )

        assertThat(request.captured.userFields)
            .containsExactly("email", "mario@campus.unimib.it", "nome", "Mario")
        assertThat(request.captured.serviceFields).containsExactly("matricola", "123456")
        assertThat(request.captured.primaryValue).isEqualTo("mario@campus.unimib.it")
        assertThat(request.captured.serviceId).isEqualTo(7)
        assertThat(request.captured.areaId).isEqualTo(3)
        assertThat(request.captured.backofficeFields).isEmpty()
    }

    @Test
    fun `bookAppointment refuses portals that require a captcha without creating a hold`() = runTest {
        coEvery { portals.getPortal() } returns portalConfig(captchaRequired = true)

        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                repository.bookAppointment(
                    service = service(),
                    offering = offering(),
                    form = form(),
                    slotStart = LocalDateTime.of(2026, 6, 15, 10, 0),
                    values = mapOf("email" to "mario@campus.unimib.it", "gdpr" to "true"),
                )
            }
        }

        coVerify(exactly = 0) { reservations.createReservation(any()) }
    }

    @Test
    fun `bookAppointment force-deletes the hold and rethrows when confirm fails`() = runTest {
        coEvery { reservations.createReservation(any()) } returns createResult(entryId = 555)
        coEvery { reservations.confirmReservation(555) } throws IOException("boom")
        coEvery {
            reservations.deleteReservation("ABC123", EasyStaffPlanningReservationDeletion(force = true), any())
        } returns EasyStaffPlanningResult()

        assertThrows(IOException::class.java) {
            runBlocking {
                repository.bookAppointment(
                    service = service(),
                    offering = offering(),
                    form = form(),
                    slotStart = LocalDateTime.of(2026, 6, 15, 10, 0),
                    values = mapOf("email" to "mario@campus.unimib.it", "gdpr" to "true"),
                )
            }
        }

        coVerify(exactly = 1) {
            reservations.deleteReservation("ABC123", EasyStaffPlanningReservationDeletion(force = true), "mario@campus.unimib.it")
        }
        coVerify(exactly = 0) { reservationDao.upsert(any()) }
    }

    @Test
    fun `refreshReservations drops long-past reservations without re-validating`() = runTest {
        val past = AppointmentReservationEntity(
            code = "OLD",
            email = "mario@campus.unimib.it",
            entryId = 1,
            serviceId = 7,
            serviceName = "Badge",
            serviceGroup = null,
            areaName = null,
            areaAddress = null,
            startEpochSeconds = LocalDateTime.of(2000, 1, 1, 9, 0)
                .atZone(ZoneId.of("Europe/Rome")).toEpochSecond(),
            endEpochSeconds = LocalDateTime.of(2000, 1, 1, 10, 0)
                .atZone(ZoneId.of("Europe/Rome")).toEpochSecond(),
            qrCodeDataUrl = null,
            webConferenceUrl = null,
            createdAt = 0L,
        )
        coEvery { reservationDao.getAll() } returns listOf(past)

        repository.refreshReservations()

        coVerify(exactly = 1) { reservationDao.delete("OLD") }
        coVerify(exactly = 0) { reservations.getReservation(any(), any()) }
    }

    @Test
    fun `refreshReservations forgets a reservation reported gone by the portal`() = runTest {
        val future = futureEntity("GONE")
        coEvery { reservationDao.getAll() } returns listOf(future)
        coEvery { reservations.getReservation("GONE", any()) } throws EasyStaffPlanningException(404, EasyStaffPlanningException.NOT_FOUND, "not_found")

        repository.refreshReservations()

        coVerify(exactly = 1) { reservationDao.delete("GONE") }
    }

    @Test
    fun `refreshReservations keeps the local copy on an unrelated failure`() = runTest {
        val future = futureEntity("KEEP")
        coEvery { reservationDao.getAll() } returns listOf(future)
        coEvery { reservations.getReservation("KEEP", any()) } throws RuntimeException("server error 500")

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.refreshReservations() }
        }

        coVerify(exactly = 0) { reservationDao.delete("KEEP") }
    }

    @Test
    fun `cancelReservation deletes locally even when the portal reports it gone`() = runTest {
        coEvery {
            reservations.deleteReservation("CODE", EasyStaffPlanningReservationDeletion(), "mario@campus.unimib.it")
        } throws EasyStaffPlanningException(404, EasyStaffPlanningException.NOT_FOUND, "not_found")

        val reservation = futureEntity("CODE").toDomainReservation()
        repository.cancelReservation(reservation)

        coVerify(exactly = 1) { reservationDao.delete("CODE") }
    }

    @Test
    fun `cancelReservation rethrows an unrelated portal failure and skips the local delete`() = runTest {
        coEvery { reservations.deleteReservation("CODE", any(), any()) } throws RuntimeException("503 unavailable")

        assertThrows(RuntimeException::class.java) {
            runBlocking {
                repository.cancelReservation(futureEntity("CODE").toDomainReservation())
            }
        }

        coVerify(exactly = 0) { reservationDao.delete("CODE") }
    }

    private fun futureEntity(code: String) = AppointmentReservationEntity(
        code = code,
        email = "mario@campus.unimib.it",
        entryId = 9,
        serviceId = 7,
        serviceName = "Badge",
        serviceGroup = null,
        areaName = null,
        areaAddress = null,
        startEpochSeconds = LocalDateTime.now().plusDays(7)
            .atZone(ZoneId.of("Europe/Rome")).toEpochSecond(),
        endEpochSeconds = LocalDateTime.now().plusDays(7).plusHours(1)
            .atZone(ZoneId.of("Europe/Rome")).toEpochSecond(),
        qrCodeDataUrl = null,
        webConferenceUrl = null,
        createdAt = 0L,
    )

    private fun AppointmentReservationEntity.toDomainReservation() = toDomain()
}
