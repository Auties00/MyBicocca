package it.attendance100.mybicocca.data.remote.elearning.api

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
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Node

/**
 * Base class of the generated Moodle web page modules: fetches a page and reads it with one of
 * [it.attendance100.mybicocca.data.remote.elearning.dto.MoodleWebScrapers].
 *
 * Whether a request carries the user's session is up to the client a module is built with:
 * [ElearningApi] builds the catalog on a client without cookies and the media pages on its own.
 *
 * @param basePath Path prefix of every operation of the module (empty for these pages).
 */
abstract class MoodleWebAbstractApi(
    protected val client: HttpClient,
    protected val json: Json,
    private val basePath: String,
) {
    protected suspend fun <T> executeHtmlGet(
        endpoint: String,
        server: MoodleWebServer,
        scrape: (Node) -> T?,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = readPage(execute(HttpMethod.Get, server, endpoint, block), scrape)

    protected suspend fun <T> executeHtmlPost(
        endpoint: String,
        server: MoodleWebServer,
        scrape: (Node) -> T?,
        block: HttpRequestBuilder.() -> Unit = {},
    ): T = readPage(execute(HttpMethod.Post, server, endpoint, block), scrape)

    private suspend fun <T> readPage(response: HttpResponse, scrape: (Node) -> T?): T {
        val url = response.request.url.toString()
        return scrape(Jsoup.parse(response.bodyAsText(), url))
            ?: throw HtmlParsingException("$url doesn't hold what was expected; the page may have changed")
    }

    /** Sends the request, throwing [ApiRequestException] on an error status. */
    private suspend fun execute(
        method: HttpMethod,
        server: MoodleWebServer,
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
