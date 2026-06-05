package it.attendance100.mybicocca.data.remote.affluences.exception

/**
 * Exception thrown when the Affluences API rejects a request with a structured error response.
 *
 * The Affluences backends return two error envelopes, both mapped to this exception:
 * - App API (`api.affluences.com`): `{"error": {"code": "...", "message": "..."}}`
 * - Reservation API (`reservation.affluences.com`): `{"error": "...", "errorMessage": "..."}`
 *
 * Responses with a non-success status code but no parsable error body are reported as
 * [it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException] instead.
 *
 * @property errorCode The machine-readable error code (e.g. `site-not-found`, `email_invalid`),
 * or null when the server provided only a message.
 * @property errorMessage The human-readable error message, localized according to the
 * `Accept-Language` of the request.
 */
class AffluencesException(
    val errorCode: String?,
    val errorMessage: String
) : Exception(errorMessage)
