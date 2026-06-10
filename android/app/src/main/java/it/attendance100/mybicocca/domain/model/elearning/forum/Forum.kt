package it.attendance100.mybicocca.domain.model.elearning.forum

import it.attendance100.mybicocca.domain.model.elearning.course.CourseId

/**
 * A forum activity of an e-learning course, sourced from the mod_forum_get_forums_by_courses web
 * service and cached in Room. Listed in the course detail's forum section; opening one shows its
 * discussions in the forum modal sheet.
 *
 * @property id Moodle forum instance id.
 * @property courseId Course the forum belongs to.
 * @property cmId Course-module id of the forum activity, null when the payload omits it.
 * @property name Forum title as shown in the course.
 * @property intro Forum description as Moodle HTML, null when absent.
 * @property type Behavioural forum type, [ForumType.Other] when unrecognized.
 * @property discussionCount Number of discussions reported by the forums web service.
 * @property postCount Total posts in the forum; zero when the source payload does not report it.
 * @property canCreateDiscussions Whether the user may start a new discussion here.
 * @property canSubscribe Whether subscribing is possible; false when the forum force-disables
 * subscription.
 * @property canAttachFiles Whether posts may carry attachments, i.e. the forum allows at least
 * one attachment per post.
 */
data class Forum(
    val id: ForumId,
    val courseId: CourseId,
    val cmId: Int?,
    val name: String,
    val intro: String?,
    val type: ForumType,
    val discussionCount: Int,
    val postCount: Int,
    val canCreateDiscussions: Boolean,
    val canSubscribe: Boolean,
    val canAttachFiles: Boolean,
)
