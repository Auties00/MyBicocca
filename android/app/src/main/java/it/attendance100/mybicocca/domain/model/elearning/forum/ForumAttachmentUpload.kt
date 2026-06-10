package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * A file the user picked to attach to a new discussion, reply, or edit, held in memory until it
 * is uploaded to a Moodle draft area whose id is then passed as the post's `attachmentsid`.
 *
 * @property fileName File name the upload is stored under.
 * @property mimeType MIME type reported by the picker, null when unknown.
 * @property bytes Raw file content; equality and hashing are content-based.
 */
data class ForumAttachmentUpload(
    val fileName: String,
    val mimeType: String?,
    val bytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ForumAttachmentUpload) return false
        return fileName == other.fileName && mimeType == other.mimeType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}
