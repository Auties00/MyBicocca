package it.attendance100.mybicocca.data.remote.esse3.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import it.attendance100.mybicocca.data.remote.common.util.buildUrl
import it.attendance100.mybicocca.data.remote.common.util.isRedirect
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3ErrorResponse
import it.attendance100.mybicocca.data.remote.esse3.dto.Esse3PermissionLevel
import it.attendance100.mybicocca.data.remote.esse3.exception.Esse3Exception
import kotlinx.serialization.json.Json

/**
 * Abstract base class for all ESSE3 REST API implementations.
 *
 * This class provides the common infrastructure for making requests to the
 * ESSE3 REST API. Each concrete API implementation specifies its own service
 * base path (e.g., `/libretto-service-v2`).
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 * @param json The shared [Json] instance for serialization/deserialization
 * @param serviceBasePath The service-specific path prefix (e.g., "/libretto-service-v2")
 */
abstract class Esse3AbstractApi(
    protected val client: HttpClient,
    protected val json: Json,
    private val serviceBasePath: String
) {
    companion object {
        /**
         * Base URL for all ESSE3 REST API requests.
         */
        protected const val BASE_URL = "https://s3w.si.unimib.it/e3rest/api"
    }

    /**
     * Builds the full URL for a given endpoint, prepending the service base path.
     */
    @PublishedApi
    internal fun buildServiceUrl(endpoint: String): String {
        return buildUrl(BASE_URL, "$serviceBasePath$endpoint")
    }

    /**
     * Checks a response for error status codes and throws appropriate exceptions.
     *
     * @throws Esse3Exception for HTTP 403 (Forbidden)
     * @throws Esse3Exception for HTTP 422 (Unprocessable Entity)
     * @throws Esse3Exception for other non-success status codes
     */
    @PublishedApi
    internal suspend fun ensureSuccess(
        response: HttpResponse,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet()
    ) {
        if (response.status.isSuccess() || response.status.isRedirect()) return
        val error = runCatching {
            response.body<Esse3ErrorResponse>()
        }.getOrElse {
            Esse3ErrorResponse(
                statusCode = response.status.value,
                returnCode = -1,
                errorMessage = "Request failed with status: ${response.status}"
            )
        }
        throw Esse3Exception(
            expectedPermissionLevels = expectedPermissionLevels,
            errorResponse = error
        )
    }

    /**
     * Executes a GET request to the specified endpoint.
     *
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The HTTP response
     */
    protected suspend inline fun executeGet(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.get(buildServiceUrl(endpoint), block)
    }

    /**
     * Executes a GET request and deserializes the JSON response as a list,
     * returning an empty list if the server responds with HTTP 404.
     */
    protected suspend inline fun <reified T> executeJsonGetList(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): List<T> {
        val response = client.get(buildServiceUrl(endpoint), block)
        if (response.status == HttpStatusCode.NotFound) return emptyList()
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<List<T>>()
    }

    /**
     * Executes a POST request and deserializes the JSON response as a list,
     * returning an empty list if the server responds with HTTP 404.
     */
    protected suspend inline fun <reified T> executeJsonPostList(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): List<T> {
        val response = client.post(buildServiceUrl(endpoint), block)
        if (response.status == HttpStatusCode.NotFound) return emptyList()
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<List<T>>()
    }

    /**
     * Executes a PUT request and deserializes the JSON response as a list,
     * returning an empty list if the server responds with HTTP 404.
     */
    protected suspend inline fun <reified T> executeJsonPutList(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): List<T> {
        val response = client.put(buildServiceUrl(endpoint), block)
        if (response.status == HttpStatusCode.NotFound) return emptyList()
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<List<T>>()
    }

    /**
     * Executes a DELETE request and deserializes the JSON response as a list,
     * returning an empty list if the server responds with HTTP 404.
     */
    protected suspend inline fun <reified T> executeJsonDeleteList(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): List<T> {
        val response = client.delete(buildServiceUrl(endpoint), block)
        if (response.status == HttpStatusCode.NotFound) return emptyList()
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<List<T>>()
    }

    /**
     * Executes a PATCH request and deserializes the JSON response as a list,
     * returning an empty list if the server responds with HTTP 404.
     */
    protected suspend inline fun <reified T> executeJsonPatchList(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): List<T> {
        val response = client.patch(buildServiceUrl(endpoint), block)
        if (response.status == HttpStatusCode.NotFound) return emptyList()
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<List<T>>()
    }

    /**
     * Executes a GET request and deserializes the JSON response.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws Esse3Exception If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonGet(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.get(buildServiceUrl(endpoint), block)
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<T>()
    }

    /**
     * Executes a POST request to the specified endpoint.
     *
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The HTTP response
     */
    protected suspend inline fun executePost(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.post(buildServiceUrl(endpoint), block)
    }

    /**
     * Executes a POST request and deserializes the JSON response.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws Esse3Exception If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonPost(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.post(buildServiceUrl(endpoint), block)
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<T>()
    }

    /**
     * Executes a PUT request and deserializes the JSON response.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws Esse3Exception If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonPut(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.put(buildServiceUrl(endpoint), block)
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<T>()
    }

    /**
     * Executes a DELETE request and deserializes the JSON response.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws Esse3Exception If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonDelete(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.delete(buildServiceUrl(endpoint), block)
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<T>()
    }

    /**
     * Executes a DELETE request to the specified endpoint.
     *
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The HTTP response
     */
    protected suspend inline fun executeDelete(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.delete(buildServiceUrl(endpoint), block)
    }

    /**
     * Executes a PATCH request and deserializes the JSON response.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws Esse3Exception If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonPatch(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.patch(buildServiceUrl(endpoint), block)
        ensureSuccess(response, expectedPermissionLevels)
        return response.body<T>()
    }

    /**
     * Executes a GET request and returns the response body as a [ByteReadChannel]
     * for streaming consumption.
     */
    protected suspend inline fun executeStreamGet(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): ByteReadChannel {
        val response = client.get(buildServiceUrl(endpoint)) {
            accept(ContentType.Application.OctetStream)
            block()
        }
        ensureSuccess(response, expectedPermissionLevels)
        return response.bodyAsChannel()
    }

    /**
     * Executes a POST request and returns the response body as a [ByteReadChannel]
     * for streaming consumption.
     */
    protected suspend inline fun executeStreamPost(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): ByteReadChannel {
        val response = client.post(buildServiceUrl(endpoint)) {
            accept(ContentType.Application.OctetStream)
            block()
        }
        ensureSuccess(response, expectedPermissionLevels)
        return response.bodyAsChannel()
    }

    /**
     * Executes a PUT request and returns the response body as a [ByteReadChannel]
     * for streaming consumption.
     */
    protected suspend inline fun executeStreamPut(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): ByteReadChannel {
        val response = client.put(buildServiceUrl(endpoint)) {
            accept(ContentType.Application.OctetStream)
            block()
        }
        ensureSuccess(response, expectedPermissionLevels)
        return response.bodyAsChannel()
    }

    /**
     * Executes a DELETE request and returns the response body as a [ByteReadChannel]
     * for streaming consumption.
     */
    protected suspend inline fun executeStreamDelete(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): ByteReadChannel {
        val response = client.delete(buildServiceUrl(endpoint)) {
            accept(ContentType.Application.OctetStream)
            block()
        }
        ensureSuccess(response, expectedPermissionLevels)
        return response.bodyAsChannel()
    }

    /**
     * Executes a PATCH request and returns the response body as a [ByteReadChannel]
     * for streaming consumption.
     */
    protected suspend inline fun executeStreamPatch(
        endpoint: String,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet(),
        block: HttpRequestBuilder.() -> Unit = {}
    ): ByteReadChannel {
        val response = client.patch(buildServiceUrl(endpoint)) {
            accept(ContentType.Application.OctetStream)
            block()
        }
        ensureSuccess(response, expectedPermissionLevels)
        return response.bodyAsChannel()
    }

    /**
     * Executes a PUT request to the specified endpoint.
     *
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The HTTP response
     */
    protected suspend inline fun executePut(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.put(buildServiceUrl(endpoint), block)
    }
}