package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.common.util.buildUrl
import it.attendance100.mybicocca.data.common.util.isRedirect
import it.attendance100.mybicocca.data.dto.esse3.Esse3ErrorResponse
import it.attendance100.mybicocca.data.dto.esse3.Esse3PermissionLevel
import it.attendance100.mybicocca.data.exception.esse3.Esse3NotAuthorizedException
import it.attendance100.mybicocca.data.exception.esse3.Esse3ValidationException
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
     * @throws Esse3NotAuthorizedException for HTTP 403 (Forbidden)
     * @throws Esse3ValidationException for HTTP 422 (Unprocessable Entity)
     * @throws Esse3ValidationException for other non-success status codes
     */
    @PublishedApi
    internal suspend fun ensureSuccess(
        response: HttpResponse,
        expectedPermissionLevels: Set<Esse3PermissionLevel> = emptySet()
    ) {
        if (response.status.isSuccess() || response.status.isRedirect()) return
        val error = runCatching { response.body<Esse3ErrorResponse>() }.getOrNull()
        if (response.status == HttpStatusCode.Forbidden) {
            throw Esse3NotAuthorizedException(
                expectedPermissionLevels = expectedPermissionLevels,
                apiErrorMessage = error?.errorMessage
            )
        }
        throw Esse3ValidationException(
            error ?: Esse3ErrorResponse(
                statusCode = response.status.value,
                returnCode = -1,
                errorMessage = "Request failed with status: ${response.status}"
            )
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
     * Executes a GET request and deserializes the JSON response.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to service base path, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws Esse3ValidationException If the response status is not successful
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
     * @throws Esse3ValidationException If the response status is not successful
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
     * @throws Esse3ValidationException If the response status is not successful
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
     * @throws Esse3ValidationException If the response status is not successful
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
     * @throws Esse3ValidationException If the response status is not successful
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