package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationDao
import it.attendance100.mybicocca.data.mapper.appointment.CampusZone
import it.attendance100.mybicocca.data.mapper.appointment.toAppointmentForm
import it.attendance100.mybicocca.data.mapper.appointment.toAppointmentOffering
import it.attendance100.mybicocca.data.mapper.appointment.toAppointmentReservation
import it.attendance100.mybicocca.data.mapper.appointment.toAppointmentService
import it.attendance100.mybicocca.data.mapper.appointment.toAppointmentSlots
import it.attendance100.mybicocca.data.mapper.appointment.toDomain
import it.attendance100.mybicocca.data.mapper.appointment.toEntity
import it.attendance100.mybicocca.data.mapper.appointment.toMonthAvailability
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationRequest
import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormSection
import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Booking against the Portale Planning information-desks API (reached through the EasyStaff
 * client). Catalog, availability and forms are read live; the only persistent state is the
 * device-local Room reservation store, written on booking and pruned on refresh/cancel.
 */
@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val easyStaffApi: EasyStaffApi,
    private val reservationDao: AppointmentReservationDao,
) : AppointmentRepository {

    private val desks get() = easyStaffApi.planning.informationDesks

    override suspend fun getServices(): List<AppointmentService> =
        desks.getServices().map { it.toAppointmentService() }

    /**
     * Lists the publicly visible areas offering the service. The filtered area listing embeds
     * the constraints of every service bookable at each area, so the requested service's entry
     * is picked out of it.
     */
    override suspend fun getOfferings(serviceId: Int): List<AppointmentOffering> =
        desks.getAreas(serviceId)
            .filterNot { it.publicHidden }
            .mapNotNull { area ->
                area.services.firstOrNull { it.id == serviceId }
                    ?.let { toAppointmentOffering(area, it) }
            }

    override suspend fun getMonthAvailability(
        serviceId: Int,
        areaId: Int,
        month: YearMonth,
        durationSeconds: Int,
    ): AppointmentMonthAvailability =
        desks.getMonthSchedule(serviceId, areaId, month, durationSeconds).toMonthAvailability()

    override suspend fun getDaySlots(
        serviceId: Int,
        areaId: Int,
        date: LocalDate,
        durationSeconds: Int,
    ): List<AppointmentSlot> =
        desks.getDaySchedule(serviceId, areaId, date, durationSeconds).toAppointmentSlots()

    override suspend fun getBookingForm(serviceId: Int): AppointmentForm =
        desks.getServiceForm(serviceId).toAppointmentForm()

    /**
     * Books in two steps with hold semantics: the create call places a provisional hold, the
     * confirm call finalizes it. The user already reviewed everything in-app, so the hold is
     * confirmed immediately; if confirming fails the hold is force-deleted so the slot is not
     * left blocked. The finalized reservation is then persisted in the device-local store.
     */
    override suspend fun bookAppointment(
        service: AppointmentService,
        offering: AppointmentOffering,
        form: AppointmentForm,
        slotStart: LocalDateTime,
        values: Map<String, String>,
    ): AppointmentReservation {
        val email = form.primaryField?.code?.let { values[it] }
        require(!email.isNullOrBlank()) { "The booking email is required" }

        val slotEnd = slotStart.plusSeconds(offering.durationSeconds.toLong())
        val result = desks.createReservation(
            EasyStaffPlanningReservationRequest(
                reservationNumber = 0,
                portalCode = desks.portal.code,
                startTimeEpochSeconds = slotStart.atZone(CampusZone).toEpochSecond(),
                endTimeEpochSeconds = slotEnd.atZone(CampusZone).toEpochSecond(),
                durationSeconds = offering.durationSeconds,
                serviceId = service.id,
                areaId = offering.area.id,
                primaryValue = email,
                userFields = sectionValues(form, values, AppointmentFormSection.User),
                serviceFields = sectionValues(form, values, AppointmentFormSection.Service),
                resourceId = null,
                recaptchaToken = null,
                timeZone = CampusZone.id,
            )
        )

        val entryId = result.entryId
        if (entryId != null) {
            try {
                desks.confirmReservation(entryId)
            } catch (cause: Exception) {
                result.reservationCode?.let { code ->
                    runCatching { desks.deleteReservation(code, email, force = true) }
                }
                throw cause
            }
        }

        val reservation = toAppointmentReservation(
            result = result,
            service = service,
            offering = offering,
            slotStart = slotStart,
            durationSeconds = offering.durationSeconds,
            email = email,
        )
        reservationDao.upsert(reservation.toEntity(createdAt = System.currentTimeMillis()))
        return reservation
    }

    override fun observeReservations(): Flow<List<AppointmentReservation>> =
        reservationDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    /**
     * Prunes the local store: long-past appointments are dropped without re-validation (the
     * portal keeps no useful state for them and the list should only show what's actionable),
     * the rest are looked up on the portal and forgotten locally when cancelled elsewhere or
     * never finalized. Any other failure (network, server hiccup) keeps the local copy.
     */
    override suspend fun refreshReservations() {
        val now = LocalDateTime.now(CampusZone)
        for (entity in reservationDao.getAll()) {
            val reservation = entity.toDomain()
            if (reservation.end.isBefore(now.minusDays(1))) {
                reservationDao.delete(reservation.code)
                continue
            }
            try {
                desks.getReservation(reservation.code, reservation.email)
            } catch (cause: Exception) {
                if (!cause.isReservationGone()) throw cause
                reservationDao.delete(reservation.code)
            }
        }
    }

    /**
     * Deletes the booking on the portal (authorized by code + email) and then locally. A
     * booking already gone server-side still proceeds with the local cleanup.
     */
    override suspend fun cancelReservation(reservation: AppointmentReservation) {
        try {
            desks.deleteReservation(reservation.code, reservation.email)
        } catch (cause: Exception) {
            if (!cause.isReservationGone()) throw cause
        }
        reservationDao.delete(reservation.code)
    }

    override suspend fun getReservationPdf(entryId: Int): ByteArray =
        desks.getReservationPdf(entryId)

    /**
     * Detects "this reservation does not exist on the portal". The data-api exception types
     * aren't on :app's compile classpath, so — same as SessionManager — the stable error key is
     * sniffed rather than type-checked: the planning ApiRequestException carries the backend
     * key ("not_found" from the manage endpoint, "reservation_not_found" from the info
     * endpoint) as its message.
     */
    private fun Throwable.isReservationGone(): Boolean {
        val message = message?.lowercase().orEmpty()
        return "not_found" in message
    }

    /**
     * Collects the non-blank values of one form section, keyed by field code. The GDPR consent
     * is validated client-side but never submitted.
     */
    private fun sectionValues(
        form: AppointmentForm,
        values: Map<String, String>,
        section: AppointmentFormSection,
    ): Map<String, String> =
        form.fields.asSequence()
            .filterNot { it is AppointmentFormField.Consent }
            .filter { it.section == section }
            .mapNotNull { field -> values[field.code]?.takeIf { it.isNotBlank() }?.let { field.code to it } }
            .toMap()
}
