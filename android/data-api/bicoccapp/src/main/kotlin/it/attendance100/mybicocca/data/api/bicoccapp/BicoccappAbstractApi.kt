package it.attendance100.mybicocca.data.api.bicoccapp

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import it.attendance100.mybicocca.data.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.common.util.buildUrl
import it.attendance100.mybicocca.data.common.util.isRedirect
import kotlinx.serialization.json.Json

/**
 * Abstract base class for all Bicoccapp API implementations.
 *
 * This class provides the common infrastructure for making authenticated requests
 * to the Bicoccapp REST API. All concrete API implementations should extend
 * this class.
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 * @param json The shared [Json] instance for serialization/deserialization
 */
abstract class BicoccappAbstractApi(
    protected val client: HttpClient,
    protected val json: Json
) {
    companion object {
        /**
         * Base URL for all Bicoccapp API requests.
         */
        protected const val BASE_URL = "https://backoffice-app.unimib.it/api/v1"
    }

    /**
     * Executes a GET request to the specified endpoint.
     *
     * @param endpoint The API endpoint (relative to BASE_URL, should start with /)
     * @param block Optional configuration block for the request
     * @return the HTTP response
     * @throws ApiRequestException If the response status is not successful
     */
    protected suspend inline fun executeGet(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.get(buildUrl(BASE_URL, endpoint), block)
    }

    /**
     * Executes a GET request to the specified endpoint.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to BASE_URL, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws ApiRequestException If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonGet(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.get(buildUrl(BASE_URL, endpoint), block)
        if (!response.status.isSuccess() && !response.status.isRedirect()) {
            throw ApiRequestException(response.status.value)
        }
        return response.body<T>()
    }

    /**
     * Executes a POST request without expecting a response body.
     *
     * @param endpoint The API endpoint (relative to BASE_URL, should start with /)
     * @param block Optional configuration block for the request
     * @return the HTTP response
     * @throws ApiRequestException If the response status is not successful
     */
    protected suspend inline fun executePost(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.post(buildUrl(BASE_URL, endpoint), block)
    }

    /**
     * Executes a POST request to the specified endpoint.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to BASE_URL, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws ApiRequestException If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonPost(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.post(buildUrl(BASE_URL, endpoint), block)
        if (!response.status.isSuccess() && !response.status.isRedirect()) {
            throw ApiRequestException(response.status.value)
        }
        return response.body<T>()
    }

    /**
     * Executes a DELETE request to the specified endpoint.
     *
     * @param T The expected response type
     * @param endpoint The API endpoint (relative to BASE_URL, should start with /)
     * @param block Optional configuration block for the request
     * @return The parsed response of type T
     * @throws ApiRequestException If the response status is not successful
     */
    protected suspend inline fun <reified T> executeJsonDelete(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = client.delete(buildUrl(BASE_URL, endpoint), block)
        if (!response.status.isSuccess() && !response.status.isRedirect()) {
            throw ApiRequestException(response.status.value)
        }
        return response.body<T>()
    }
}
