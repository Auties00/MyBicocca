package it.attendance100.mybicocca.data.mapper.appointment

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningArea
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDaySchedule
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDetailedService
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldOption
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldType
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningMonthSchedule
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningSlotAvailability
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningTimeRange
import it.attendance100.mybicocca.domain.model.appointment.AppointmentArea
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormSection
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Covers the Portale Planning appointment mappers: service/area/offering field mapping, month
 * availability (empty days filtered, days sorted), day slots (available iff count > 0, sorted),
 * the dynamic form (blocked fields dropped, type variants, consent policy-URL resolution), the
 * reservation build (required code, time fallbacks) and the cache round-trip.
 */
class AppointmentMapperTest {

    @Test
    fun `service maps and trims its scalar fields`() {
        val service = EasyStaffPlanningService(
            id = 3,
            name = "  Sportello  ",
            durationSeconds = 1800,
            description = "<p>desc</p>",
            group = "  Carriere  ",
        ).toAppointmentService()
        assertThat(service.id).isEqualTo(3)
        assertThat(service.name).isEqualTo("Sportello")
        assertThat(service.group).isEqualTo("Carriere")
        assertThat(service.descriptionHtml).isEqualTo("<p>desc</p>")
        assertThat(service.durationSeconds).isEqualTo(1800)
    }

    @Test
    fun `service blanks an empty group`() {
        val service = EasyStaffPlanningService(id = 1, name = "S", durationSeconds = 60, group = "   ")
            .toAppointmentService()
        assertThat(service.group).isNull()
    }

    @Test
    fun `area maps and trims code name and address`() {
        val area = EasyStaffPlanningArea(
            id = 2,
            portalId = 1,
            code = " U17 ",
            name = " Edificio U17 ",
            address = "  ",
        ).toAppointmentArea()
        assertThat(area.code).isEqualTo("U17")
        assertThat(area.name).isEqualTo("Edificio U17")
        assertThat(area.address).isNull()
    }

    @Test
    fun `offering pulls booking constraints from the detailed service`() {
        val area = EasyStaffPlanningArea(id = 1, portalId = 1, code = "U1", name = "Area")
        val service = EasyStaffPlanningDetailedService(
            id = 1,
            portalId = 1,
            name = "Service",
            durationSeconds = 900,
            virtual = true,
            addDaysAhead = 2,
            addDeadline = LocalTime.of(12, 0),
            maxRangeDays = 30,
            bookingLimitCount = 1,
            bookingLimitIntervalDays = 7,
            description = "<b>x</b>",
        )
        val offering = toAppointmentOffering(area, service)
        assertThat(offering.durationSeconds).isEqualTo(900)
        assertThat(offering.virtual).isTrue()
        assertThat(offering.minNoticeDays).isEqualTo(2)
        assertThat(offering.noticeDeadline).isEqualTo(LocalTime.of(12, 0))
        assertThat(offering.horizonDays).isEqualTo(30)
        assertThat(offering.bookingLimitCount).isEqualTo(1)
        assertThat(offering.bookingLimitIntervalDays).isEqualTo(7)
        assertThat(offering.descriptionHtml).isEqualTo("<b>x</b>")
    }

    @Test
    fun `month availability filters empty days and sorts the remaining`() {
        val range = EasyStaffPlanningTimeRange(LocalTime.of(9, 0), LocalTime.of(9, 30))
        val schedule = EasyStaffPlanningMonthSchedule(
            firstAvailable = LocalDateTime.of(2024, 6, 5, 9, 0),
            days = mapOf(
                LocalDate.of(2024, 6, 10) to listOf(range),
                LocalDate.of(2024, 6, 3) to listOf(range),
                LocalDate.of(2024, 6, 7) to emptyList(),
            ),
        )
        val availability = schedule.toMonthAvailability()
        assertThat(availability.firstAvailable).isEqualTo(LocalDateTime.of(2024, 6, 5, 9, 0))
        assertThat(availability.availableDays).containsExactly(
            LocalDate.of(2024, 6, 3),
            LocalDate.of(2024, 6, 10),
        ).inOrder()
    }

    @Test
    fun `day slots are available when free count is positive and sorted by start`() {
        val early = EasyStaffPlanningTimeRange(LocalTime.of(9, 0), LocalTime.of(9, 30))
        val late = EasyStaffPlanningTimeRange(LocalTime.of(11, 0), LocalTime.of(11, 30))
        val schedule = EasyStaffPlanningDaySchedule(
            days = mapOf(
                LocalDate.of(2024, 6, 5) to linkedMapOf(
                    late to EasyStaffPlanningSlotAvailability(availableCount = 0, totalCount = 3),
                    early to EasyStaffPlanningSlotAvailability(availableCount = 2, totalCount = 3),
                ),
            ),
        )
        val slots = schedule.toAppointmentSlots()
        assertThat(slots).hasSize(2)
        assertThat(slots[0].start).isEqualTo(LocalTime.of(9, 0))
        assertThat(slots[0].available).isTrue()
        assertThat(slots[1].start).isEqualTo(LocalTime.of(11, 0))
        assertThat(slots[1].available).isFalse()
    }

