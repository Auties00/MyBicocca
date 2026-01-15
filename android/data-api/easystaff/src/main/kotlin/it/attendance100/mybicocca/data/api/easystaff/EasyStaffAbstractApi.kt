package it.attendance100.mybicocca.data.api.easystaff

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.json.Json
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
 * The Agenda Web platform uses:
 * - GET requests with query parameters
 * - HTML responses parsed with JSoup
 * - JSON data wrapped in JavaScript variable assignments for AJAX calls
 * - Date format DD-MM-YYYY
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
        const val BASE_URL = "https://gestioneorari.didattica.unimib.it"

        /**
         * Date format used by Agenda Web (DD-MM-YYYY).
         */
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        /**
         * Alternative date format used in some responses (DD/MM/YYYY).
         */
        private val DATE_FORMAT_SLASH: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        /**
         * Time format used by Agenda Web (HH:mm).
         */
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * Time range format used by Agenda Web.
         */
        private val TIME_RANGE_REGEX = """(\d{2}:\d{2})\s*-\s*(\d{2}:\d{2})""".toRegex()

        /**
         * Time entry format used by Agenda Web.
         */
        private val TIME_SINGLE_REGEX = """(\d{2}:\d{2})""".toRegex()

        /**
         * API endpoint used by Agenda Web.
         */
        const val AGENDA_WEB_API = "/PortaleStudentiUnimib/index.php"

        /**
         * Parses a date string in Agenda Web format (DD-MM-YYYY).
         *
         * @param value The date string to parse
         * @return The parsed [LocalDate], or null if parsing fails
         */
        fun parseDate(value: String): LocalDate? = runCatching {
            LocalDate.parse(value.trim(), DATE_FORMAT)
        }.getOrElse {
            runCatching {
                LocalDate.parse(value.trim(), DATE_FORMAT_SLASH)
            }.getOrNull()
        }

        /**
         * Parses a time string in Agenda Web format (HH:mm).
         *
         * @param value The time string to parse
         * @return The parsed [LocalTime], or null if parsing fails
         */
        fun parseTime(value: String): LocalTime? = runCatching {
            LocalTime.parse(value.trim(), TIME_FORMAT)
        }.getOrNull()

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
    protected suspend fun executeGet(
        path: String,
        queryParams: Map<String, String> = emptyMap()
    ): Document {
        val url = buildUrl(path, queryParams)
        val response = client.get(url)
        return Jsoup.parse(response.bodyAsChannel().toInputStream(), "UTF-8", BASE_URL)
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
        queryParams: Map<String, String> = emptyMap()
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
    protected suspend fun executePostRaw(
        path: String,
        body: Any,
        queryParams: Map<String, String> = emptyMap()
    ): HttpResponse {
        val url = buildUrl(path, queryParams)
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    /**
     * Builds a full URL from a path and query parameters.
     *
     * @param path The request path (can be relative or absolute)
     * @param queryParams Query parameters to append
     * @return The full URL string
     */
    protected fun buildUrl(path: String, queryParams: Map<String, String> = emptyMap()): String {
        val basePath = when {
            path.startsWith("http") -> path
            path.startsWith("/") -> "$BASE_URL$path"
            else -> "$BASE_URL/$path"
        }
        return if (queryParams.isEmpty()) {
            basePath
        } else {
            val query = queryParams.entries.joinToString("&") { (key, value) ->
                "$key=${encodeQueryValue(value)}"
            }
            if (basePath.contains("?")) "$basePath&$query" else "$basePath?$query"
        }
    }

    /**
     * Parses a time range from text.
     */
    protected fun parseTimeRange(text: String?): Pair<LocalTime?, LocalTime?> {
        if (text.isNullOrBlank()) return Pair(null, null)

        val rangeMatch = TIME_RANGE_REGEX.find(text)
        if (rangeMatch != null) {
            return Pair(
                parseTime(rangeMatch.groupValues[1]),
                parseTime(rangeMatch.groupValues[2])
            )
        }

        val singleMatch = TIME_SINGLE_REGEX.find(text)
        if (singleMatch != null) {
            return Pair(parseTime(singleMatch.groupValues[1]), null)
        }

        return Pair(null, null)
    }

    /**
     * Parses room and building from a combined string.
     */
    protected fun parseRoomAndBuilding(text: String?): Pair<String?, String?> {
        if (text.isNullOrBlank()) return Pair(null, null)

        // Try [Building] format
        val bracketMatch = """(.+?)\s*\[(.+?)]""".toRegex().find(text)
        if (bracketMatch != null) {
            return Pair(bracketMatch.groupValues[1].trim(), bracketMatch.groupValues[2].trim())
        }

        // Try (Building) format
        val parenMatch = """(.+?)\s*\((.+?)\)""".toRegex().find(text)
        if (parenMatch != null) {
            return Pair(parenMatch.groupValues[1].trim(), parenMatch.groupValues[2].trim())
        }

        return Pair(text.trim(), null)
    }
}