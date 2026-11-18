package it.attendance100.mybicocca.data.remote.easystaff.api

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import it.attendance100.mybicocca.data.remote.common.exception.ApiRequestException
import it.attendance100.mybicocca.data.remote.common.exception.HtmlParsingException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import org.jsoup.Jsoup
import org.jsoup.nodes.Node

/**
 * Base class of the generated Agenda Web and EasyBadge modules: sends a request and reads its
 * response as JSON, as the JSON a script assigns to a variable (`var elenco_sedi = [...];`, as
 * `combo.php` declares the options of its forms) or as an HTML page.
 *
 * Every response is read whatever its content type, since Agenda Web answers JSON as `text/html`.
 *
 * @param basePath Path prefix of every operation of the module (empty for Agenda Web).
 */
abstract class EasyStaffAbstractApi(
    protected val client: HttpClient,
    @PublishedApi internal val json: Json,
    private val basePath: String,
) {
    protected suspend inline fun <reified T> executeJsonGet(
        endpoint: String,
        server: EasyStaffServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = executeJsonGet(endpoint, server, envelope, serializer<T>(), block)

    protected suspend fun <T> executeJsonGet(
        endpoint: String,
        server: EasyStaffServer,
        envelope: List<String> = emptyList(),
        deserializer: DeserializationStrategy<T>,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = decode(execute(HttpMethod.Get, server, endpoint, block).bodyAsText(), envelope, deserializer)

    protected suspend inline fun <reified T> executeJsonGetList(
        endpoint: String,
        server: EasyStaffServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): List<T> = executeJsonGet(endpoint, server, envelope, serializer<List<T>>(), block)

    protected suspend inline fun <reified T> executeJsonPost(
        endpoint: String,
        server: EasyStaffServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = executeJsonPost(endpoint, server, envelope, serializer<T>(), block)

    protected suspend fun <T> executeJsonPost(
        endpoint: String,
        server: EasyStaffServer,
        envelope: List<String> = emptyList(),
        deserializer: DeserializationStrategy<T>,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = decode(execute(HttpMethod.Post, server, endpoint, block).bodyAsText(), envelope, deserializer)

    protected suspend inline fun <reified T> executeJsonPostList(
        endpoint: String,
        server: EasyStaffServer,
        envelope: List<String> = emptyList(),
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): List<T> = executeJsonPost(endpoint, server, envelope, serializer<List<T>>(), block)

    protected suspend inline fun <reified T> executeScriptGet(
        endpoint: String,
        server: EasyStaffServer,
        variable: String,
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = executeScriptGet(endpoint, server, variable, serializer<T>(), block)

    protected suspend fun <T> executeScriptGet(
        endpoint: String,
        server: EasyStaffServer,
        variable: String,
        deserializer: DeserializationStrategy<T>,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = readScript(execute(HttpMethod.Get, server, endpoint, block), variable, deserializer)

    protected suspend inline fun <reified T> executeScriptPost(
        endpoint: String,
        server: EasyStaffServer,
        variable: String,
        noinline block: HttpRequestBuilder.() -> Unit = {},
    ): T = executeScriptPost(endpoint, server, variable, serializer<T>(), block)

    protected suspend fun <T> executeScriptPost(
        endpoint: String,
        server: EasyStaffServer,
        variable: String,
        deserializer: DeserializationStrategy<T>,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = readScript(execute(HttpMethod.Post, server, endpoint, block), variable, deserializer)

    protected suspend fun <T> executeHtmlGet(
        endpoint: String,
        server: EasyStaffServer,
        scrape: (Node) -> T?,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = readPage(execute(HttpMethod.Get, server, endpoint, block), scrape)

    protected suspend fun <T> executeHtmlPost(
        endpoint: String,
        server: EasyStaffServer,
        scrape: (Node) -> T?,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = readPage(execute(HttpMethod.Post, server, endpoint, block), scrape)

    private fun <T> decode(body: String, envelope: List<String>, deserializer: DeserializationStrategy<T>): T {
        val element = envelope.fold(json.parseToJsonElement(body)) { value, key -> value.jsonObject[key] ?: JsonObject(emptyMap()) }
        return json.decodeFromJsonElement(deserializer, element)
    }

    private suspend fun <T> readScript(response: HttpResponse, variable: String, deserializer: DeserializationStrategy<T>): T {
        // The variable is declared (var name = …;) or assigned (window.name = …;) on a line of its own.
        val prefix = "$variable ="
        val assignment = response.bodyAsText().lineSequence()
            .map { line -> DECLARATION_KEYWORDS.fold(line.trim()) { statement, keyword -> statement.removePrefix(keyword) } }
            .firstOrNull { it.startsWith(prefix) }
            ?: throw HtmlParsingException("${response.request.url} assigns no '$variable'")
        val value: JsonElement = json.parseToJsonElement(assignment.removePrefix(prefix).trim().removeSuffix(";"))
        return json.decodeFromJsonElement(deserializer, value)
    }

    private suspend fun <T> readPage(response: HttpResponse, scrape: (Node) -> T?): T {
        val url = response.request.url.toString()
        return scrape(Jsoup.parse(response.bodyAsText(), url))
            ?: throw HtmlParsingException("$url doesn't hold what was expected; the page may have changed")
    }

    /** Sends the request, throwing [ApiRequestException] on an error status. */
    private suspend fun execute(
        method: HttpMethod,
        server: EasyStaffServer,
        endpoint: String,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val response = client.request(server.url.trimEnd('/') + basePath + endpoint) {
            this.method = method
            block()
        }
        if (!response.status.isSuccess()) throw ApiRequestException(response.status.value)
        return response
    }
}

/** Keywords a script may declare a variable with, before its name. */
private val DECLARATION_KEYWORDS = listOf("var ", "let ", "const ")
