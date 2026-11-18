package it.attendance100.mybicocca.data.mapper.appointment

import it.attendance100.mybicocca.data.local.appointment.AppointmentReservationEntity
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningArea
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDaySchedule
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDetailedService
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldType
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningMonthSchedule
import it.attendance100.mybicocca.data.easystaff.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationRequest
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import it.attendance100.mybicocca.data.easystaff.entryId
import it.attendance100.mybicocca.data.easystaff.options
import it.attendance100.mybicocca.data.easystaff.slots
import it.attendance100.mybicocca.domain.model.appointment.AppointmentArea
import it.attendance100.mybicocca.domain.model.appointment.AppointmentForm
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormField
import it.attendance100.mybicocca.domain.model.appointment.AppointmentFormOption
import it.attendance100.mybicocca.domain.model.appointment.AppointmentMonthAvailability
import it.attendance100.mybicocca.domain.model.appointment.AppointmentOffering
import it.attendance100.mybicocca.domain.model.appointment.AppointmentReservation
import it.attendance100.mybicocca.domain.model.appointment.AppointmentService
import it.attendance100.mybicocca.domain.model.appointment.AppointmentSlot
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Time zone appointment times are expressed in: they are local to the campuses, while the
 * Portale Planning backend speaks epoch seconds.
 */
val CampusZone: ZoneId = ZoneId.of("Europe/Rome")

/** Storage root that consent-info documents may be referenced relative to. */
private const val PolicyStorageBase = "https://gestioneorari.didattica.unimib.it/portaleplanningAPI/storage/"

fun EasyStaffPlanningService.toAppointmentService() = AppointmentService(
    id = id,
    name = name.trim(),
    group = group?.trim()?.takeIf { it.isNotEmpty() },
    descriptionHtml = description?.takeIf { it.isNotBlank() },
    durationSeconds = durationSeconds,
)

fun EasyStaffPlanningArea.toAppointmentArea() = AppointmentArea(
    id = id,
    code = code.trim(),
    name = name.trim(),
    address = address?.trim()?.takeIf { it.isNotEmpty() },
)

fun toAppointmentOffering(
    area: EasyStaffPlanningArea,
    service: EasyStaffPlanningDetailedService,
) = AppointmentOffering(
    area = area.toAppointmentArea(),
    durationSeconds = service.durationSeconds,
    virtual = service.virtual,
    descriptionHtml = service.description?.takeIf { it.isNotBlank() },
    minNoticeDays = service.addDaysAhead,
    noticeDeadline = service.addDeadline,
    horizonDays = service.maxRangeDays,
    bookingLimitCount = service.bookingLimitCount,
    bookingLimitIntervalDays = service.bookingLimitIntervalDays,
)

fun EasyStaffPlanningMonthSchedule.toMonthAvailability() = AppointmentMonthAvailability(
    firstAvailable = firstAvailable,
    availableDays = days.filterValues { it.isNotEmpty() }.keys.sorted(),
)

fun EasyStaffPlanningDaySchedule.toAppointmentSlots(): List<AppointmentSlot> =
    slots.entries
        .map { (range, availability) ->
            AppointmentSlot(
                start = range.start,
                end = range.end,
                available = availability.availableCount > 0,
            )
        }
        .sortedBy { it.start }

/**
 * Builds the domain form, dropping blocked fields: those are read-only values filled from a
 * logged-in profile, and the anonymous flow has none to show.
 */
fun List<EasyStaffPlanningFormField>.toAppointmentForm() = AppointmentForm(
    fields = filterNot { it.blocked }.map { it.toAppointmentFormField() },
)

/**
 * Maps one wire field to its typed domain counterpart. GDPR-positioned fields and checkboxes
 * become the consent field (with the policy URL resolved against the storage root when
 * relative); unknown input types degrade to plain text, which the backend accepts as a string.
 */
private fun EasyStaffPlanningFormField.toAppointmentFormField(): AppointmentFormField {
    val placeholder = placeholder?.takeIf { it.isNotBlank() }
    return when {
        position == EasyStaffPlanningFieldPosition.Gdpr || type == EasyStaffPlanningFieldType.Checkbox ->
            AppointmentFormField.Consent(
                code = code,
                label = label,
                required = required,
                policyUrl = options?.firstOrNull()?.value?.let { url ->
                    if (url.startsWith("http")) url else PolicyStorageBase + url
                },
            )

        type == EasyStaffPlanningFieldType.Email -> AppointmentFormField.Email(
            code = code,
            label = label,
            required = required,
            placeholder = placeholder,
            primary = primary,
        )

        type == EasyStaffPlanningFieldType.TextArea -> AppointmentFormField.TextArea(
            code = code,
            label = label,
            required = required,
            placeholder = placeholder,
        )

        type == EasyStaffPlanningFieldType.Phone -> AppointmentFormField.Phone(
            code = code,
            label = label,
            required = required,
            placeholder = placeholder,
        )

        type == EasyStaffPlanningFieldType.Select -> AppointmentFormField.Select(
            code = code,
            label = label,
            required = required,
            placeholder = placeholder,
            options = options.orEmpty().map { AppointmentFormOption(value = it.value, label = it.label) },
        )

        else -> AppointmentFormField.Text(
            code = code,
            label = label,
            required = required,
            placeholder = placeholder,
        )
    }
}

