package it.attendance100.mybicocca.domain.model.elearning.assignment

/**
 * A local file the student picked to hand in, already read into memory. The UI resolves a
 * picked content uri to its bytes before handing it to the submission use case, keeping the
 * domain layer free of Android types. Held only for the duration of a save; never persisted.
 *
 * @property fileName Display name, used as the file name on Moodle.
 * @property mimeType MIME type reported by the document provider, when known.
 * @property sizeBytes Size in bytes, when known.
 * @property bytes Full file content to upload.
 */
class SubmissionFile(
    val fileName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
    val bytes: ByteArray,
)
