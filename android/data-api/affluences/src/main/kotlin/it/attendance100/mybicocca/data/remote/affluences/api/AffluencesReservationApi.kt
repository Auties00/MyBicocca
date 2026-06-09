package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAgreement
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAgreementsEnvelope
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesAvailableResource
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailConfirmation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesIdentityProvider
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesIdentityProvidersEnvelope
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationCancellation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationResult
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationSiteInfo
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationToCancel
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesReservationValidation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceEmailPolicy
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceForms
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceStatus
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceTypeFilters
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesTrustDeviceRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesValidateReservationRequest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate
import java.time.LocalTime

/**
 * API for resource and booking operations (`reservation.affluences.com`).
 *
 * Provides access to:
 * - Bookable resource types and their availability (seat zones, study rooms, carrels...)
 * - Booking constraints (open days, durations, agreements, email restrictions, custom forms,
 *   single sign-on requirements)
 * - The full reservation lifecycle: create, validate with the emailed code, confirm, cancel
 *
 * The standard booking flow for a site without single sign-on is:
 * 1. [getReservationInfo] to list the resource types of the site
 * 2. [getAvailableResources] to pick a resource and a time slot
 * 3. [createReservation] with the user email
 * 4. The user receives a validation code by email, redeemed with [validateReservation]
 *    (devices previously trusted through [trustDevice] skip this step)
 *
 * Site identifiers accept either the UUID or the slug of the site, like the site endpoints.
 */
