package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * Identifier of a forum discussion (thread), as used by the mod_forum web services.
 *
 * @property value Raw Moodle discussion id.
 */
@JvmInline
value class DiscussionId(val value: Int)