    @Test
    fun `form drops blocked fields`() {
        val fields = listOf(
            formField(code = "blocked", blocked = true),
            formField(code = "visible", blocked = false),
        )
        val form = fields.toAppointmentForm()
        assertThat(form.fields).hasSize(1)
        assertThat(form.fields.first().code).isEqualTo("visible")
    }

    @Test
    fun `email field maps to Email with its primary flag and section`() {
        val field = formField(
            code = "email",
            type = EasyStaffPlanningFieldType.Email,
            primary = true,
            position = EasyStaffPlanningFieldPosition.User,
        ).let { listOf(it) }.toAppointmentForm().fields.first()
        assertThat(field).isInstanceOf(AppointmentFormField.Email::class.java)
        field as AppointmentFormField.Email
        assertThat(field.primary).isTrue()
        assertThat(field.section).isEqualTo(AppointmentFormSection.User)
    }

    @Test
    fun `service-positioned field carries the Service section`() {
        val field = listOf(
            formField(
                code = "note",
                type = EasyStaffPlanningFieldType.TextArea,
                position = EasyStaffPlanningFieldPosition.Service,
            ),
        ).toAppointmentForm().fields.first()
        assertThat(field).isInstanceOf(AppointmentFormField.TextArea::class.java)
        assertThat(field.section).isEqualTo(AppointmentFormSection.Service)
    }

    @Test
    fun `phone field maps to Phone`() {
        val field = listOf(formField(code = "tel", type = EasyStaffPlanningFieldType.Phone))
            .toAppointmentForm().fields.first()
        assertThat(field).isInstanceOf(AppointmentFormField.Phone::class.java)
    }

    @Test
    fun `select field maps its options`() {
        val field = listOf(
            formField(
                code = "area",
                type = EasyStaffPlanningFieldType.Select,
                values = listOf(
                    EasyStaffPlanningFieldOption(value = "PSI", label = "Area Psicologica"),
                    EasyStaffPlanningFieldOption(value = "MED", label = "Area Medica"),
                ),
            ),
        ).toAppointmentForm().fields.first()
        assertThat(field).isInstanceOf(AppointmentFormField.Select::class.java)
        field as AppointmentFormField.Select
        assertThat(field.options.map { it.value }).containsExactly("PSI", "MED").inOrder()
        assertThat(field.options.first().label).isEqualTo("Area Psicologica")
    }

    @Test
    fun `unknown input type degrades to a plain text field`() {
        val field = listOf(formField(code = "x", type = EasyStaffPlanningFieldType.Other("captcha")))
            .toAppointmentForm().fields.first()
        assertThat(field).isInstanceOf(AppointmentFormField.Text::class.java)
    }

    @Test
    fun `gdpr-positioned field becomes consent and resolves a relative policy url`() {
        val field = listOf(
            formField(
                code = "gdpr",
                type = EasyStaffPlanningFieldType.Text,
                position = EasyStaffPlanningFieldPosition.Gdpr,
                values = listOf(EasyStaffPlanningFieldOption(value = "privacy/policy.pdf", label = "Privacy")),
            ),
        ).toAppointmentForm().fields.first()
        assertThat(field).isInstanceOf(AppointmentFormField.Consent::class.java)
        field as AppointmentFormField.Consent
        assertThat(field.policyUrl).isEqualTo(
            "https://gestioneorari.didattica.unimib.it/portaleplanningAPI/storage/privacy/policy.pdf",
        )
    }

    @Test
    fun `checkbox type becomes consent keeping an absolute policy url`() {
        val field = listOf(
            formField(
                code = "consent",
                type = EasyStaffPlanningFieldType.Checkbox,
                values = listOf(EasyStaffPlanningFieldOption(value = "https://privacy.example", label = "P")),
            ),
        ).toAppointmentForm().fields.first()
        assertThat(field).isInstanceOf(AppointmentFormField.Consent::class.java)
        field as AppointmentFormField.Consent
        assertThat(field.policyUrl).isEqualTo("https://privacy.example")
    }

