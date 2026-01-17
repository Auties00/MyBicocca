package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Abstract base class for all EasyStaff/Agenda Web API implementations.
 *
 * This class provides common infrastructure for making requests to the
 * Agenda Web application (powered by EasyStaff) and parsing HTML/JSON responses.
 *
 * @param client The shared [HttpClient] instance for making HTTP requests
 */
abstract class EasyStaffAbstractApi(
    protected val client: HttpClient,
    protected val json: Json
) {
    companion object {
        /**
         * Base URL for all Agenda Web requests.
         */
        private const val BASE_URL = "https://gestioneorari.didattica.unimib.it"

        /**
         * Date format used by Agenda Web (DD-MM-YYYY).
         */
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        /**
         * Time format used by Agenda Web (HH:mm).
         */
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * Endpoint to query search options.
         */
        protected const val COMBO_ENDPOINT = "/PortaleStudentiUnimib/combo.php"


        /**
         * API endpoint used by Agenda Web.
         */
        protected const val AGENDA_WEB_ENDPOINT = "/PortaleStudentiUnimib/index.php"

        /**
         * Formats a date to Agenda Web format (DD-MM-YYYY).
         *
         * @param date The date to format
         * @return The formatted date string
         */
        fun formatDate(date: LocalDate): String = date.format(DATE_FORMAT)

        /**
         * Formats a date to Agenda Web time format (HH:mm).
         *
         * @param time The time to format
         * @return The formatted time string
         */
        fun formatTime(time: LocalTime): String = time.format(TIME_FORMAT)

        /**
         * URL-encodes a query parameter value.
         *
         * @param value The value to encode
         * @return The URL-encoded value
         */
        fun encodeQueryValue(value: String): String =
            URLEncoder.encode(value, Charsets.UTF_8.name())

        /**
         * Extracts JSON content from a JavaScript variable assignment.
         *
         * The Agenda Web combo.php endpoint returns data in the format:
         * `var variable_name = [JSON_DATA];`
         *
         * This function extracts the JSON portion.
         *
         * @param jsContent The JavaScript content containing the variable assignment
         * @param variableName The name of the variable to extract
         * @return The JSON string, or null if not found
         */
        fun extractJsonFromJsVariable(jsContent: String, variableName: String): String? {
            val regex = """var\s+$variableName\s*=\s*(.+?);""".toRegex(RegexOption.DOT_MATCHES_ALL)
            return regex.find(jsContent)?.groupValues?.get(1)?.trim()?.removeSuffix(";")
        }
    }

    /**
     * Executes a GET request and returns the parsed HTML document.
     *
     * @param path The request path (relative to BASE_URL)
     * @param queryParams Query parameters to append to the URL
     * @return The parsed HTML [Document]
     */
    protected suspend fun executeGetHtml(
        path: String,
        queryParams: Map<String, List<String>> = emptyMap()
    ): Document {
        val url = buildUrl(path, queryParams)
        val response = client.get(url)
        return Jsoup.parse(response.bodyAsChannel().toInputStream(), null, BASE_URL)
    }

    /**
     * Executes a GET request and returns the raw response body as text.
     *
     * Useful for endpoints that return JSON or JavaScript content.
     *
     * @param path The request path (relative to BASE_URL)
     * @param queryParams Query parameters to append to the URL
     * @return The response body as a string
     */
    protected suspend fun executeGetText(
        path: String,
        queryParams: Map<String, List<String>> = emptyMap()
    ): String {
        val url = buildUrl(path, queryParams)
        val response = client.get(url)
        return response.bodyAsText()
    }

    /**
     * Executes a POST request and returns the raw HTTP response.
     *
     * @param path The request path (relative to BASE_URL)
     * @param body Body of the request
     * @param queryParams Query parameters to append to the URL
     * @return The [HttpResponse]
     */
    @OptIn(ExperimentalSerializationApi::class)
    protected suspend inline fun <reified T> executePostJson(
        path: String,
        body: Any,
        queryParams: Map<String, List<String>> = emptyMap()
    ): T {
        val url = buildUrl(path, queryParams)
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        if(!response.status.isSuccess()) {
            throw IllegalStateException("Invalid status code: ${response.status.value}")
        }
        return json.decodeFromStream(response.bodyAsChannel().toInputStream())
    }

    /**
     * Executes a POST request with form parameters and returns the raw response body as a deserialized JSON type.
     *
     * @param T the JSON type
     * @param path The request path (relative to BASE_URL)
     * @param formParameters The form parameters to send (supports multiple values per key)
     * @return The response body as a JSON
     */
    @OptIn(ExperimentalSerializationApi::class)
    protected suspend inline fun <reified T> executePostForm(
        path: String,
        formParameters: Map<String, List<String>>
    ): T {
        val url = buildUrl(path)
        val response = client.post(url) {
            setBody(FormDataContent(Parameters.build {
                formParameters.forEach { (key, values) ->
                    values.forEach { value ->
                        append(key, value)
                    }
                }
            }))
        }
        if(!response.status.isSuccess()) {
            throw IllegalStateException("Invalid status code: ${response.status.value}")
        }
        return json.decodeFromStream(response.bodyAsChannel().toInputStream())
    }

    /**
     * Builds a full URL from a path and query parameters.
     *
     * @param path The request path (can be relative or absolute)
     * @param queryParams Query parameters to append
     * @return The full URL string
     */
    protected fun buildUrl(path: String, queryParams: Map<String, List<String>> = emptyMap()): String {
        val basePath = when {
            path.startsWith("http") -> path
            path.startsWith("/") -> "$BASE_URL$path"
            else -> "$BASE_URL/$path"
        }
        return if (queryParams.isEmpty()) {
            basePath
        } else {
            val query = queryParams.entries
                .flatMap { (key, values) -> values.map { value -> "$key=${encodeQueryValue(value)}" } }
                .joinToString("&")
            if (basePath.contains("?")) {
                "$basePath&$query"
            } else {
                "$basePath?$query"
            }
        }
    }
}