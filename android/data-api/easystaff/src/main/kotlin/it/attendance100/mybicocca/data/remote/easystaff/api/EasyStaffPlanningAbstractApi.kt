package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.jvm.javaio.toInputStream
import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.remote.common.util.buildUrl
import it.attendance100.mybicocca.data.remote.easystaff.dto.EasyStaffPlanningPortal
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Abstract base class for the Portale Planning API implementations.
 *
 * The Portale Planning backend is a separate JSON service hosted alongside the Agenda Web
 * endpoints. Every request targets a specific booking portal, identified through the
 * `X-CLIENTE` header. Error responses carry a `{"message": "<key>", "data": [...]}` envelope
 * whose key (e.g. `reservation_not_found`) becomes the [ApiRequestException] message;
 * unparsable error bodies fall back to the default message.
 *
 * Some endpoints, the booking form and the availability schedules among them, also require
 * the `jwt_public` guest cookie. The backend hands it out on the service and area listings and
 * lets it expire after twenty minutes, so a request rejected with `unauthorized.invalid_token`
 * fetches the service listing for a fresh token and is sent once more. This relies on the
 * client keeping cookies.
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 * @param json The shared [Json] instance for decoding responses
 */
abstract class EasyStaffPlanningAbstractApi(
    protected val client: HttpClient,
    protected val json: Json
) {
    companion object {
        /**
         * Base URL of the Portale Planning API.
         */
        const val PLANNING_BASE_URL = "https://gestioneorari.didattica.unimib.it/portaleplanningAPI/api"

        /**
         * Header carrying the numeric identifier of the target portal.
         */
        private const val PORTAL_HEADER = "X-CLIENTE"

        /**
         * Header carrying the locale of the request.
         */
        private const val LOCALE_HEADER = "X-APP-LOCALE"

        /**
         * Locale sent with every request.
         */
        private const val LOCALE = "it"

        /**
         * Error key the backend returns when the guest token is missing or expired.
         */
        private const val INVALID_TOKEN_ERROR = "unauthorized.invalid_token"

        /**
         * Month format used in schedule paths (yyyy-MM).
         */
        private val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

        /**
         * Date format used in schedule paths (yyyy-MM-dd).
         */
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        /**
         * Formats a month to the Portale Planning format (yyyy-MM).
         *
         * @param month The month to format
         * @return The formatted month string
         */
        fun formatMonth(month: YearMonth): String = month.format(MONTH_FORMAT)

        /**
         * Formats a date to the Portale Planning format (yyyy-MM-dd).
         *
         * @param date The date to format
         * @return The formatted date string
         */
        fun formatDate(date: LocalDate): String = date.format(DATE_FORMAT)
    }

    /**
     * Executes a GET request against the Portale Planning API and returns the response body
     * as a deserialized JSON type.
     *
     * @param T The JSON type to deserialize the response into
     * @param portal The portal the request targets, or null for portal-independent endpoints
     * @param path The request path (relative to [PLANNING_BASE_URL])
     * @param queryParams Query parameters to append to the URL
     * @param headers Additional headers to send with the request
     * @return The deserialized response body
     * @throws ApiRequestException When the server rejects the request
     */
    protected suspend inline fun <reified T> executeGet(
        portal: EasyStaffPlanningPortal?,
        path: String,
        queryParams: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T {
        val response = sendWithGuestToken(portal) {
            client.get(buildUrl(PLANNING_BASE_URL, path, queryParams)) {
                planningHeaders(portal)
                headers.forEach { (name, value) -> header(name, value) }
            }
        }
        return decodeOrThrow(response)
    }

    /**
     * Executes a POST request with a JSON body against the Portale Planning API and returns
     * the response body as a deserialized JSON type.
     *
     * Raw [JsonElement] bodies are encoded directly rather than handed to content negotiation,
     * whose serializer guessing cannot resolve the internal JsonElement classes.
     *
     * @param T The JSON type to deserialize the response into
     * @param portal The portal the request targets
     * @param path The request path (relative to [PLANNING_BASE_URL])
     * @param body The request body, serialized as JSON
     * @param queryParams Query parameters to append to the URL
     * @return The deserialized response body
     * @throws ApiRequestException When the server rejects the request
     */
    protected suspend inline fun <reified T> executePost(
        portal: EasyStaffPlanningPortal,
        path: String,
        body: Any,
        queryParams: Map<String, Any> = emptyMap()
    ): T {
        val response = sendWithGuestToken(portal) {
            client.post(buildUrl(PLANNING_BASE_URL, path, queryParams)) {
                planningHeaders(portal)
                when (body) {
                    is JsonElement -> setBody(
                        TextContent(
                            json.encodeToString(JsonElement.serializer(), body),
                            ContentType.Application.Json
                        )
                    )

                    else -> {
                        contentType(ContentType.Application.Json)
                        setBody(body)
                    }
                }
            }
        }
        return decodeOrThrow(response)
    }

    /**
     * Executes a GET request against the Portale Planning API and returns the raw response
     * body, used for binary documents like PDFs and ICS files.
     *
     * @param portal The portal the request targets
     * @param path The request path (relative to [PLANNING_BASE_URL])
     * @param queryParams Query parameters to append to the URL
     * @return The raw response body
     * @throws ApiRequestException When the server rejects the request
     */
    protected suspend fun executeGetBytes(
        portal: EasyStaffPlanningPortal,
        path: String,
        queryParams: Map<String, Any> = emptyMap()
    ): ByteArray {
        val response = sendWithGuestToken(portal) {
            client.get(buildUrl(PLANNING_BASE_URL, path, queryParams)) {
                planningHeaders(portal)
            }
        }
        if (!response.status.isSuccess()) {
            throwApiError(response)
        }
        return response.bodyAsBytes()
    }

    /**
     * Sends a request, renewing the guest token and sending it again if the backend rejected
     * the token.
     *
     * Portal-independent requests are sent as they are: the guest token is issued per portal.
     *
     * @param portal The portal the request targets, or null for portal-independent endpoints
     * @param request Sends the request; called a second time after a token renewal
     * @return The response to the last attempt
     * @throws ApiRequestException When the request is unauthorized for any other reason
     */
    protected suspend fun sendWithGuestToken(
        portal: EasyStaffPlanningPortal?,
        request: suspend () -> HttpResponse
    ): HttpResponse {
        val response = request()
        if (portal == null || response.status != HttpStatusCode.Unauthorized) {
            return response
        }
        val errorKey = readErrorKey(response)
        if (errorKey != INVALID_TOKEN_ERROR) {
            throwApiError(response.status.value, errorKey)
        }
        client.get(buildUrl(PLANNING_BASE_URL, "/servizi/${portal.id}")) {
            planningHeaders(portal)
        }
        return request()
    }

    /**
     * Applies the headers the Portale Planning API expects to a request.
     *
     * @param portal The portal the request targets, or null for portal-independent endpoints
     */
    protected fun HttpRequestBuilder.planningHeaders(portal: EasyStaffPlanningPortal?) {
        if (portal != null) {
            header(PORTAL_HEADER, portal.id)
        }
        header(LOCALE_HEADER, LOCALE)
        header(HttpHeaders.Accept, ContentType.Application.Json)
    }

    /**
     * Deserializes a successful response, or translates an error response into an exception.
     *
     * @param T The JSON type to deserialize the response into
     * @param response The HTTP response to process
     * @return The deserialized response body
     * @throws ApiRequestException When the response status is not successful
     */
    @OptIn(ExperimentalSerializationApi::class)
    protected suspend inline fun <reified T> decodeOrThrow(response: HttpResponse): T {
        if (!response.status.isSuccess()) {
            throwApiError(response)
        }
        return json.decodeFromStream(response.bodyAsChannel().toInputStream())
    }

    /**
     * Translates an error response into an [ApiRequestException].
     *
     * The Portale Planning API reports errors as `{"message": "<key>", "data": [...]}`; when
     * the body matches that envelope the key becomes the exception message.
     *
     * @param response The failed HTTP response
     * @throws ApiRequestException Always
     */
    protected suspend fun throwApiError(response: HttpResponse): Nothing =
        throwApiError(response.status.value, readErrorKey(response))

    private fun throwApiError(statusCode: Int, errorKey: String?): Nothing {
        if (!errorKey.isNullOrBlank()) {
            throw ApiRequestException(statusCode, errorKey)
        }
        throw ApiRequestException(statusCode)
    }

    /**
     * Reads the error key of a `{"message": "<key>", ...}` error body.
     *
     * @param response The failed HTTP response
     * @return The error key, or null if the body doesn't match the envelope
     */
    private suspend fun readErrorKey(response: HttpResponse): String? {
        val errorObject = runCatching { json.parseToJsonElement(response.bodyAsText()) }
            .getOrNull() as? JsonObject
        return (errorObject?.get("message") as? JsonPrimitive)?.contentOrNull
    }
}