    @Test
    fun `reservation prefers the backend echoed times`() {
        val result = EasyStaffPlanningReservationResult(
            reservationCode = "RC1",
            entryId = 99,
            startTimeEpochSeconds = LocalDateTime.of(2024, 6, 5, 10, 0).atZone(CampusZone).toEpochSecond(),
            endTimeEpochSeconds = LocalDateTime.of(2024, 6, 5, 10, 30).atZone(CampusZone).toEpochSecond(),
            qrCode = "data:image/png;base64,AAA",
            webConferenceUrl = "https://meet.example",
        )
        val reservation = toAppointmentReservation(
            result = result,
            service = service(),
            offering = offering(),
            slotStart = LocalDateTime.of(2024, 6, 5, 9, 0),
            durationSeconds = 1800,
            email = "me@example.com",
        )
        assertThat(reservation.code).isEqualTo("RC1")
        assertThat(reservation.start).isEqualTo(LocalDateTime.of(2024, 6, 5, 10, 0))
        assertThat(reservation.end).isEqualTo(LocalDateTime.of(2024, 6, 5, 10, 30))
        assertThat(reservation.qrCodeDataUrl).isEqualTo("data:image/png;base64,AAA")
        assertThat(reservation.webConferenceUrl).isEqualTo("https://meet.example")
        assertThat(reservation.entryId).isEqualTo(99)
    }

    @Test
    fun `reservation falls back to the picked slot and computed end when backend omits times`() {
        val result = EasyStaffPlanningReservationResult(reservationCode = "RC2")
        val reservation = toAppointmentReservation(
            result = result,
            service = service(),
            offering = offering(),
            slotStart = LocalDateTime.of(2024, 6, 5, 9, 0),
            durationSeconds = 1800,
            email = "me@example.com",
        )
        assertThat(reservation.start).isEqualTo(LocalDateTime.of(2024, 6, 5, 9, 0))
        assertThat(reservation.end).isEqualTo(LocalDateTime.of(2024, 6, 5, 9, 30))
    }

    @Test
    fun `reservation requires a reservation code`() {
        val result = EasyStaffPlanningReservationResult(reservationCode = null)
        assertThrows(IllegalArgumentException::class.java) {
            toAppointmentReservation(
                result = result,
                service = service(),
                offering = offering(),
                slotStart = LocalDateTime.of(2024, 6, 5, 9, 0),
                durationSeconds = 1800,
                email = "me@example.com",
            )
        }
    }

    @Test
    fun `reservation blanks an empty qr and web conference url`() {
        val result = EasyStaffPlanningReservationResult(reservationCode = "RC3", qrCode = "  ", webConferenceUrl = "")
        val reservation = toAppointmentReservation(
            result = result,
            service = service(),
            offering = offering(),
            slotStart = LocalDateTime.of(2024, 6, 5, 9, 0),
            durationSeconds = 600,
            email = "me@example.com",
        )
        assertThat(reservation.qrCodeDataUrl).isNull()
        assertThat(reservation.webConferenceUrl).isNull()
    }

    @Test
    fun `reservation round-trips through the cache entity`() {
        val reservation = toAppointmentReservation(
            result = EasyStaffPlanningReservationResult(reservationCode = "RC4", entryId = 5),
            service = service(),
            offering = offering(),
            slotStart = LocalDateTime.of(2024, 6, 5, 9, 0),
            durationSeconds = 1800,
            email = "me@example.com",
        )
        val entity = reservation.toEntity(createdAt = 12345L)
        assertThat(entity.code).isEqualTo("RC4")
        assertThat(entity.createdAt).isEqualTo(12345L)
        assertThat(entity.toDomain()).isEqualTo(reservation)
    }

    private fun service() = AppointmentService(
        id = 7,
        name = "Sportello Carriere",
        group = "Carriere Studenti",
        descriptionHtml = null,
        durationSeconds = 1800,
    )

    private fun offering() = AppointmentOffering(
        area = AppointmentArea(id = 1, code = "U17", name = "Edificio U17", address = "Via 1"),
        durationSeconds = 1800,
        virtual = false,
        descriptionHtml = null,
        minNoticeDays = 1,
        noticeDeadline = null,
        horizonDays = 30,
        bookingLimitCount = null,
        bookingLimitIntervalDays = null,
    )

    private fun formField(
        code: String,
        type: EasyStaffPlanningFieldType = EasyStaffPlanningFieldType.Text,
        blocked: Boolean = false,
        primary: Boolean = false,
        required: Boolean = false,
        position: EasyStaffPlanningFieldPosition = EasyStaffPlanningFieldPosition.User,
        values: List<EasyStaffPlanningFieldOption>? = null,
        placeholder: String? = null,
    ) = EasyStaffPlanningFormField(
        id = 1,
        label = "Label",
        code = code,
        type = type,
        values = values,
        placeholder = placeholder,
        primary = primary,
        required = required,
        portalId = 1,
        blocked = blocked,
        position = position,
    )
}