class AffluencesReservationApi(
    client: HttpClient,
    json: Json
) : AffluencesAbstractApi(client, json) {

    /**
     * Retrieves the bookable resource types of a site.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The resource types and the booking contact of the site
     */
    suspend fun getReservationInfo(siteIdentifier: String): AffluencesReservationSiteInfo =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/sites/$siteIdentifier/infos"
        )

    /**
     * Retrieves the resources of a type with their availability for a day.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @param date The day to query availability for
     * @param resourceTypeId The resource type to query, from
     * [it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesResourceTypeInfo.resourceTypeId]
     * @param durationMinutes When provided, only slots that can fit this duration are returned
     * @param resourceId When provided, only this resource is returned
     * @param capacity When provided, only resources that can host this many people are returned
     * @return The available resources with their slots
     */
    suspend fun getAvailableResources(
        siteIdentifier: String,
        date: LocalDate,
        resourceTypeId: Int,
        durationMinutes: Int? = null,
        resourceId: Int? = null,
        capacity: Int? = null
    ): List<AffluencesAvailableResource> =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/resources/$siteIdentifier/available",
            queryParams = buildMap {
                put("date", formatDate(date))
                put("type", resourceTypeId)
                if (durationMinutes != null) {
                    put("duration", durationMinutes)
                }
                if (resourceId != null) {
                    put("resource_id", resourceId)
                }
                if (capacity != null) {
                    put("capacity", capacity)
                }
            }
        )

    /**
     * Retrieves the booking filters of a resource type: open days, slot start times, accepted
     * durations and party sizes, and the resources of the type.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @param resourceTypeId The resource type to query
     * @param date The reference day for the filters
     * @param resourceId When provided, filters are computed for this resource only
     * @return The booking filters of the type
     */
    suspend fun getResourceTypeFilters(
        siteIdentifier: String,
        resourceTypeId: Int,
        date: LocalDate,
        resourceId: Int? = null
    ): AffluencesResourceTypeFilters =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/sites/$siteIdentifier/types/$resourceTypeId/filters",
            queryParams = buildMap {
                put("date", formatDate(date))
                if (resourceId != null) {
                    put("resource_id", resourceId)
                }
            }
        )

    /**
     * Retrieves the terms-of-use agreements a user must consent to before booking at a site.
     *
     * @param siteIdentifier The UUID or slug of the site
     * @return The agreements of the site
     */
    suspend fun getAgreements(siteIdentifier: String): List<AffluencesAgreement> =
        executeGet<AffluencesAgreementsEnvelope>(
            baseUrl = RESERVATION_API_URL,
            path = "/sites/$siteIdentifier/agreements"
        ).agreements

    /**
     * Retrieves the email restrictions of a resource.
     *
     * @param resourceId The numeric identifier of the resource
     * @return The email restrictions of the resource
     */
    suspend fun getResourceEmailPolicy(resourceId: Int): AffluencesResourceEmailPolicy =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/resource/$resourceId"
        )

    /**
     * Retrieves the custom booking form of a resource, when the site defines one.
     *
     * @param resourceId The numeric identifier of the resource
     * @return The custom form of the resource
     */
    suspend fun getResourceForms(resourceId: Int): AffluencesResourceForms =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/resource/$resourceId/forms"
        )

    /**
     * Retrieves the single sign-on identity providers required to book a resource.
     *
     * @param resourceId The numeric identifier of the resource
     * @return The identity providers, or an empty list when booking only needs email validation
     */
    suspend fun getResourceIdentityProviders(resourceId: Int): List<AffluencesIdentityProvider> =
        executeGet<AffluencesIdentityProvidersEnvelope>(
            baseUrl = RESERVATION_API_URL,
            path = "/resource/$resourceId/identity-providers"
        ).identityProviders

    /**
     * Retrieves the live status of a resource: its current state, the slots of the day, and
     * the booking instructions of the site.
     *
     * @param resourceId The numeric identifier of the resource
     * @return The status of the resource
     */
    suspend fun getResourceStatus(resourceId: Int): AffluencesResourceStatus =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/status/$resourceId"
        )

    /**
     * Creates a reservation for a resource.
     *
     * Unless the device was previously trusted (see [trustDevice]) or the site uses single
     * sign-on, the reservation must then be validated with the code the user receives by email
     * (see [validateReservation]).
     *
     * Resources with a custom booking form (see [getResourceForms]) may require additional
     * fields this client does not model; such reservations should be created through the
     * Affluences website instead.
     *
     * @param resourceId The numeric identifier of the resource
     * @param email The email address of the user. Must match the email restrictions of the
     * resource (see [getResourceEmailPolicy])
     * @param date The day of the reservation
     * @param startTime The start of the reservation, local to the site time zone
     * @param endTime The end of the reservation, local to the site time zone
     * @param personCount The number of people the reservation is for
     * @param note The note attached to the reservation, when the resource requires one
     * @param userFirstName The first name of the user, when the resource requires it
     * @param userLastName The last name of the user, when the resource requires it
     * @param userPhone The phone number of the user, when the resource requires it
     * @param userDeviceToken The trusted device token of the user, which skips email validation
     * @param authType The single sign-on protocol used to authenticate (`SAML` or `OIDC`),
     * or null for email-validated reservations
     * @param authorization The single sign-on credential (a SAML auth token or an OIDC id
     * token), sent as the `Authorization` header, or null for email-validated reservations
     * @return The result of the reservation
     */
    suspend fun createReservation(
        resourceId: Int,
        email: String,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        personCount: Int = 1,
        note: String? = null,
        userFirstName: String? = null,
        userLastName: String? = null,
        userPhone: String? = null,
        userDeviceToken: String? = null,
        authType: String? = null,
        authorization: String? = null
    ): AffluencesReservationResult =
        executePost(
            baseUrl = RESERVATION_API_URL,
            path = "/reserve/$resourceId",
            body = AffluencesReservationRequest(
                email = email,
                date = formatDate(date),
                startTime = formatTime(startTime),
                endTime = formatTime(endTime),
                personCount = personCount,
                authType = authType,
                note = note,
                userFirstName = userFirstName,
                userLastName = userLastName,
                userPhone = userPhone,
                userDeviceToken = userDeviceToken
            ),
            headers = buildMap {
                if (authorization != null) {
                    put(HttpHeaders.Authorization, authorization)
                }
            }
        )

    /**
     * Validates the pending reservations of a user with the code received by email.
     *
     * @param validationCode The validation code received by email
     * @param email The email address the reservation was created with
     * @return The validated reservations
     */
    suspend fun validateReservation(validationCode: String, email: String): AffluencesReservationValidation =
        executePost(
            baseUrl = RESERVATION_API_URL,
            path = "/validateReservation",
            body = AffluencesValidateReservationRequest(
                validationCode = validationCode,
                email = email
            )
        )

    /**
     * Confirms a reservation on site (e.g. after scanning the check-in QR code).
     *
     * @param reservationToken The token identifying the reservation
     * @return The result of the confirmation
     */
    suspend fun confirmReservation(reservationToken: String): AffluencesReservationResult =
        executePost(
            baseUrl = RESERVATION_API_URL,
            path = "/reservations/$reservationToken/confirmation",
            body = EmptyJsonBody
        )

    /**
     * Retrieves the reservation a cancellation email token refers to.
     *
     * @param emailToken The cancellation token received by email (the `reservationToken` carried
     * by the confirmation/cancellation links)
     * @return The reservation the token refers to
     */
    suspend fun getReservationToCancel(emailToken: String): AffluencesReservationToCancel =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/cancelReservation/$emailToken"
        )

    /**
     * Cancels a reservation.
     *
     * @param emailToken The cancellation token received by email
     * @return The result of the cancellation
     */
    suspend fun cancelReservation(emailToken: String): AffluencesReservationCancellation =
        executeDelete(
            baseUrl = RESERVATION_API_URL,
            path = "/cancelReservation/",
            queryParams = mapOf("email_token" to emailToken)
        )

    /**
     * Confirms the email address of a user.
     *
     * @param token The confirmation token received by email
     * @return The result of the confirmation
     */
    suspend fun confirmEmail(token: String): AffluencesEmailConfirmation =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/myreservations/email_confirmation/$token"
        )

    /**
     * Marks the current device as trusted, so that future reservations with the same email
     * skip the validation step.
     *
     * @param email The email address of the user
     * @param token The trust token received by email
     * @return The raw trust payload
     */
    suspend fun trustDevice(email: String, token: String): JsonObject =
        executePost(
            baseUrl = RESERVATION_API_URL,
            path = "/trust-device",
            body = AffluencesTrustDeviceRequest(
                email = email,
                token = token
            )
        )

    private companion object {
        // POST /reservations/{token}/confirmation expects a JSON body, even if empty
        private val EmptyJsonBody = emptyMap<String, String>()
    }
}
