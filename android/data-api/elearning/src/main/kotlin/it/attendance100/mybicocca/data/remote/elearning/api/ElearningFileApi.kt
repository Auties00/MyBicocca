package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUpload
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUploadedFile
import it.attendance100.mybicocca.data.remote.elearning.exception.ElearningException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
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
     * URLs that already carry a token (e.g. submission file URLs persisted with it) are
     * returned unchanged, so a second token is never appended.
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
        if (fileUrl.contains("token=")) return fileUrl
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

    /**
     * Downloads a web service file fully into memory.
     *
     * Same endpoint and error handling as [downloadFile], but returns the bytes eagerly — used
     * when the payload must be re-uploaded (e.g. keeping an existing submission file while editing).
     * Intended for the small files typical of assignment submissions.
     *
     * @param wsToken The web service token (32 characters).
     * @param fileUrl A `webservice/pluginfile.php` URL (the token may already be appended).
     * @return The file content.
     * @throws ElearningException If the request fails or Moodle returns an error envelope.
     */
    suspend fun downloadFileBytes(wsToken: String, fileUrl: String): ByteArray {
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
        return response.readRawBytes()
    }

    /**
     * Uploads one or more files to the current user's private draft file area.
     *
     * Wraps Moodle's `webservice/upload.php` endpoint (the token is passed as the `token` query
     * parameter). The returned [ElearningUploadedFile.itemId] identifies the draft area; the same
     * id is shared by every file in a single call and is what the submission save call expects
     * (e.g. [it.attendance100.mybicocca.data.remote.elearning.dto.ElearningSaveSubmissionRequest.filesDraftItemId]).
     * Pass [itemId] to append to an existing draft area instead of creating a new one.
     *
     * Like the download endpoint, `upload.php` reports failures as an HTTP 200 JSON object with an
     * `error` field rather than a non-2xx status, so the response shape is inspected.
     *
     * @param wsToken The web service token (32 characters).
     * @param files The files to upload (held in memory).
     * @param itemId An existing draft-area id to append to, or `0` to create a new one.
     * @return The stored files, each carrying the draft-area [ElearningUploadedFile.itemId].
     * @throws IllegalArgumentException If the token is invalid or [files] is empty.
     * @throws ElearningException If the upload fails or Moodle returns an error envelope.
     */
    suspend fun uploadToDraftArea(
        wsToken: String,
        files: List<ElearningUpload>,
        itemId: Int = 0
    ): List<ElearningUploadedFile> {
        require(wsToken.length == WS_TOKEN_LENGTH) {
            "Invalid wsToken: expected a $WS_TOKEN_LENGTH-character string"
        }
        require(files.isNotEmpty()) { "No files to upload" }

        val parts = formData {
            append("itemid", itemId.toString())
            append("filepath", "/")
            files.forEachIndexed { index, file ->
                append(
                    "file_$index",
                    file.bytes,
                    Headers.build {
                        file.mimeType?.let { append(HttpHeaders.ContentType, it) }
                        append(
                            HttpHeaders.ContentDisposition,
                            ContentDisposition.File
                                .withParameter(ContentDisposition.Parameters.Name, "file_$index")
                                .withParameter(ContentDisposition.Parameters.FileName, file.fileName)
                                .toString()
                        )
                    }
                )
            }
        }

        val response = client.post("${BASE_URL}webservice/upload.php?token=$wsToken") {
            setBody(MultiPartFormDataContent(parts))
        }
        if (response.status != HttpStatusCode.OK) {
            throw ElearningException(null, "Invalid response status: ${response.status}")
        }
        return when (val element = response.body<JsonElement>()) {
            is JsonArray -> json.decodeFromJsonElement(ListSerializer(ElearningUploadedFile.serializer()), element)
            is JsonObject -> {
                val errorCode = element["errorcode"]?.jsonPrimitive?.contentOrNull
                val message = element["error"]?.jsonPrimitive?.contentOrNull
                    ?: element["message"]?.jsonPrimitive?.contentOrNull
                    ?: "Upload returned an error envelope"
                throw ElearningException(errorCode, message)
            }
            else -> throw ElearningException(null, "Invalid upload response: $element")
        }
    }
}
