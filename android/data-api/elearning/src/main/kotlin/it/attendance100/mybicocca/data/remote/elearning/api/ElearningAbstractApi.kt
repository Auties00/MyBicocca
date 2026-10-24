package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import it.attendance100.mybicocca.data.remote.common.util.buildUrl
import kotlinx.serialization.json.Json

/**
 * Base class of the hand-written e-learning APIs, which scrape pages or call endpoints outside
 * Moodle's web services (those are in [MoodleApi]).
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 * @param json The shared [Json] instance for serialization/deserialization
 */
abstract class ElearningAbstractApi(
    protected val client: HttpClient,
    protected val json: Json
) {
    companion object {
        /**
         * Base URL for all Moodle web service requests.
         */
        const val BASE_URL = "https://elearning.unimib.it/"

        /**
         * Expected length of a valid Moodle web service token.
         */
        const val WS_TOKEN_LENGTH = 32

        /**
         * Moodle langpack code sent as `moodlewssettinglang` until the caller sets another one
         * (see [ElearningApi.language]).
         */
        const val DEFAULT_LANGUAGE = "en"
    }

    /**
     * Executes a GET request to the specified endpoint.
     *
     * @param endpoint The API endpoint (relative to BASE_URL, should start with /)
     * @param block Optional configuration block for the request
     * @return the HTTP response
     * @throws ElearningException If the response status is not successful
     */
    protected suspend inline fun executeGet(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.get(buildUrl(BASE_URL, endpoint), block)
    }

    /**
     * Executes a POST request without expecting a response body.
     *
     * @param endpoint The API endpoint (relative to BASE_URL, should start with /)
     * @param block Optional configuration block for the request
     * @return the HTTP response
     * @throws ElearningException If the response status is not successful
     */
    protected suspend inline fun executePost(
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): HttpResponse {
        return client.post(buildUrl(BASE_URL, endpoint), block)
    }
}
