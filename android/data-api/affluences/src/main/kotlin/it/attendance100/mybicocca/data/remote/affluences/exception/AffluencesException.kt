package it.attendance100.mybicocca.data.remote.affluences.exception

/**
 * A request Affluences rejected.
 *
 * The two backends report errors in different envelopes, both read into this exception:
 * - App API (`api.affluences.com`): `{"error": {"code": "...", "message": "..."}}`
 * - Reservation API (`reservation.affluences.com`): `{"error": "...", "errorMessage": "..."}`
 *
 * Match on [errorCode] (or [statusCode] when the server sent no envelope), never on the
 * message, which is localised.
 *
 * @property statusCode The HTTP status of the response.
 * @property errorCode The machine-readable error code (e.g. `does_not_exist`, `email_invalid`),
 *   or null when the response carried none.
 * @property errorMessage The human-readable message, in the language of the request.
 */
class AffluencesException(
    val statusCode: Int,
    val errorCode: String?,
    val errorMessage: String,
) : Exception(errorMessage) {
    companion object {
        /** Polling an email validation the user hasn't completed yet. */
        const val NOT_VALIDATED_YET = "does_not_exist"

        /** A validation code that doesn't match the reservation. */
        const val INVALID_VALIDATION_CODE = "invalid_validation_code"
    }
}
