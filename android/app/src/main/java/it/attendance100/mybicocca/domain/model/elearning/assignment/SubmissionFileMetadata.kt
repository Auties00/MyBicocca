package it.attendance100.mybicocca.domain.model.elearning.assignment

/**
 * Display metadata of a file the student picked for an assignment submission, resolved from the
 * picker's opaque uri without reading the content. Rendered as attachment chips in the consegna
 * editor; the bytes are read separately as a [SubmissionFile] only when the submission is saved.
 *
 * @property fileName Display name reported by the document provider, falling back to the uri's
 * last path segment.
 * @property mimeType Provider-reported MIME type, when known.
 * @property sizeBytes Provider-reported size, when known.
 */
data class SubmissionFileMetadata(
    val fileName: String,
    val mimeType: String?,
    val sizeBytes: Long?,
)
