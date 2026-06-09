package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDaySchedule
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFieldPosition
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningFormField
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningManagedReservation
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningMonthSchedule
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationRequest
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningReservationResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningResourceAvailability
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningResult
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

/**
 * API surface bound to a Portale Planning portal that supports anonymous booking, extending
 * the discovery operations of [EasyStaffPlanningPortalApi] with availability schedules and
 * the full reservation lifecycle.
 *
 * The standard anonymous booking flow is:
 * 1. [getServices] and [getAreas] to pick a service and the area offering it
 * 2. [getMonthSchedule] to find a day with availability, then [getDaySchedule] to pick a slot
 * 3. [getServiceForm] to load the dynamic booking form of the service
 * 4. [buildReservationRequest] + [createReservation], which create a provisional hold
 * 5. [confirmReservation] within the booking session window to finalize the hold; abandoning
 *    the flow instead requires [deleteReservation] with `force = true`
 *
 * Reservations are keyed by the value of the primary form field (the email address): together
 * with the reservation code it authorizes [getReservation], [updateReservationFields], and
 * [deleteReservation].
 */
class EasyStaffPlanningBookingApi(
    portal: EasyStaffPlanningPortal,
    client: HttpClient,
    json: Json
) : EasyStaffPlanningPortalApi(portal, client, json) {

    companion object {
        /**
         * Time zone of the university campuses, used to convert appointment times to the Unix
         * timestamps the backend expects.
         */
        val CAMPUS_TIME_ZONE: ZoneId = ZoneId.of("Europe/Rome")

        // The write endpoints expect a JSON body, even when there is nothing to send
        private val EmptyJsonBody = JsonObject(emptyMap())
    }

    /**
     * Retrieves the dynamic booking form of a service.
     *
     * @param serviceId The numeric identifier of the service
     * @return The fields of the booking form
     */
    suspend fun getServiceForm(serviceId: Int): List<EasyStaffPlanningFormField> =
        executeGet(
            portal = portal,
            path = "/cliente/${portal.code}/formData/$serviceId"
        )

    /**
     * Retrieves the availability of a service at an area over a month.
     *
     * @param serviceId The numeric identifier of the service
     * @param areaId The numeric identifier of the area
     * @param month The month to query
     * @param durationSeconds The appointment duration, in seconds (usually
     * [EasyStaffPlanningService.durationSeconds])
     * @return The month availability
     */
    suspend fun getMonthSchedule(
        serviceId: Int,
        areaId: Int,
        month: YearMonth,
        durationSeconds: Int
    ): EasyStaffPlanningMonthSchedule =
        executeGet(
            portal = portal,
            path = "/entry/$serviceId/schedule/${formatMonth(month)}/$areaId/$durationSeconds"
        )

    /**
     * Retrieves the slot-by-slot availability of a service at an area on a day.
     *
     * @param serviceId The numeric identifier of the service
     * @param areaId The numeric identifier of the area
     * @param date The day to query
     * @param durationSeconds The appointment duration, in seconds (usually
     * [EasyStaffPlanningService.durationSeconds])
     * @return The day availability
     */
    suspend fun getDaySchedule(
        serviceId: Int,
        areaId: Int,
        date: LocalDate,
        durationSeconds: Int
    ): EasyStaffPlanningDaySchedule =
        executeGet(
            portal = portal,
            path = "/entry/$serviceId/schedule/${formatDate(date)}/$areaId/$durationSeconds"
        )

    /**
     * Checks whether a service has any availability at an area.
     *
     * The month schedule (see [EasyStaffPlanningMonthSchedule.firstAvailable]) reports when
     * the first available slot is; this endpoint only acknowledges that one exists.
     *
     * @param serviceId The numeric identifier of the service
     * @param areaId The numeric identifier of the area
     * @return Whether the service has availability at the area
     */
    suspend fun getFirstAvailability(
        serviceId: Int,
        areaId: Int
    ): EasyStaffPlanningResult =
        executeGet(
            portal = portal,
            path = "/prima_disponibilita/$serviceId/$areaId"
        )

    /**
     * Retrieves the availability of each resource serving a service at an area on a day.
     *
     * @param serviceId The numeric identifier of the service
     * @param areaId The numeric identifier of the area
     * @param date The day to query
     * @param durationSeconds The appointment duration, in seconds (usually
     * [EasyStaffPlanningService.durationSeconds])
     * @return The per-resource availability
     */
    suspend fun getResourceAvailability(
        serviceId: Int,
        areaId: Int,
        date: LocalDate,
        durationSeconds: Int
    ): List<EasyStaffPlanningResourceAvailability> =
        executeGet(
            portal = portal,
            path = "/risorsa/$serviceId/$areaId/${formatDate(date)}/$durationSeconds"
        )

    /**
     * Builds a reservation request from the dynamic booking form of a service.
     *
     * Values are split into the user and appointment sections of the request based on the
     * position of each form field; the GDPR consent field is excluded, matching the web
     * front-end. The value of the primary field becomes the reservation key.
     *
     * @param serviceId The numeric identifier of the service
     * @param areaId The numeric identifier of the area
     * @param slotStart The start of the appointment, local to [CAMPUS_TIME_ZONE]
     * @param durationSeconds The appointment duration, in seconds (usually
     * [EasyStaffPlanningService.durationSeconds])
     * @param formFields The booking form of the service, from [getServiceForm]
     * @param values The form values, keyed by field code (see
     * [EasyStaffPlanningFormField.code]). Must include a value for the primary field
     * @param resourceId The specific resource to book, or null to let the backend assign one
     * @return The reservation request to pass to [createReservation]
     * @throws IllegalArgumentException When the primary field has no value
     */
    fun buildReservationRequest(
        serviceId: Int,
        areaId: Int,
        slotStart: LocalDateTime,
        durationSeconds: Int,
        formFields: List<EasyStaffPlanningFormField>,
        values: Map<String, String>,
        resourceId: Int? = null
    ): EasyStaffPlanningReservationRequest {
        val primaryField = formFields.firstOrNull { it.primary }
        val primaryValue = primaryField?.code?.let { values[it] }
        require(!primaryValue.isNullOrBlank()) {
            "A value for the primary field (${primaryField?.code ?: "unknown"}) is required"
        }
        val slotEnd = slotStart.plusSeconds(durationSeconds.toLong())
        return EasyStaffPlanningReservationRequest(
            reservationNumber = 0,
            portalCode = portal.code,
            startTimeEpochSeconds = slotStart.atZone(CAMPUS_TIME_ZONE).toEpochSecond(),
            endTimeEpochSeconds = slotEnd.atZone(CAMPUS_TIME_ZONE).toEpochSecond(),
            durationSeconds = durationSeconds,
            serviceId = serviceId,
            areaId = areaId,
            primaryValue = primaryValue,
            userFields = fieldValues(formFields, values, EasyStaffPlanningFieldPosition.User),
            serviceFields = fieldValues(formFields, values, EasyStaffPlanningFieldPosition.Service),
            resourceId = resourceId,
            recaptchaToken = null,
            timeZone = CAMPUS_TIME_ZONE.id
        )
    }

    /**
     * Creates a provisional reservation.
     *
     * The reservation holds the slot but is not final: it must be confirmed with
     * [confirmReservation] within the booking session window (the web front-end uses 30
     * minutes), or discarded with [deleteReservation] and `force = true`.
     *
     * @param request The reservation request, usually from [buildReservationRequest]. Must
     * target this portal
     * @return The created reservation
     */
    suspend fun createReservation(
        request: EasyStaffPlanningReservationRequest
    ): EasyStaffPlanningReservationResult {
        require(request.portalCode == portal.code) {
            "The request targets portal ${request.portalCode}, not ${portal.code}"
        }
        return executePost(
            portal = portal,
            path = "/entry/store",
            body = request
        )
    }

    /**
     * Confirms a provisional reservation, finalizing it.
     *
     * @param entryId The entry identifier of the reservation, from
     * [EasyStaffPlanningReservationResult.entryId]
     * @return The acknowledgement of the confirmation
     */
    suspend fun confirmReservation(entryId: Int): EasyStaffPlanningResult =
        executePost(
            portal = portal,
            path = "/entry/confirm/$entryId",
            body = EmptyJsonBody
        )

    /**
     * Retrieves a reservation for management.
     *
     * @param reservationCode The code of the reservation
     * @param primaryValue The primary value (email address) the reservation is keyed by
     * @return The managed reservation
     * @throws it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
     * With message `not_found` when the reservation does not exist
     */
    suspend fun getReservation(
        reservationCode: String,
        primaryValue: String
    ): EasyStaffPlanningManagedReservation =
        executeGet(
            portal = portal,
            path = "/entry/manage/$reservationCode",
            queryParams = mapOf("chiave" to primaryValue)
        )

    /**
     * Retrieves the identifier of the service a reservation was booked for, used to reload
     * its booking form with [getServiceForm].
     *
     * @param reservationCode The code of the reservation
     * @param primaryValue The primary value (email address) the reservation is keyed by
     * @return The numeric identifier of the booked service
     * @throws it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
     * With message `reservation_not_found` when the reservation does not exist
     */
    suspend fun getReservationServiceId(
        reservationCode: String,
        primaryValue: String
    ): Int =
        executeGet(
            portal = portal,
            path = "/entry/$reservationCode/info",
            queryParams = mapOf("chiave" to primaryValue)
        )

    /**
     * Updates the user-editable form fields of a reservation.
     *
     * @param reservationCode The code of the reservation
     * @param primaryValue The primary value (email address) the reservation is keyed by
     * @param publicFields The new field values, keyed by field code. Primary and identifying
     * fields cannot be edited
     * @return The acknowledgement of the update
     */
    suspend fun updateReservationFields(
        reservationCode: String,
        primaryValue: String,
        publicFields: Map<String, String>
    ): EasyStaffPlanningResult =
        executePost(
            portal = portal,
            path = "/entry/update/$reservationCode",
            body = buildJsonObject {
                put("public_fields", buildJsonObject {
                    publicFields.forEach { (code, value) -> put(code, value) }
                })
            },
            queryParams = mapOf("chiave" to primaryValue)
        )

    /**
     * Releases the seat of a reservation early, making it bookable again without cancelling
     * the appointment record.
     *
     * Only services with [it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningDetailedService.seatReleaseEnabled]
     * support this operation.
     *
     * @param reservationCode The code of the reservation
     * @param primaryValue The primary value (email address) the reservation is keyed by
     * @return The acknowledgement of the release
     */
    suspend fun releaseSeat(
        reservationCode: String,
        primaryValue: String
    ): EasyStaffPlanningResult =
        executePost(
            portal = portal,
            path = "/entry/update/$reservationCode",
            body = buildJsonObject {
                put("type", "libera_posto")
            },
            queryParams = mapOf("chiave" to primaryValue)
        )

    /**
     * Confirms the presence of the user at their appointment, recording the workstation they
     * are at.
     *
     * @param reservationCode The code of the reservation
     * @param primaryValue The primary value (email address) the reservation is keyed by
     * @param station The workstation the user is at
     * @return The acknowledgement of the confirmation
     */
    suspend fun confirmPresence(
        reservationCode: String,
        primaryValue: String,
        station: String
    ): EasyStaffPlanningResult =
        executePost(
            portal = portal,
            path = "/entry/update/$reservationCode",
            body = buildJsonObject {
                put("type", "conferma_presenza")
                put("postazione", station)
            },
            queryParams = mapOf("chiave" to primaryValue)
        )

    /**
     * Deletes a reservation.
     *
     * @param reservationCode The code of the reservation
     * @param primaryValue The primary value (email address) the reservation is keyed by
     * @param force Whether to force the deletion, used to discard provisional reservations
     * that were never confirmed
     * @return The acknowledgement of the deletion
     */
    suspend fun deleteReservation(
        reservationCode: String,
        primaryValue: String,
        force: Boolean = false
    ): EasyStaffPlanningResult =
        executePost(
            portal = portal,
            path = "/entry/delete/$reservationCode",
            body = if (force) {
                buildJsonObject {
                    put("force", true)
                }
            } else {
                EmptyJsonBody
            },
            queryParams = mapOf("chiave" to primaryValue)
        )

    /**
     * Downloads the PDF summary of a reservation.
     *
     * @param entryId The entry identifier of the reservation
     * @return The PDF document bytes
     */
    suspend fun getReservationPdf(entryId: Int): ByteArray =
        executeGetBytes(
            portal = portal,
            path = "/entry/pdf/$entryId"
        )

    /**
     * Downloads the ICS calendar file of a recurring reservation.
     *
     * @param repeatId The repetition identifier of the reservation, from
     * [EasyStaffPlanningReservationResult.repeatId]
     * @param timeZone The IANA time zone the event times are expressed in
     * @return The ICS file bytes
     */
    suspend fun getReservationIcs(
        repeatId: Int,
        timeZone: ZoneId = CAMPUS_TIME_ZONE
    ): ByteArray =
        executeGetBytes(
            portal = portal,
            path = "/ics/$repeatId",
            queryParams = mapOf("timezone" to timeZone.id)
        )

    private fun fieldValues(
        formFields: List<EasyStaffPlanningFormField>,
        values: Map<String, String>,
        position: EasyStaffPlanningFieldPosition
    ): Map<String, String> =
        formFields.asSequence()
            .filter { it.position == position }
            .mapNotNull { field -> values[field.code]?.let { field.code to it } }
            .toMap()
}
