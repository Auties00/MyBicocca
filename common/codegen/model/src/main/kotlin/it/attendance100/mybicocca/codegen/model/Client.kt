package it.attendance100.mybicocca.codegen.model

/**
 * How an API's client talks to its servers: what every request carries, how failures are
 * told apart from results, and which failures are worth sending a request again for. Targets
 * generate the API's client and its error type from it.
 *
 * @property baseUrl The base URL of the operations naming no [Operation.server]; null when
 *   every operation names one.
 * @property timeoutSeconds How long a request may take, on platforms whose clients have no
 *   reasonable default; null for the platform's.
 * @property keepsCookies Whether the client stores the cookies servers set and sends them back,
 *   as a session or guest token needs.
 * @property caches Whether the client keeps responses, by the same rules on every platform: a
 *   successful GET read whole, not a download, answers its URL for the `max-age` of its
 *   `Cache-Control` (unless `no-store` or `no-cache`), and any other request empties the cache.
 * @property sendsDefaults Whether request bodies spell out fields left at their default values;
 *   some servers need them, others read their presence as a filter.
 * @property sendsNulls Whether request bodies spell out fields without a value as `null`, rather
 *   than leaving them out.
 * @property headers Headers sent with every request, before the operation's own.
 * @property query Query parameters sent with every request.
 * @property settings Values sent with every request that the caller may change while the client
 *   is in use, such as the language of the server's strings.
 * @property emptyStatuses Statuses with which a list operation answers an empty list.
 * @property error How a failed response describes the failure.
 * @property retries Failures to recover from by sending the request again, tried in order.
 */
data class ClientSpec(
    val baseUrl: String? = null,
    val timeoutSeconds: Int? = null,
    val keepsCookies: Boolean = false,
    val caches: Boolean = false,
    val sendsDefaults: Boolean = false,
    val sendsNulls: Boolean = true,
    val headers: Map<String, String> = emptyMap(),
    val query: Map<String, String> = emptyMap(),
    val settings: List<ClientSetting> = emptyList(),
    val emptyStatuses: Set<Int> = emptySet(),
    val error: ErrorFormat = ErrorFormat(),
    val retries: List<RetryRule> = emptyList(),
) {
    companion object {
        val DEFAULT = ClientSpec()
    }
}

/**
 * A value sent with every request that the caller may change at any time.
 *
 * @property name camelCase name of the setting.
 * @property location Where it is sent: [ParameterLocation.QUERY] or [ParameterLocation.HEADER].
 * @property wireName Name of the query parameter or header.
 * @property default The value until the caller sets one.
 */
data class ClientSetting(
    val name: String,
    val location: ParameterLocation,
    val wireName: String,
    val default: String,
    val description: String? = null,
)

/**
 * Where a failed response's JSON body holds the error's code and message. Locations are JSON
 * pointers (`/error/code`), tried in order; one holding no string or number is skipped.
 *
 * @property detect Locations that, holding a value other than `false` or `null`, mark a
 *   response with a successful status as a failure: some servers report errors with HTTP 200.
 *   Only JSON bodies are inspected.
 * @property code Locations of the machine-readable error code.
 * @property message Locations of the human-readable message. The code stands in for a missing
 *   message, and the status for both.
 * @property codes The error codes callers may want to handle, by PascalCase name.
 */
data class ErrorFormat(
    val detect: List<String> = emptyList(),
    val code: List<String> = emptyList(),
    val message: List<String> = emptyList(),
    val codes: List<ErrorCode> = emptyList(),
)

/** A known error code, named [name] (PascalCase) in the generated error type. */
data class ErrorCode(val name: String, val value: String, val description: String? = null)

/**
 * A failure the client recovers from by sending the request again, once.
 *
 * @property status The status of the failure, or null for any error status.
 * @property code The error code of the failure (see [ErrorFormat.code]), or null for any.
 * @property before A request to send before trying again, such as one handing out a fresh
 *   token; its response is ignored.
 * @property delaySeconds How long to wait before trying again.
 */
data class RetryRule(
    val status: Int? = null,
    val code: String? = null,
    val before: RetryRequest? = null,
    val delaySeconds: Int? = null,
    val description: String? = null,
)

/**
 * A request sent to recover from a failure, to the server of the failed request.
 *
 * @property path Path relative to that server; `{name}` placeholders are replaced by the API
 *   parameter of that name.
 * @property headers Headers to send, with the same placeholders.
 */
data class RetryRequest(
    val method: HttpMethod,
    val path: String,
    val headers: Map<String, String> = emptyMap(),
)
