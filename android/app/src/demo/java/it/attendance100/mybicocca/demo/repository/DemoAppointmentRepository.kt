package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.demo.DemoCareer
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.appointment.AppointmentArea
import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormOption
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormSection
import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import it.attendance100.mybicocca.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Three bookable offices with weekday morning slots, and one reservation already made: the
 * segreteria appointment the demo calendar shows for tomorrow.
 */
@Singleton
class DemoAppointmentRepository @Inject constructor() : AppointmentRepository {

    private val today: LocalDate = DemoClock.now.toLocalDate()

    private val services = listOf(
        AppointmentService(1, "Segreteria studenti", "Segreterie studenti", "<p>Immatricolazioni, iscrizioni, certificati e carriera.</p>", 20 * 60),
        AppointmentService(2, "Ufficio tasse e contributi", "Segreterie studenti", "<p>Rate, esoneri, rimborsi e ISEE.</p>", 15 * 60),
        AppointmentService(3, "Ufficio mobilità internazionale", "Internazionalizzazione", "<p>Erasmus+, Exchange e doppie lauree.</p>", 30 * 60),
    )

    private val area = AppointmentArea(id = 11, code = "U17", name = "Edificio U17", address = "Piazzetta Difesa per le Donne, Milano")

    private val reservations = MutableStateFlow(
        listOf(
            AppointmentReservation(
                code = "BK-48213",
                email = DemoStudent.USERNAME,
                entryId = 48213,
                serviceId = 1,
                serviceName = "Segreteria studenti",
                serviceGroup = "Segreterie studenti",
                areaName = "Sportello 3",
                areaAddress = "U17",
                start = today.plusDays(1).atTime(14, 0),
                end = today.plusDays(1).atTime(14, 20),
                qrCodeDataUrl = null,
                webConferenceUrl = null,
            ),
        ),
    )

    override suspend fun getServices(): List<AppointmentService> = services

    override suspend fun getOfferings(serviceId: Int): List<AppointmentOffering> {
        val service = services.first { it.id == serviceId }
        return listOf(
            AppointmentOffering(
                area = area,
                durationSeconds = service.durationSeconds,
                virtual = false,
                descriptionHtml = "<p>Presentarsi allo sportello cinque minuti prima con un documento.</p>",
                minNoticeDays = 1,
                noticeDeadline = null,
                horizonDays = 30,
                bookingLimitCount = 2,
                bookingLimitIntervalDays = 30,
            ),
        )
    }

    override suspend fun getMonthAvailability(
        serviceId: Int,
        areaId: Int,
        month: YearMonth,
        durationSeconds: Int,
    ): AppointmentMonthAvailability {
        val days = (1..month.lengthOfMonth()).map(month::atDay).filter(::isBookable)
        return AppointmentMonthAvailability(firstAvailable = days.firstOrNull()?.atTime(9, 0), availableDays = days)
    }

    override suspend fun getDaySlots(serviceId: Int, areaId: Int, date: LocalDate, durationSeconds: Int): List<AppointmentSlot> {
        if (!isBookable(date)) return emptyList()
        val minutes = durationSeconds / 60L
        return generateSequence(LocalTime.of(9, 0)) { it.plusMinutes(minutes) }
            .takeWhile { it < LocalTime.of(12, 0) }
            .mapIndexed { index, start -> AppointmentSlot(start, start.plusMinutes(minutes), available = (index + date.dayOfMonth) % 3 != 0) }
            .toList()
    }

    override suspend fun getBookingForm(serviceId: Int): AppointmentForm = AppointmentForm(
        fields = listOf(
            AppointmentFormField.Email("email", "E-mail", required = true, placeholder = null, section = AppointmentFormSection.User, primary = true),
            AppointmentFormField.Text("matricola", "Matricola", required = true, placeholder = null, section = AppointmentFormSection.User),
            AppointmentFormField.Select(
                "motivo", "Motivo della richiesta", required = true, placeholder = null, section = AppointmentFormSection.Service,
                options = listOf("Iscrizione", "Certificati", "Piano di studi", "Altro").map { AppointmentFormOption(it.lowercase(), it) },
            ),
            AppointmentFormField.TextArea("note", "Note", required = false, placeholder = "Descrivi brevemente la richiesta", section = AppointmentFormSection.Service),
            AppointmentFormField.Consent("privacy", "Ho letto l'informativa sulla privacy", required = true, policyUrl = null),
        ),
    )

    override suspend fun bookAppointment(
        service: AppointmentService,
        offering: AppointmentOffering,
        form: AppointmentForm,
        slotStart: LocalDateTime,
        values: Map<String, String>,
    ): AppointmentReservation {
        val entryId = 48300 + reservations.value.size
        val reservation = AppointmentReservation(
            code = "BK-$entryId",
            email = values["email"] ?: DemoStudent.USERNAME,
            entryId = entryId,
            serviceId = service.id,
            serviceName = service.name,
            serviceGroup = service.group,
            areaName = offering.area.name,
            areaAddress = offering.area.address,
            start = slotStart,
            end = slotStart.plusSeconds(offering.durationSeconds.toLong()),
            qrCodeDataUrl = null,
            webConferenceUrl = null,
        )
        reservations.update { it + reservation }
        return reservation
    }

    override fun observeReservations(): Flow<List<AppointmentReservation>> = reservations

    override suspend fun refreshReservations() = Unit

    override suspend fun cancelReservation(reservation: AppointmentReservation) {
        reservations.update { all -> all.filterNot { it.code == reservation.code } }
    }

    override suspend fun getReservationPdf(entryId: Int): ByteArray {
        val reservation = reservations.value.firstOrNull { it.entryId == entryId }
        return DemoCareer.pdf(
            "Promemoria appuntamento",
            listOfNotNull(reservation?.serviceName, reservation?.let { "${it.start.toLocalDate()} alle ${it.start.toLocalTime()}" }, reservation?.areaName),
        )
    }

    private fun isBookable(date: LocalDate): Boolean =
        date.isAfter(today) && date.isBefore(today.plusDays(31)) && date.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
}
