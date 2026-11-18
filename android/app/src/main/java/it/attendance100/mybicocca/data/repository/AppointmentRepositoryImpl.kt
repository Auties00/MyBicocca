package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.text.StringResolver
import it.attendance100.mybicocca.core.time.AppClock
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
import it.attendance100.mybicocca.data.mapper.appointment.toReservationRequest
import it.attendance100.mybicocca.data.remote.easystaff.api.EasyStaffPlanningApi
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningEntryFilters
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningIdReference
import it.attendance100.mybicocca.data.easystaff.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationDeletion
import it.attendance100.mybicocca.data.easystaff.entryId
import it.attendance100.mybicocca.data.remote.easystaff.exception.EasyStaffPlanningException
import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
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
    private val stringResolver: StringResolver,
    private val desks: EasyStaffPlanningApi,
    private val reservationDao: AppointmentReservationDao,
) : AppointmentRepository {


    override suspend fun getServices(): List<AppointmentService> =
        desks.catalog.getServices().map { it.toAppointmentService() }

    /**
     * Lists the publicly visible areas offering the service. The filtered area listing embeds
     * the constraints of every service bookable at each area, so the requested service's entry
     * is picked out of it.
     */
    override suspend fun getOfferings(serviceId: Int): List<AppointmentOffering> =
        desks.catalog.getAreas(EasyStaffPlanningEntryFilters(service = EasyStaffPlanningIdReference(serviceId)))
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
        desks.availability.getMonthSchedule(
            serviceId = serviceId,
            month = month.toString(),
            areaId = areaId,
            durationSeconds = durationSeconds,
        ).toMonthAvailability()

    override suspend fun getDaySlots(
        serviceId: Int,
        areaId: Int,
        date: LocalDate,
        durationSeconds: Int,
    ): List<AppointmentSlot> =
        desks.availability.getDaySchedule(
            serviceId = serviceId,
            date = date,
            areaId = areaId,
            durationSeconds = durationSeconds,
        ).toAppointmentSlots()

    override suspend fun getBookingForm(serviceId: Int): AppointmentForm =
        desks.portals.getServiceForm(serviceId).toAppointmentForm()

    /**
     * Books in two steps with hold semantics: the create call places a provisional hold, the
     * confirm call finalizes it. The request is built from the service's current form, which
     * splits the values by field position; portals that require a reCAPTCHA are refused up
     * front, since the app can't produce one. The user already reviewed everything in-app, so
     * the hold is confirmed immediately; if confirming fails the hold is force-deleted so the
     * slot is not left blocked. The finalized reservation is then persisted in the device-local
     * store.
     */
    override suspend fun bookAppointment(
        service: AppointmentService,
        offering: AppointmentOffering,
        form: AppointmentForm,
        slotStart: LocalDateTime,
        values: Map<String, String>,
    ): AppointmentReservation {
        val email = form.primaryField?.code?.let { values[it] }
        require(!email.isNullOrBlank()) { stringResolver.getString(it.attendance100.mybicocca.R.string.appointment_booking_email_required) }

        val (formFields, portal) = coroutineScope {
            val formFields = async { desks.portals.getServiceForm(service.id) }
            val portal = async { desks.portals.getPortal() }
            formFields.await() to portal.await()
        }
        check(portal.settings?.general?.captchaRequired != true) {
            stringResolver.getString(it.attendance100.mybicocca.R.string.appointment_booking_captcha_required)
        }
        val result = desks.reservations.createReservation(
            formFields.toReservationRequest(
                portal = EasyStaffPlanningPortal.INFORMATION_DESKS,
                serviceId = service.id,
                areaId = offering.area.id,
                slotStart = slotStart,
                durationSeconds = offering.durationSeconds,
                values = values,
            )
        )

        val entryId = result.entryId
        if (entryId != null) {
            try {
                desks.reservations.confirmReservation(entryId)
            } catch (cause: Exception) {
                result.reservationCode?.let { code ->
                    runCatching {
                        desks.reservations.deleteReservation(
                            reservationCode = code,
                            body = EasyStaffPlanningReservationDeletion(force = true),
                            primaryValue = email,
                        )
                    }
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
        val now = LocalDateTime.now(AppClock.clock.withZone(CampusZone))
        for (entity in reservationDao.getAll()) {
            val reservation = entity.toDomain()
            if (reservation.end.isBefore(now.minusDays(1))) {
                reservationDao.delete(reservation.code)
                continue
            }
            try {
                desks.reservations.getReservation(reservation.code, reservation.email)
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
            desks.reservations.deleteReservation(
                reservationCode = reservation.code,
                body = EasyStaffPlanningReservationDeletion(),
                primaryValue = reservation.email,
            )
        } catch (cause: Exception) {
            if (!cause.isReservationGone()) throw cause
        }
        reservationDao.delete(reservation.code)
    }

    override suspend fun getReservationPdf(entryId: Int): ByteArray {
        val channel = desks.reservations.getReservationPdf(entryId)
        return withContext(Dispatchers.IO) { channel.toInputStream().use { it.readBytes() } }
    }

    /**
     * Detects "this reservation does not exist on the portal": the manage endpoint reports it as
     * `not_found`, the info endpoint as `reservation_not_found`.
     */
    private fun Throwable.isReservationGone(): Boolean =
        this is EasyStaffPlanningException &&
            (errorCode == EasyStaffPlanningException.NOT_FOUND || errorCode == EasyStaffPlanningException.RESERVATION_NOT_FOUND)

    /**
     * Collects the non-blank values of one form section, keyed by field code. The GDPR consent
     * is validated client-side but never submitted.
     */}
