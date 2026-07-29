package it.attendance100.mybicocca.data.repository

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationDao
import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationEntity
import it.attendance100.mybicocca.data.mapper.appointment.toDomain
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffPlanningBookingApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningArea
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDetailedService
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationRequest
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import it.attendance100.mybicocca.domain.model.appointment.AppointmentArea
import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormSection
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import kotlinx.coroutines.runBlocking
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

    private val context: Context = mockk(relaxed = true)
    private val easyStaffApi: EasyStaffApi = mockk(relaxed = true)
    private val desks: EasyStaffPlanningBookingApi = mockk(relaxed = true)
    private val reservationDao: AppointmentReservationDao = mockk(relaxed = true)

    private lateinit var repository: AppointmentRepositoryImpl

    @Before
    fun setUp() {
        every { easyStaffApi.planning.informationDesks } returns desks
        every { desks.portal } returns EasyStaffPlanningPortal.INFORMATION_DESKS
        repository = AppointmentRepositoryImpl(context, easyStaffApi, reservationDao)
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
                section = AppointmentFormSection.User,
                primary = true,
            ),
            AppointmentFormField.Text(
                code = "nome",
                label = "Nome",
                required = true,
                placeholder = null,
                section = AppointmentFormSection.User,
            ),
            AppointmentFormField.Text(
                code = "matricola",
                label = "Matricola",
                required = false,
                placeholder = null,
                section = AppointmentFormSection.Service,
            ),
            AppointmentFormField.Consent(
                code = "gdpr",
                label = "Consenso",
                required = true,
                policyUrl = null,
            ),
        ),
    )

    private fun createResult(
        reservationCode: String? = "ABC123",
        entryId: Int? = 555,
    ) = EasyStaffPlanningReservationResult(
        reservationCode = reservationCode,
        entryId = entryId,
        startTimeEpochSeconds = null,
        endTimeEpochSeconds = null,
    )

    @Test
    fun `getServices maps every wire service`() = runTest {
        coEvery { desks.getServices() } returns listOf(
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
        coEvery { desks.getAreas(7) } returns listOf(visibleArea, hiddenArea, areaWithoutService)

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
        coEvery { desks.createReservation(capture(request)) } returns createResult()
        coEvery { desks.confirmReservation(555) } returns EasyStaffPlanningResult()
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

        coVerify(exactly = 1) { desks.confirmReservation(555) }
        coVerify(exactly = 1) { reservationDao.upsert(any()) }
        assertThat(reservation.code).isEqualTo("ABC123")
        assertThat(reservation.email).isEqualTo("mario@campus.unimib.it")
        assertThat(entity.captured.code).isEqualTo("ABC123")
    }

    @Test
    fun `bookAppointment splits values by section and excludes the consent field`() = runTest {
        val request = slot<EasyStaffPlanningReservationRequest>()
        coEvery { desks.createReservation(capture(request)) } returns createResult()
        coEvery { desks.confirmReservation(any()) } returns EasyStaffPlanningResult()

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
    }

    @Test
    fun `bookAppointment force-deletes the hold and rethrows when confirm fails`() = runTest {
        coEvery { desks.createReservation(any()) } returns createResult(entryId = 555)
        coEvery { desks.confirmReservation(555) } throws IOException("boom")
        coEvery { desks.deleteReservation("ABC123", any(), force = true) } returns EasyStaffPlanningResult()

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

        coVerify(exactly = 1) { desks.deleteReservation("ABC123", "mario@campus.unimib.it", force = true) }
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
        coVerify(exactly = 0) { desks.getReservation(any(), any()) }
    }

    @Test
    fun `refreshReservations forgets a reservation reported gone by the portal`() = runTest {
        val future = futureEntity("GONE")
        coEvery { reservationDao.getAll() } returns listOf(future)
        coEvery { desks.getReservation("GONE", any()) } throws RuntimeException("not_found")

        repository.refreshReservations()

        coVerify(exactly = 1) { reservationDao.delete("GONE") }
    }

    @Test
    fun `refreshReservations keeps the local copy on an unrelated failure`() = runTest {
        val future = futureEntity("KEEP")
        coEvery { reservationDao.getAll() } returns listOf(future)
        coEvery { desks.getReservation("KEEP", any()) } throws RuntimeException("server error 500")

        assertThrows(RuntimeException::class.java) {
            runBlocking { repository.refreshReservations() }
        }

        coVerify(exactly = 0) { reservationDao.delete("KEEP") }
    }

    @Test
    fun `cancelReservation deletes locally even when the portal reports it gone`() = runTest {
        coEvery {
            desks.deleteReservation("CODE", "mario@campus.unimib.it", false)
        } throws RuntimeException("not_found")

        val reservation = futureEntity("CODE").toDomainReservation()
        repository.cancelReservation(reservation)

        coVerify(exactly = 1) { reservationDao.delete("CODE") }
    }

    @Test
    fun `cancelReservation rethrows an unrelated portal failure and skips the local delete`() = runTest {
        coEvery { desks.deleteReservation("CODE", any(), any()) } throws RuntimeException("503 unavailable")

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
