package it.attendance100.mybicocca.data.api.esse3

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Abstract base class for all Esse3 API implementations.
 *
 * This class provides common infrastructure for making authenticated requests
 * to the Esse3 web application and parsing HTML responses.
 *
 * Unlike REST APIs, Esse3 uses:
 * - Session-based authentication with cookies (JSESSIONID)
 * - HTML responses that need to be parsed with JSoup
 * - Form-based data submission
 * - XPath-like field names for form inputs
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 */
abstract class Esse3AbstractApi(
    protected val client: HttpClient
) {
    companion object {
        /**
         * Base URL for all Esse3 requests.
         */
        const val BASE_URL = "https://s3w.si.unimib.it"

        /**
         * Date format used by Esse3 (DD/MM/YYYY).
         */
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        /**
         * DateTime format used by Esse3 (DD/MM/YYYY HH:mm).
         */
        val DATETIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

        /**
         * Parses a date string in Esse3 format.
         */
        fun parseDate(value: String): LocalDate? = runCatching {
            LocalDate.parse(value.trim(), DATE_FORMAT)
        }.getOrNull()

        /**
         * Parses a datetime string in Esse3 format.
         */
        fun parseDateTime(value: String): LocalDateTime? = runCatching {
            LocalDateTime.parse(value.trim(), DATETIME_FORMAT)
        }.getOrElse {
            parseDate(value)?.atStartOfDay()
        }

        /**
         * Formats a date to Esse3 format.
         */
        fun formatDate(date: LocalDate): String = date.format(DATE_FORMAT)

        /**
         * URL-encodes a form field value.
         */
        fun encodeFormValue(value: String): String =
            URLEncoder.encode(value, Charsets.UTF_8.name())
    }

    /**
     * Executes an authenticated GET request.
     *
     * @param path The request path (relative to BASE_URL)
     * @param queryParams Optional query parameters
     * @return The parsed HTML document
     * @throws IllegalStateException if the page is not found or session data is invalid
     */
    protected suspend fun executeGet(
        path: String,
        queryParams: Map<String, String> = emptyMap()
    ): Document {
        val url = buildUrl(path, queryParams)
        val response = client.get(url)
        val doc = Jsoup.parse(response.bodyAsChannel().toInputStream(), null, BASE_URL)
        checkForErrorPage(doc)
        return doc
    }

    /**
     * Executes an authenticated GET request and returns raw response.
     *
     * @param path The request path (relative to BASE_URL)
     * @param queryParams Optional query parameters
     * @return The HTTP response
     */
    protected suspend fun executeGetRaw(
        path: String,
        queryParams: Map<String, String> = emptyMap()
    ): HttpResponse {
        val url = buildUrl(path, queryParams)
        return client.get(url)
    }

    /**
     * Executes an authenticated POST request with form data.
     *
     * @param path The request path (relative to BASE_URL)
     * @param formData The form data to submit
     * @param queryParams Optional query parameters
     * @return The parsed HTML document
     * @throws IllegalStateException if the page is not found or session data is invalid
     */
    protected suspend fun executePost(
        path: String,
        formData: Map<String, String>,
        queryParams: Map<String, String> = emptyMap()
    ): Document {
        val url = buildUrl(path, queryParams)
        val parameters = ParametersBuilder().apply {
            formData.forEach { (key, value) ->
                append(key, value)
            }
        }.build()

        val response = client.post(url) {
            setBody(FormDataContent(parameters))
        }

        val doc = Jsoup.parse(response.bodyAsChannel().toInputStream(), null, BASE_URL)
        checkForErrorPage(doc)
        return doc
    }

    /**
     * Executes an authenticated POST request and returns raw response.
     */
    protected suspend fun executePostRaw(
        path: String,
        parameters: Parameters,
        queryParams: Map<String, String> = emptyMap()
    ): HttpResponse {
        val url = buildUrl(path, queryParams)
        val response = client.post(url) {
            setBody(FormDataContent(parameters))
        }
        if(response.status == HttpStatusCode.Found) {
            val redirectUrl = response.headers[HttpHeaders.Location]
            if (redirectUrl != null) {
                return executeGetRaw(redirectUrl)
            }
        }
        return response;
    }

    /**
     * Builds a full URL from path and query parameters.
     */
    protected fun buildUrl(path: String, queryParams: Map<String, String> = emptyMap()): String {
        val basePath = if (path.startsWith("/")) "$BASE_URL$path" else "$BASE_URL/$path"
        return if (queryParams.isEmpty()) {
            basePath
        } else {
            val query = queryParams.entries.joinToString("&") { (k, v) ->
                "$k=${encodeFormValue(v)}"
            }
            "$basePath?$query"
        }
    }

    /**
     * Checks if the document is an error page and throws an exception if so.
     */
    protected fun checkForErrorPage(doc: Document) {
        // Check for "Messaggio" page which indicates an error
        val title = doc.selectFirst("title")?.text() ?: ""
        if (!title.contains("Messaggio", ignoreCase = true)) {
            return
        }

        // Look for error message headers
        val h2Text = doc.select("h2").text()

        // Check for "page not found" error
        if (h2Text.contains("Pagina non trovata", ignoreCase = true)) {
            throw IllegalStateException("Page not found: the requested page does not exist")
        }

        // Check for "session data not found" error
        if (h2Text.contains("Dati non trovati in sessione", ignoreCase = true)) {
            throw IllegalStateException(
                "Session data not found: the page requires proper navigation flow. " +
                        "Use the menu entry point URL with menu_opened_cod parameter."
            )
        }
    }

    /**
     * Checks if an update was successful
     */
    protected suspend fun checkForUpdateError(response: HttpResponse) {
        if (response.status.value != 200) {
            throw IllegalStateException("Invalid response status: ${response.status.value}")
        }

        val document = Jsoup.parse(response.bodyAsChannel().toInputStream(), null, BASE_URL)

        val alertError = document.selectFirst("#alertError")
        if (alertError != null) {
            var errorMessage = alertError.text()
            if(errorMessage.isBlank()) {
                errorMessage = "unknown error"
            }
            throw IllegalStateException(errorMessage)
        }
    }

    /**
     * Extracts the page title from a document.
     */
    protected fun Document.pageTitle(): String? {
        return selectFirst("title")?.text()?.let { title ->
            // Remove "Segreterie OnLine - ..." suffix
            title.split(",").firstOrNull()?.trim()
        }
    }

    /**
     * Extracts hidden form fields from a form.
     */
    protected fun Element.extractHiddenFields(): Map<String, String> {
        return select("input[type=hidden]").associate { input ->
            input.attr("name") to input.attr("value")
        }
    }

}