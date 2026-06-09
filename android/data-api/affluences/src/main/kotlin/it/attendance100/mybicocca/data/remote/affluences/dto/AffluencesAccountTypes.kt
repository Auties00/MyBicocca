package it.attendance100.mybicocca.data.remote.affluences.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Request payload for the device check-in (`POST /app/v4/apps/checkin`).
 *
 * Sent without an `authorization` header to mint a fresh device identity: the response then
 * carries a new [AffluencesCheckin.apiKey] to use as the `user-identifier` of later calls.
 *
 * @property deviceId An opaque device identifier (the app uses the Android id).
 * @property deviceLang The device language (e.g. `it`).
 * @property lastCheckin The timestamp of the previous check-in, or null on first run.
 * @property lastCheckinAdvertPopup The timestamp of the last advert popup, or null.
 * @property firebaseToken The FCM token, or null when push is not configured.
 */
@Serializable
data class AffluencesCheckinRequest(
    @SerialName("deviceId")
    val deviceId: String,
    @SerialName("deviceLang")
    val deviceLang: String,
    @SerialName("lastCheckin")
    val lastCheckin: String? = null,
    @SerialName("lastCheckinAdvertPopup")
    val lastCheckinAdvertPopup: String? = null,
    @SerialName("firebaseToken")
    val firebaseToken: String? = null
)

/**
 * Result of a device check-in.
 *
 * @property apiKey The device identity to send as the `user-identifier` header of reservation
 * account calls, or null when the server did not issue one.
 * @property latestVersion The latest published app version.
 * @property buildExpired Whether the current build is considered expired by the server.
 */
@Serializable
data class AffluencesCheckin(
    @SerialName("apiKey")
    val apiKey: String? = null,
    @SerialName("latestVersion")
    val latestVersion: String? = null,
    @SerialName("buildExpired")
    val buildExpired: Boolean = false
)

/**
 * Request body for an email-validation link (`POST /myreservations/token`).
 *
 * @property email The email address to validate.
 */
@Serializable
internal class AffluencesEmailTokenBody(
    @SerialName("email")
    val email: String
)

/**
 * Result of requesting an email-validation link (`POST /myreservations/token`).
 *
 * The server emails the user a link (`affluences.com/.../reservation/confirm?...&type=user-reservations`);
 * once the user opens it, [AffluencesEmailValidation.authToken] becomes available through
 * [it.attendance100.mybicocca.data.remote.affluences.api.AffluencesAccountApi.pollEmailValidation].
 *
 * @property message The localized status message.
 * @property requestUuid The identifier of the validation request, used to poll for completion.
 */
@Serializable
data class AffluencesEmailLinkRequest(
    @SerialName("message")
    val message: String? = null,
    @SerialName("request_uuid")
    val requestUuid: String? = null
)

/**
 * Result of polling an email-validation request (`GET /myreservations/token?request_uuid=...`).
 *
 * Before the user opens the email link the server answers with a `does_not_exist` error (HTTP 404);
 * once opened it returns the session [authToken].
 *
 * @property authToken The session token authorizing `my reservations` calls, as a Bearer token.
 * @property message The localized status message.
 */
@Serializable
data class AffluencesEmailValidation(
    @SerialName("auth_token")
    val authToken: String? = null,
    @SerialName("message")
    val message: String? = null
)

/**
 * The reservations of an email address (`GET /myreservations`).
 *
 * @property email The email address the reservations belong to.
 * @property results The reservations, newest activity first.
 */
@Serializable
data class AffluencesMyReservations(
    @SerialName("email")
    val email: String? = null,
    @SerialName("results")
    val results: List<AffluencesMyReservation> = emptyList()
)

/**
 * Lifecycle state of a reservation. Unknown wire values decode to [UNKNOWN] (the field default,
 * coerced by the lenient Json config).
 */
@Serializable
enum class AffluencesReservationState {
    @SerialName("UPCOMING")
    UPCOMING,

    @SerialName("AWAITING_CONFIRMATION")
    AWAITING_CONFIRMATION,

    @SerialName("ONGOING")
    ONGOING,

    @SerialName("PAST")
    PAST,

    @SerialName("CANCELLED")
    CANCELLED,

    UNKNOWN
}

/**
 * A reservation as listed by `GET /myreservations`.
 *
 * @property reservationId The numeric identifier of the reservation.
 * @property resourceId The numeric identifier of the booked resource.
 * @property resourceName The display name of the booked resource (e.g. `R 100 - Posto con presa elettrica`).
 * @property siteName The main display name of the site.
 * @property siteSecondaryName The secondary display name of the site.
 * @property siteTimeZone The IANA time zone of the site; the datetimes below are UTC and should be
 * rendered in this zone.
 * @property reservationToken The short human-readable reservation code.
 * @property cancellationToken The token authorizing cancellation of this reservation.
 * @property personCount The number of people the reservation is for.
 * @property note The note attached to the reservation.
 * @property startDateTime The start, as an ISO-8601 UTC instant.
 * @property endDateTime The end, as an ISO-8601 UTC instant.
 * @property requestDateTime When the reservation was created, as an ISO-8601 UTC instant.
 * @property state The reservation state (e.g. `UPCOMING`, `AWAITING_CONFIRMATION`, `PAST`, `CANCELLED`).
 * @property validated Whether the user has validated (confirmed) the reservation from the email.
 * @property confirmationMandatory Whether email confirmation is required for this reservation.
 * @property userValidation Whether the reservation still awaits user validation.
 * @property ticketPayload The ticket payload, when the site issues one.
 */
@Serializable
data class AffluencesMyReservation(
    @SerialName("reservation_id")
    val reservationId: Int,
    @SerialName("resource_id")
    val resourceId: Int? = null,
    @SerialName("resource_name")
    val resourceName: String? = null,
    @SerialName("site_name")
    val siteName: String? = null,
    @SerialName("site_secondary_name")
    val siteSecondaryName: String? = null,
    @SerialName("site_timezone")
    val siteTimeZone: String? = null,
    @SerialName("reservation_token")
    val reservationToken: String? = null,
    @SerialName("cancellation_token")
    val cancellationToken: String? = null,
    @SerialName("person_count")
    val personCount: Int = 1,
    @SerialName("note")
    val note: String? = null,
    @SerialName("reservation_start_datetime")
    val startDateTime: String? = null,
    @SerialName("reservation_end_datetime")
    val endDateTime: String? = null,
    @SerialName("request_datetime")
    val requestDateTime: String? = null,
    @SerialName("state")
    val state: AffluencesReservationState = AffluencesReservationState.UNKNOWN,
    @SerialName("validated")
    val validated: Boolean = false,
    @SerialName("confirmation_mandatory")
    val confirmationMandatory: Boolean = false,
    @SerialName("user_validation")
    val userValidation: Boolean = false,
    @SerialName("ticket_payload")
    val ticketPayload: JsonElement? = null
)
