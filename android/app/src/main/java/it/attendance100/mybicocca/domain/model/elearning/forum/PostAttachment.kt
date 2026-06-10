package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * A file attached to a forum post, carried by the mod_forum_get_discussion_posts web service and
 * rendered as an attachment chip in the forum sheet's thread view.
 *
 * @property fileName Display file name.
 * @property fileUrl Moodle pluginfile download URL, null when absent; downloading it requires
 * the web-service token.
 * @property mimeType MIME type reported by Moodle, null when unknown.
 * @property sizeBytes File size in bytes, null when unknown.
 */
data class PostAttachment(
    val fileName: String,
    val fileUrl: String?,
    val mimeType: String?,
    val sizeBytes: Long?,
)
