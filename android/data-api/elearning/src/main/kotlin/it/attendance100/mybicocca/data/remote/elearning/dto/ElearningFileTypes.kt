package it.attendance100.mybicocca.data.remote.elearning.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A file to upload to a Moodle private draft file area via
 * [it.attendance100.mybicocca.data.remote.elearning.api.ElearningFileApi.uploadToDraftArea].
 *
 * The bytes are held in memory; this endpoint is intended for the small payloads typical of
 * assignment submissions (documents, archives, images), not for arbitrarily large uploads.
 *
 * @property fileName The name the file should have in the draft area (and, later, in the
 *   submission).
 * @property mimeType The MIME type sent with the multipart part, or `null` to let the server
 *   infer it.
 * @property bytes The raw file content.
 */
class ElearningUpload(
    val fileName: String,
    val mimeType: String?,
    val bytes: ByteArray
)

/**
 * A file that was stored in a draft file area by `webservice/upload.php`.
 *
 * The [itemId] identifies the draft area; pass it to subsequent uploads (to add more files to the
 * same area) and to the submission save call (e.g.
 * [ElearningSaveSubmissionRequest.filesDraftItemId]).
 *
 * @property itemId The draft-area item id.
 * @property fileName The stored file name.
 * @property filePath The draft-area path (usually `/`).
 * @property fileSize The stored file size in bytes.
 * @property fileArea The file area (typically `draft`).
 */
@Serializable
data class ElearningUploadedFile(
    @SerialName("itemid")
    val itemId: Int,
    @SerialName("filename")
    val fileName: String? = null,
    @SerialName("filepath")
    val filePath: String? = null,
    @SerialName("filesize")
    val fileSize: Long? = null,
    @SerialName("filearea")
    val fileArea: String? = null
)
