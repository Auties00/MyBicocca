package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCheckin
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesCheckinRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesDataEnvelope
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailLinkRequest
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailTokenBody
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesEmailValidation
import it.attendance100.mybicocca.data.remote.affluences.dto.AffluencesMyReservations
import kotlinx.serialization.json.Json

/**
 * API for the personal "my reservations" account flow.
 *
 * Unlike the public site and reservation endpoints, these calls identify the caller as the mobile
 * app (a fixed `x-service-name: mobile_app` header set and a per-install `user-identifier`) and let
 * a user list and manage every reservation made with their email, across booking channels.
 *
 * The flow is:
 * 1. [checkIn] once per install to mint a `user-identifier` (the device api key).
 * 2. [requestEmailValidation] with the user email — the server emails a one-time validation link.
 * 3. The user opens that link in a browser.
 * 4. [pollEmailValidation] until it returns a session `auth_token`.
 * 5. [getMyReservations] with that token to list the user's reservations.
 *
 * Cancellation reuses [AffluencesReservationApi.cancelReservation] with the `cancellation_token`
 * carried by each listed reservation.
 *
 * These endpoints gate on the mobile app header set ([MOBILE_HEADERS]) rather than the user agent,
 * so this shares the main client and adds those headers per request.
 */
class AffluencesAccountApi(
    client: HttpClient,
    json: Json
) : AffluencesAbstractApi(client, json) {

    /**
     * Registers the device and mints a `user-identifier` api key.
     *
     * Sent without an `authorization` header so the server issues a fresh identity; an unknown
     * identity would otherwise be rejected.
     *
     * @param deviceId An opaque device identifier
     * @param deviceLang The device language (e.g. `it`)
     * @param firebaseToken The FCM token, or null when push is not configured
     * @return The check-in result, whose api key is the `user-identifier` for later calls
     */
    suspend fun checkIn(
        deviceId: String,
        deviceLang: String = "it",
        firebaseToken: String? = null
    ): AffluencesCheckin =
        executePost<AffluencesDataEnvelope<AffluencesCheckin>>(
            baseUrl = APP_API_V4_URL,
            path = "/apps/checkin",
            body = AffluencesCheckinRequest(
                deviceId = deviceId,
                deviceLang = deviceLang,
                firebaseToken = firebaseToken
            ),
            headers = MOBILE_APP_HEADERS
        ).data

    /**
     * Requests an email-validation link for an email address.
     *
     * The server emails the user a one-time link; opening it unlocks the session token, which then
     * becomes available through [pollEmailValidation].
     *
     * @param apiKey The device api key from [checkIn], sent as `user-identifier`
     * @param email The email address to validate
     * @return The validation request, whose [AffluencesEmailLinkRequest.requestUuid] is used to poll
     */
    suspend fun requestEmailValidation(apiKey: String, email: String): AffluencesEmailLinkRequest =
        executePost(
            baseUrl = RESERVATION_API_URL,
            path = "/myreservations/token",
            body = AffluencesEmailTokenBody(email),
            headers = MOBILE_APP_HEADERS + (USER_IDENTIFIER_HEADER to apiKey)
        )

    /**
     * Polls an email-validation request for completion.
     *
     * Before the user opens the email link the server answers with a `does_not_exist` error
     * (surfaced as an exception); once opened it returns the session token.
     *
     * @param apiKey The device api key from [checkIn], sent as `user-identifier`
     * @param requestUuid The identifier returned by [requestEmailValidation]
     * @return The validation result carrying the session `auth_token`
     */
    suspend fun pollEmailValidation(apiKey: String, requestUuid: String): AffluencesEmailValidation =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/myreservations/token",
            queryParams = mapOf("request_uuid" to requestUuid),
            headers = MOBILE_APP_HEADERS + (USER_IDENTIFIER_HEADER to apiKey)
        )

    /**
     * Lists the reservations of the validated email.
     *
     * @param apiKey The device api key from [checkIn], sent as `user-identifier`
     * @param authToken The session token from [pollEmailValidation]
     * @return The reservations of the email
     */
    suspend fun getMyReservations(apiKey: String, authToken: String): AffluencesMyReservations =
        executeGet(
            baseUrl = RESERVATION_API_URL,
            path = "/myreservations",
            headers = MOBILE_APP_HEADERS + mapOf(
                USER_IDENTIFIER_HEADER to apiKey,
                HttpHeaders.Authorization to "Bearer $authToken"
            )
        )
}
