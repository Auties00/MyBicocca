package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * Identifier of a single forum post, as used by the mod_forum web services. A discussion's
 * opening post has its own post id, which is the reply target for top-level replies.
 *
 * @property value Raw Moodle post id.
 */
@JvmInline
value class PostId(val value: Int)
