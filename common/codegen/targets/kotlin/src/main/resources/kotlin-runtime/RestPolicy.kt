package __PACKAGE__

import io.ktor.http.HttpMethod
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlin.time.Duration

/**
 * How an API's modules send requests and read failures, as its spec describes them.
 *
 * @property baseUrl The base URL of the operations naming no server.
 * @property emptyStatuses Statuses with which a list operation answers an empty list.
 * @property errors Where a failed response's body holds the error's code and message.
 * @property retries Failures to recover from by sending the request again, tried in order.
 * @property failure Builds the API's exception for a failure.
 */
class RestPolicy(
    val baseUrl: String?,
    val emptyStatuses: Set<Int> = emptySet(),
    val errors: ErrorFormat = ErrorFormat(),
    val retries: List<RetryRule> = emptyList(),
    val failure: (Failure) -> Exception,
)

/**
 * A failed request, as handed to [RestPolicy.failure].
 *
 * @property roles The roles the operation requires, a hint when the status is 401 or 403.
 */
class Failure(
    val statusCode: Int,
    val errorCode: String?,
    val errorMessage: String,
    val roles: Set<Enum<*>>,
)

/**
 * Where a failed response's JSON body holds the error's code and message: JSON pointers, tried
 * in order, skipping those holding no string or number.
 *
 * @property detect Pointers that, holding a value other than `false` or `null`, mark a body
 *   received with a successful status as an error.
 */
class ErrorFormat(
    val detect: List<String> = emptyList(),
    val code: List<String> = emptyList(),
    val message: List<String> = emptyList(),
) {
    /** Whether [body], received with a successful status, reports an error. */
    fun detects(body: JsonElement?): Boolean = detect.any { pointer ->
        when (val value = body?.at(pointer)) {
            null, JsonNull -> false
            is JsonPrimitive -> value.booleanOrNull != false
            else -> true
        }
    }

    fun code(body: JsonElement?): String? = code.firstNotNullOfOrNull { body?.text(it) }

    fun message(body: JsonElement?): String? = message.firstNotNullOfOrNull { body?.text(it) }

    /** The string or number at [pointer], if there is one. */
    private fun JsonElement.text(pointer: String): String? =
        (at(pointer) as? JsonPrimitive)?.takeIf { it !is JsonNull && it.booleanOrNull == null }?.content

    private fun JsonElement.at(pointer: String): JsonElement? =
        pointer.removePrefix("/").split('/').fold<String, JsonElement?>(this) { element, key ->
            (element as? JsonObject)?.get(key.replace("~1", "/").replace("~0", "~"))
        }
}

/**
 * A failure to recover from by sending the request again, once.
 *
 * @property status The status of the failure, or null for any error status.
 * @property code The error code of the failure, or null for any.
 * @property before A request to send first, such as one handing out a fresh token.
 * @property delay How long to wait before trying again.
 */
class RetryRule(
    val status: Int? = null,
    val code: String? = null,
    val before: RetryRequest? = null,
    val delay: Duration? = null,
)

/**
 * A request sent to recover from a failure, to the server of the failed request. `{name}`
 * placeholders in [path] and [headers] are replaced by the module's API parameters.
 */
class RetryRequest(
    val method: HttpMethod,
    val path: String,
    val headers: Map<String, String> = emptyMap(),
)