/**
 * Builds the reservation of a slot from the service's booking form and the values entered,
 * keyed by field code. Like the web front-end, values go in the user, appointment or staff
 * section of the request by the position of their field; consent fields, fields with an
 * unknown position and blank values are left out. The primary field's value keys the
 * reservation.
 */
fun List<EasyStaffPlanningFormField>.toReservationRequest(
    portal: EasyStaffPlanningPortal,
    serviceId: Int,
    areaId: Int,
    slotStart: LocalDateTime,
    durationSeconds: Int,
    values: Map<String, String>,
): EasyStaffPlanningReservationRequest {
    val primaryField = firstOrNull { it.primary }
    val primaryValue = primaryField?.code?.let { values[it] }
    require(!primaryValue.isNullOrBlank()) { "A value for the primary field (${primaryField?.code}) is required" }
    val slotEnd = slotStart.plusSeconds(durationSeconds.toLong())
    return EasyStaffPlanningReservationRequest(
        reservationNumber = 0,
        portalCode = portal.code,
        startTimeEpochSeconds = slotStart.atZone(CampusZone).toEpochSecond(),
        endTimeEpochSeconds = slotEnd.atZone(CampusZone).toEpochSecond(),
        durationSeconds = durationSeconds,
        serviceId = serviceId,
        areaId = areaId,
        primaryValue = primaryValue,
        userFields = valuesAt(EasyStaffPlanningFieldPosition.User, values),
        serviceFields = valuesAt(EasyStaffPlanningFieldPosition.Service, values),
        backofficeFields = valuesAt(EasyStaffPlanningFieldPosition.Backoffice, values),
        resourceId = null,
        recaptchaToken = null,
        timeZone = CampusZone.id,
    )
}

/** The non-blank values of the fields at [position], keyed by field code. */
private fun List<EasyStaffPlanningFormField>.valuesAt(
    position: EasyStaffPlanningFieldPosition,
    values: Map<String, String>,
): Map<String, String> =
    filter { it.position == position }
        .mapNotNull { field -> values[field.code]?.takeIf { it.isNotBlank() }?.let { field.code to it } }
        .toMap()

/**
 * Builds the domain reservation from the booking response, denormalizing the service and
 * offering the user picked. The appointment times prefer the authoritative values echoed by
 * the backend and fall back to the picked slot.
 */
fun toAppointmentReservation(
    result: EasyStaffPlanningReservationResult,
    service: AppointmentService,
    offering: AppointmentOffering,
    slotStart: LocalDateTime,
    durationSeconds: Int,
    email: String,
): AppointmentReservation {
    val start = result.startTimeEpochSeconds?.toCampusDateTime() ?: slotStart
    val end = result.endTimeEpochSeconds?.toCampusDateTime()
        ?: slotStart.plusSeconds(durationSeconds.toLong())
    return AppointmentReservation(
        code = requireNotNull(result.reservationCode) { "The booking returned no reservation code" },
        email = email,
        entryId = result.entryId,
        serviceId = service.id,
        serviceName = service.name,
        serviceGroup = service.group,
        areaName = offering.area.name,
        areaAddress = offering.area.address,
        start = start,
        end = end,
        qrCodeDataUrl = result.qrCode?.takeIf { it.isNotBlank() },
        webConferenceUrl = result.webConferenceUrl?.takeIf { it.isNotBlank() },
    )
}

fun AppointmentReservation.toEntity(createdAt: Long) = AppointmentReservationEntity(
    code = code,
    email = email,
    entryId = entryId,
    serviceId = serviceId,
    serviceName = serviceName,
    serviceGroup = serviceGroup,
    areaName = areaName,
    areaAddress = areaAddress,
    startEpochSeconds = start.atZone(CampusZone).toEpochSecond(),
    endEpochSeconds = end.atZone(CampusZone).toEpochSecond(),
    qrCodeDataUrl = qrCodeDataUrl,
    webConferenceUrl = webConferenceUrl,
    createdAt = createdAt,
)

fun AppointmentReservationEntity.toDomain() = AppointmentReservation(
    code = code,
    email = email,
    entryId = entryId,
    serviceId = serviceId,
    serviceName = serviceName,
    serviceGroup = serviceGroup,
    areaName = areaName,
    areaAddress = areaAddress,
    start = startEpochSeconds.toCampusDateTime(),
    end = endEpochSeconds.toCampusDateTime(),
    qrCodeDataUrl = qrCodeDataUrl,
    webConferenceUrl = webConferenceUrl,
)

private fun Long.toCampusDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochSecond(this), CampusZone)
