package it.attendance100.mybicocca.domain.model.elearning.forum

/**
 * Identifier of a Moodle forum, as used by the mod_forum web services. This is the forum
 * instance id, distinct from the activity's course-module id.
 *
 * @property value Raw Moodle forum id.
 */
@JvmInline
value class ForumId(val value: Int)
