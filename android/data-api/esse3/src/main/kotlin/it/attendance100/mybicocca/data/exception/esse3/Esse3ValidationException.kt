package it.attendance100.mybicocca.data.exception.esse3

import io.ktor.http.HttpStatusCode
import it.attendance100.mybicocca.data.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.dto.esse3.Esse3ErrorResponse

/**
 * Raised when the ESSE3 server rejects a request body due to validation errors (HTTP 422).
 *
 * @property errorResponse The parsed error detail returned by the server, if available
 */
class Esse3ValidationException(
    val errorResponse: Esse3ErrorResponse? = null
) : ApiRequestException(errorResponse?.statusCode ?: HttpStatusCode.UnprocessableEntity.value, buildMessage(errorResponse)) {
    companion object {
        private fun buildMessage(errorResponse: Esse3ErrorResponse?): String {
            return buildString {
                append("ESSE3 validation failed (status code ${errorResponse?.statusCode ?: 422})")
                val msg = errorResponse?.errorMessage
                if (!msg.isNullOrBlank()) {
                    append(": ")
                    append(msg)
                }
                val details = errorResponse?.errorDetails
                if (!details.isNullOrEmpty()) {
                    append(" [")
                    details.joinTo(this) { it.errorType }
                    append("]")
                }
            }
        }
    }
}
