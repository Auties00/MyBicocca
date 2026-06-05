package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * API for downloading files served by the Moodle web service file endpoint.
 *
 * Course contents (resources, folder entries, page/book attachments) expose their payloads
 * as `webservice/pluginfile.php` URLs in
 * [it.attendance100.mybicocca.data.remote.elearning.dto.ElearningModuleContent.fileUrl].
 * Those URLs are downloadable with a plain GET once the web service token is appended as
 * the `token` query parameter — no cookies or headers are required, and the endpoint
 * honors HTTP `Range` requests (`Accept-Ranges: bytes`), so the same authenticated URL can
 * also be handed to streaming media players.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningFileApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Builds the authenticated variant of a `webservice/pluginfile.php` URL.
     *
     * The returned URL embeds the web service token as the `token` query parameter and can
     * be fetched with a plain unauthenticated GET (e.g. by a media player or an external
     * application). Treat it as a credential: the token grants access to the whole web
     * service surface, not just this file.
     *
     * @param wsToken The web service token (32 characters)
     * @param fileUrl The `fileUrl` of a module content entry, as returned by
     *   [ElearningCourseApi.getCourseContents]
     * @return The same URL with the `token` query parameter appended
     * @throws IllegalArgumentException If the token is invalid or the URL is not a
     *   web service file URL
     */
    fun authenticatedFileUrl(wsToken: String, fileUrl: String): String {
        require(wsToken.length == WS_TOKEN_LENGTH) {
            "Invalid wsToken: expected a $WS_TOKEN_LENGTH-character string"
        }
        require(fileUrl.contains("/webservice/pluginfile.php")) {
            "Not a web service file URL: $fileUrl"
        }
        val separator = if ('?' in fileUrl) '&' else '?'
        return "$fileUrl${separator}token=$wsToken"
    }

    /**
     * Downloads a file from the Moodle web service file endpoint.
     *
     * Moodle reports authentication failures on this endpoint as an HTTP 200 response
     * with a JSON error envelope instead of a non-2xx status, so the response content
     * type is inspected to distinguish file payloads from errors.
     *
     * @param wsToken The web service token (32 characters)
     * @param fileUrl The `fileUrl` of a module content entry, as returned by
     *   [ElearningCourseApi.getCourseContents]
     * @return The file content as a byte channel
     * @throws IllegalArgumentException If the token is invalid or the URL is not a
     *   web service file URL
     * @throws ElearningException If the request fails or Moodle returns an error envelope
     */
    suspend fun downloadFile(wsToken: String, fileUrl: String): ByteReadChannel {
        val response = client.get(authenticatedFileUrl(wsToken, fileUrl))
        if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.PartialContent) {
            throw ElearningException(null, "Invalid response status: ${response.status}")
        }
        val contentType = response.contentType()
        if (contentType != null && contentType.match(ContentType.Application.Json)) {
            val envelope = runCatching { json.decodeFromString<JsonObject>(response.bodyAsText()) }.getOrNull()
            val errorCode = envelope?.get("errorcode")?.jsonPrimitive?.contentOrNull
            val message = envelope?.get("message")?.jsonPrimitive?.contentOrNull
                ?: envelope?.get("error")?.jsonPrimitive?.contentOrNull
                ?: "File download returned an error envelope"
            throw ElearningException(errorCode, message)
        }
        return response.bodyAsChannel()
    }
}
