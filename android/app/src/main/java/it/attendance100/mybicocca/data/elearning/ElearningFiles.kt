package it.attendance100.mybicocca.data.elearning

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.toByteArray
import it.attendance100.mybicocca.data.remote.elearning.api.MoodleFilesApi
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleUploadedFile
import it.attendance100.mybicocca.data.remote.runtime.FileUpload

/**
 * The files course contents link to (`webservice/pluginfile.php` URLs, such as a content's
 * `fileUrl`), and uploads to the user's draft area, which a submission saves.
 *
 * @param files The web service file endpoints.
 */
class ElearningFiles(private val files: MoodleFilesApi) {
    /**
     * The URL of a file with the web service token, which a plain GET downloads, as a media
     * player does. Treat it as a credential: the token grants the whole web service, not just
     * the file. A URL already carrying a token is returned as it is.
     *
     * @throws IllegalArgumentException If the token is invalid or the URL isn't a web service file's.
     */
    fun authenticatedFileUrl(wsToken: String, fileUrl: String): String {
        requireToken(wsToken)
        require(PLUGINFILE in fileUrl) { "Not a web service file URL: $fileUrl" }
        if ("token=" in fileUrl) return fileUrl
        val separator = if ('?' in fileUrl) '&' else '?'
        return "$fileUrl${separator}token=$wsToken"
    }

    /** Downloads a file a course content links to. */
    suspend fun downloadFile(wsToken: String, fileUrl: String): ByteReadChannel {
        requireToken(wsToken)
        val path = fileUrl.substringAfter(PLUGINFILE, missingDelimiterValue = "").substringBefore('?')
        require(path.isNotEmpty()) { "Not a web service file URL: $fileUrl" }
        return files.downloadFile(path, wsToken)
    }

    /** Downloads a file fully into memory, as re-uploading it needs; meant for small files. */
    suspend fun downloadFileBytes(wsToken: String, fileUrl: String): ByteArray = downloadFile(wsToken, fileUrl).toByteArray()

    /**
     * Uploads files to one draft area: a new one, or [itemId] to add files to it.
     *
     * @return The stored files, all naming the draft area by their item id.
     */
    suspend fun uploadToDraftArea(wsToken: String, uploads: List<FileUpload>, itemId: Int = 0): List<MoodleUploadedFile> {
        requireToken(wsToken)
        require(uploads.isNotEmpty()) { "No files to upload" }
        var area = itemId
        return uploads.flatMap { upload ->
            files.uploadFile(wsToken, itemId = area, file = upload).also { stored -> area = stored.first().itemId }
        }
    }

    private fun requireToken(wsToken: String) {
        require(wsToken.length == WS_TOKEN_LENGTH) { "Invalid wsToken: expected a $WS_TOKEN_LENGTH-character string" }
    }

    private companion object {
        const val PLUGINFILE = "/webservice/pluginfile.php/"

        /** The length of a valid web service token. */
        const val WS_TOKEN_LENGTH = 32
    }
}
