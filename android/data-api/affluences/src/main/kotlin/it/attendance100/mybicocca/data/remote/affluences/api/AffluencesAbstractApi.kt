package it.attendance100.mybicocca.data.remote.affluences.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import it.attendance100.mybicocca.data.remote.affluences.exception.AffluencesException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Base class of the generated Affluences modules: sends a request to one of the
 * [AffluencesServer]s, translates error responses into [AffluencesException] and decodes the
 * value nested in the response's envelope.
 *
 * @param basePath Path prefix of every operation of the module (empty for Affluences).
 */
abstract class AffluencesAbstractApi(
    protected val client: HttpClient,
    @PublishedApi internal val json: Json,
    private val basePath: String,
) {
    protected suspend inline fun <reified T> executeJsonGet(
        endpoint: String,
        server: AffluencesServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = decode(send(HttpMethod.Get, server, endpoint, block), envelope)

    protected suspend inline fun <reified T> executeJsonGetList(
        endpoint: String,
        server: AffluencesServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): List<T> = decode(send(HttpMethod.Get, server, endpoint, block), envelope)

    protected suspend inline fun <reified T> executeJsonPost(
        endpoint: String,
        server: AffluencesServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = decode(send(HttpMethod.Post, server, endpoint, block), envelope)

    protected suspend inline fun <reified T> executeJsonPostList(
        endpoint: String,
        server: AffluencesServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): List<T> = decode(send(HttpMethod.Post, server, endpoint, block), envelope)

    protected suspend inline fun <reified T> executeJsonDelete(
        endpoint: String,
        server: AffluencesServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = decode(send(HttpMethod.Delete, server, endpoint, block), envelope)

    /** Sends the request and returns its body as JSON, throwing on an error status. */
    @PublishedApi
    internal suspend fun send(
        method: HttpMethod,
        server: AffluencesServer,
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit,
    ): JsonElement {
        val response = client.request(server.url.trimEnd('/') + basePath + endpoint) {
            this.method = method
            block()
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) throw error(response, body)
        return json.parseToJsonElement(body)
    }

    @PublishedApi
    internal inline fun <reified T> decode(body: JsonElement, envelope: List<String>): T =
        json.decodeFromJsonElement(envelope.fold(body) { value, key -> value.jsonObject[key] ?: JsonObject(emptyMap()) })

    /**
     * Reads the error envelope of a failed response. The two backends use different ones:
     * - App API: `{"error": {"code": "…", "message": "…"}}`
     * - Reservation API: `{"error": "…", "errorMessage": "…"}`, or `error_message`
     */
    private fun error(response: HttpResponse, body: String): AffluencesException {
        val envelope = runCatching { json.parseToJsonElement(body) }.getOrNull() as? JsonObject
        val (code, message) = when (val error = envelope?.get("error")) {
            is JsonObject -> error.string("code") to error.string("message")
            is JsonPrimitive -> error.contentOrNull to (envelope.string("errorMessage") ?: envelope.string("error_message"))
            else -> null to null
        }
        return AffluencesException(
            statusCode = response.status.value,
            errorCode = code,
            errorMessage = message ?: code ?: "Request failed with status ${response.status.value}",
        )
    }

    private fun JsonObject.string(key: String): String? = (get(key) as? JsonPrimitive)?.contentOrNull
}
