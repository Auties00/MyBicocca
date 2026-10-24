package __PACKAGE__

/**
 * A request a server rejected, either with an error status or, for servers reporting errors
 * with HTTP 200, with an error body. Each API's generated exception extends it and names the
 * error codes callers may want to handle.
 *
 * Match on [errorCode], or on [statusCode] when the server sent none, never on the message,
 * which servers may localise.
 *
 * @property api The name of the API, for the message.
 * @property statusCode The HTTP status of the response.
 * @property errorCode The machine-readable error code the body carried, if any.
 * @property errorMessage The human-readable message the body carried, else the error code,
 *   else a description of the status.
 */
open class ApiException(
    val api: String,
    val statusCode: Int,
    val errorCode: String?,
    val errorMessage: String,
) : Exception(
    "$api request failed with status $statusCode" +
        (errorCode?.takeUnless { it == errorMessage }?.let { " ($it)" } ?: "") +
        ": $errorMessage"
)

/**
 * A response that doesn't hold what the operation reads from it: a page whose layout changed,
 * or a script that no longer assigns the expected variable.
 */
class HtmlParsingException(message: String) : Exception(message)
