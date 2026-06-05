package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.remote.common.util.buildUrl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Abstract base class for all Affluences API implementations.
 *
 * This class provides common infrastructure for making requests to the Affluences backends
 * and translating error responses into exceptions:
 * - Structured error bodies become [AffluencesException]
 * - Unparsable error bodies become [ApiRequestException]
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 * @param json The shared [Json] instance for decoding responses
 */
abstract class AffluencesAbstractApi(
    protected val client: HttpClient,
    protected val json: Json
) {
    companion object {
        /**
         * Base URL of the app API v3, used for search and discovery.
         */
        const val APP_API_V3_URL = "https://api.affluences.com/app/v3"

        /**
         * Base URL of the app API v4, used for site pages.
         */
        const val APP_API_V4_URL = "https://api.affluences.com/app/v4"

        /**
         * Base URL of the reservation API, used for resources and bookings.
         */
        const val RESERVATION_API_URL = "https://reservation.affluences.com/api"

        /**
         * Date format used by the Affluences APIs (yyyy-MM-dd).
         */
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        /**
         * Time format used by the Affluences APIs (HH:mm).
         */
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * Formats a date to the Affluences format (yyyy-MM-dd).
         *
         * @param date The date to format
         * @return The formatted date string
         */
        fun formatDate(date: LocalDate): String = date.format(DATE_FORMAT)

        /**
         * Formats a time to the Affluences format (HH:mm).
         *
         * @param time The time to format
         * @return The formatted time string
         */
        fun formatTime(time: LocalTime): String = time.format(TIME_FORMAT)
    }

    /**
     * Executes a GET request and returns the response body as a deserialized JSON type.
     *
     * @param T The JSON type to deserialize the response into
     * @param baseUrl The base URL of the target backend
     * @param path The request path (relative to [baseUrl])
     * @param queryParams Query parameters to append to the URL
     * @param headers Additional headers to send with the request
     * @return The deserialized response body
     * @throws AffluencesException When the server rejects the request with a structured error
     * @throws ApiRequestException When the server rejects the request without a structured error
     */
    protected suspend inline fun <reified T> executeGet(
        baseUrl: String,
        path: String,
        queryParams: Map<String, Any> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ): T {
        val response = client.get(buildUrl(baseUrl, path, queryParams)) {
            headers.forEach { (name, value) -> header(name, value) }
        }
        return decodeOrThrow(response)
    }

    /**
     * Executes a POST request with a JSON body and returns the response body as a deserialized
     * JSON type.
     *
     * @param T The JSON type to deserialize the response into
     * @param baseUrl The base URL of the target backend
     * @param path The request path (relative to [baseUrl])
     * @param body The request body, serialized as JSON
     * @param headers Additional headers to send with the request
     * @return The deserialized response body
     * @throws AffluencesException When the server rejects the request with a structured error
     * @throws ApiRequestException When the server rejects the request without a structured error
     */
    protected suspend inline fun <reified T> executePost(
        baseUrl: String,
        path: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): T {
        val response = client.post(buildUrl(baseUrl, path)) {
            contentType(ContentType.Application.Json)
            setBody(body)
            headers.forEach { (name, value) -> header(name, value) }
        }
        return decodeOrThrow(response)
    }

    /**
     * Executes a DELETE request and returns the response body as a deserialized JSON type.
     *
     * @param T The JSON type to deserialize the response into
     * @param baseUrl The base URL of the target backend
     * @param path The request path (relative to [baseUrl])
     * @param queryParams Query parameters to append to the URL
     * @return The deserialized response body
     * @throws AffluencesException When the server rejects the request with a structured error
     * @throws ApiRequestException When the server rejects the request without a structured error
     */
    protected suspend inline fun <reified T> executeDelete(
        baseUrl: String,
        path: String,
        queryParams: Map<String, Any> = emptyMap()
    ): T {
        val response = client.delete(buildUrl(baseUrl, path, queryParams))
        return decodeOrThrow(response)
    }

    /**
     * Deserializes a successful response, or translates an error response into an exception.
     *
     * @param T The JSON type to deserialize the response into
     * @param response The HTTP response to process
     * @return The deserialized response body
     * @throws AffluencesException When the response carries a structured error
     * @throws ApiRequestException When the response status is not successful and the body is not
     * a structured error
     */
    @OptIn(ExperimentalSerializationApi::class)
    protected suspend inline fun <reified T> decodeOrThrow(response: HttpResponse): T {
        if (!response.status.isSuccess()) {
            throwApiError(response)
        }
        return json.decodeFromStream(response.bodyAsChannel().toInputStream())
    }

    /**
     * Translates an error response into the most specific exception possible.
     *
     * Two structured error envelopes are recognized:
     * - App API: `{"error": {"code": "...", "message": "..."}}`
     * - Reservation API: `{"error": "...", "errorMessage": "..."}`
     *
     * @param response The failed HTTP response
     * @throws AffluencesException When the body matches a structured error envelope
     * @throws ApiRequestException Otherwise
     */
    protected suspend fun throwApiError(response: HttpResponse): Nothing {
        val errorObject = runCatching { json.parseToJsonElement(response.bodyAsText()) }
            .getOrNull() as? JsonObject
        when (val error = errorObject?.get("error")) {
            is JsonObject -> {
                val code = (error["code"] as? JsonPrimitive)?.contentOrNull
                val message = (error["message"] as? JsonPrimitive)?.contentOrNull
                if (code != null || message != null) {
                    throw AffluencesException(code, message ?: "Request failed with error code: $code")
                }
            }

            is JsonPrimitive -> {
                if (error.isString) {
                    val message = (errorObject["errorMessage"] as? JsonPrimitive)?.contentOrNull
                    throw AffluencesException(error.content, message ?: error.content)
                }
            }

            else -> {
                // No structured error in the body, fall through to the generic exception
            }
        }
        throw ApiRequestException(response.status.value)
    }
}
