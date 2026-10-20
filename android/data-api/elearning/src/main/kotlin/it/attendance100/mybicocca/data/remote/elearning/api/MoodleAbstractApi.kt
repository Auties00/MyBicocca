package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Base class of the generated Moodle modules: posts a web service call to the site's REST
 * server and decodes its result, translating Moodle's errors into [ElearningException].
 *
 * Moodle answers errors with HTTP 200 and one of two bodies:
 * - `{"exception": "…", "errorcode": "…", "message": "…"}` when the function fails;
 * - `{"error": "…", "errorcode": "…"}` when the server rejects the call, such as for an
 *   invalid token.
 *
 * @param basePath Path prefix of every operation of the module (empty for Moodle).
 */
abstract class MoodleAbstractApi(
    protected val client: HttpClient,
    @PublishedApi internal val json: Json,
    private val basePath: String,
) {
    protected suspend inline fun <reified T> executeJsonPost(
        endpoint: String,
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = json.decodeFromJsonElement(send(endpoint, block))

    protected suspend inline fun <reified T> executeJsonPostList(
        endpoint: String,
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): List<T> = json.decodeFromJsonElement(send(endpoint, block))

    /** Posts a call whose result isn't needed; [ensureSuccess] checks it. */
    protected suspend fun executePost(endpoint: String, block: HttpRequestBuilder.() -> Unit = {}): JsonElement {
        val response = client.post(MoodleRuntime.BASE_URL + basePath + endpoint) {
            block()
            // Filter text and rewrite file URLs as the mobile app does, so that links work
            // with the token.
            url.parameters.append("moodlewssettingfilter", "true")
            url.parameters.append("moodlewssettingfileurl", "true")
        }
        if (!response.status.isSuccess()) {
            throw ElearningException(null, "Invalid response status: ${response.status}")
        }
        return json.parseToJsonElement(response.bodyAsText().ifBlank { "null" })
    }

    /** Throws the error [result] reports, if it is one. */
    protected fun ensureSuccess(result: JsonElement) {
        (result as? JsonObject)?.let(::error)?.let { throw it }
    }

    /** Posts the call and returns its result as JSON, throwing on an error. */
    @PublishedApi
    internal suspend fun send(endpoint: String, block: HttpRequestBuilder.() -> Unit): JsonElement =
        executePost(endpoint, block).also(::ensureSuccess)

    /** The error a result reports, if it is one. */
    private fun error(body: JsonObject): ElearningException? {
        val failed = body.containsKey("exception") || body.containsKey("errorcode") ||
            (body["error"] as? JsonPrimitive)?.let { it.booleanOrNull == true || it.isString } == true
        if (!failed) return null
        val code = (body["errorcode"] as? JsonPrimitive)?.contentOrNull
        val message = (body["message"] as? JsonPrimitive)?.contentOrNull
            ?: (body["error"] as? JsonPrimitive)?.takeIf { it.isString }?.content
            ?: "unknown error"
        return ElearningException(code, message)
    }
}
