package it.attendance100.mybicocca.data.common.exception

/**
 * Exception thrown when an API request fails due to an unexpected HTTP status code.
 *
 * @property statusCode The HTTP status code that caused the failure.
 */
open class ApiRequestException(
    val statusCode: Int,
    message: String = "Request failed with status code: $statusCode"
) : Exception(message)
