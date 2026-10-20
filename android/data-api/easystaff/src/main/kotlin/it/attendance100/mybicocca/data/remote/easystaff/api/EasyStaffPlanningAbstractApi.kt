package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.time.Duration.Companion.seconds

/**
 * Base class of the generated Portale Planning modules: sends a request, translates error
 * responses into [ApiRequestException] and decodes the value nested in the response's
 * envelope.
 *
 * The backend reports errors as `{"message": "<key>", "data": [...]}`; the key (e.g.
 * `reservation_not_found`) becomes the exception's message.
 *
 * Some endpoints, the booking form and the availability schedules among them, also require
 * the `jwt_public` guest cookie. The backend hands it out on the service and area listings and
 * lets it expire after twenty minutes, so a request rejected with `unauthorized.invalid_token`
 * fetches the service listing for a fresh token and is sent once more. This relies on the
 * client keeping cookies (see [EasyStaffPlanningRuntime]).
 *
 * @param basePath Path prefix of every operation of the module (empty for Portale Planning).
 */
abstract class EasyStaffPlanningAbstractApi(
    protected val client: HttpClient,
    @PublishedApi internal val json: Json,
    private val basePath: String,
) {
    private companion object {
        /** Header carrying the identifier of the portal a request targets. */
        const val PORTAL_HEADER = "X-CLIENTE"

        /** Error key the backend returns when the guest token is missing or expired. */
        const val INVALID_TOKEN_ERROR = "unauthorized.invalid_token"

        /**
         * Error key the backend returns when a booking is stored with a guest token younger
         * than about ten seconds, which it takes for automation.
         */
        const val TOO_FAST_ERROR = "unauthorized.too_fast"

        /**
         * How long to wait before sending a request rejected as [TOO_FAST_ERROR] again. Measured
         * against the live backend: a token 9 seconds old is refused, one 12 seconds old is not.
         */
        val GUEST_TOKEN_MIN_AGE = 12.seconds
    }

    protected suspend inline fun <reified T> executeJsonGet(
        endpoint: String,
        server: EasyStaffPlanningServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = decode(send(HttpMethod.Get, server, endpoint, block), envelope)

    protected suspend inline fun <reified T> executeJsonGetList(
        endpoint: String,
        server: EasyStaffPlanningServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): List<T> = decode(send(HttpMethod.Get, server, endpoint, block), envelope)

    protected suspend inline fun <reified T> executeJsonPost(
        endpoint: String,
        server: EasyStaffPlanningServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = decode(send(HttpMethod.Post, server, endpoint, block), envelope)

    protected suspend fun executeStreamGet(
        endpoint: String,
        server: EasyStaffPlanningServer,
        block: HttpRequestBuilder.() -> Unit = {},
    ): ByteReadChannel = execute(HttpMethod.Get, server, endpoint, block).bodyAsChannel()

    /** Sends the request and returns its body as JSON, throwing on an error status. */
    @PublishedApi
    internal suspend fun send(
        method: HttpMethod,
        server: EasyStaffPlanningServer,
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit,
    ): JsonElement = json.parseToJsonElement(execute(method, server, endpoint, block).bodyAsText())

    @PublishedApi
    internal inline fun <reified T> decode(body: JsonElement, envelope: List<String>): T =
        json.decodeFromJsonElement(envelope.fold(body) { value, key -> value.jsonObject[key] ?: JsonObject(emptyMap()) })

    /**
     * Sends the request, renewing the guest token and sending it once more if the backend
     * rejected the token. Bookings stored with a fresh token are refused as [TOO_FAST_ERROR],
     * which always happens right after a renewal; the request is then sent once more after
     * [GUEST_TOKEN_MIN_AGE].
     *
     * @throws ApiRequestException When the backend rejects the request for any other reason.
     */
    private suspend fun execute(
        method: HttpMethod,
        server: EasyStaffPlanningServer,
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val url = server.url.trimEnd('/') + basePath + endpoint
        val attempt: suspend () -> HttpResponse = {
            client.request(url) {
                this.method = method
                block()
            }
        }
        var response = attempt()
        var errorKey = if (response.status == HttpStatusCode.Unauthorized) errorKey(response) else null
        if (errorKey == INVALID_TOKEN_ERROR) {
            val portalId = HttpRequestBuilder().apply(block).headers[PORTAL_HEADER]
            if (portalId != null) {
                client.get(server.url.trimEnd('/') + "/servizi/$portalId") { header(PORTAL_HEADER, portalId) }
                response = attempt()
                errorKey = if (response.status == HttpStatusCode.Unauthorized) errorKey(response) else null
            }
        }
        if (errorKey == TOO_FAST_ERROR) {
            delay(GUEST_TOKEN_MIN_AGE)
            response = attempt()
        }
        if (!response.status.isSuccess()) throw error(response)
        return response
    }

    private suspend fun error(response: HttpResponse): ApiRequestException {
        val key = errorKey(response)
        return if (key.isNullOrBlank()) ApiRequestException(response.status.value) else ApiRequestException(response.status.value, key)
    }

    /** The key of a `{"message": "<key>", ...}` error body, if it is one. */
    private suspend fun errorKey(response: HttpResponse): String? {
        val body = runCatching { json.parseToJsonElement(response.bodyAsText()) }.getOrNull() as? JsonObject
        return (body?.get("message") as? JsonPrimitive)?.contentOrNull
    }
}
