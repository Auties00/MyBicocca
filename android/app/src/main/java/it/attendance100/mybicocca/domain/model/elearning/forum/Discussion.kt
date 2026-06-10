package it.attendance100.mybicocca.domain.model.elearning.forum

import java.time.Instant

/**
 * A discussion (thread) of a forum, sourced from the mod_forum_get_forum_discussions web service
 * and cached in Room. Rendered as rows of the forum sheet's discussion list, pinned threads
 * first, then by most recent activity.
 *
 * @property id Moodle discussion id.
 * @property forumId Forum the discussion belongs to.
 * @property firstPostId Post id of the opening post, the reply target for top-level replies.
 * @property subject Discussion title.
 * @property authorUserId Moodle user id of the discussion starter, null when the payload omits it.
 * @property authorName Display name of the discussion starter.
 * @property authorAvatarUrl Avatar URL of the discussion starter, null when absent.
 * @property createdAt When the discussion was started, null when unknown.
 * @property timeModified When the discussion last changed, typically the latest post; null when
 * unknown.
 * @property isPinned Whether the discussion is pinned to the top of the forum.
 * @property isLocked Whether the discussion is locked against new replies.
 * @property unreadCount Posts the user has not read yet; zeroed locally when the thread is opened.
 * @property replyCount Number of replies, excluding the opening post.
 * @property lastPostAuthorName Who wrote the most recent post; equals [authorName] when the
 * thread has no replies.
 * @property messagePreview Plain-text excerpt of the opening post, pre-stripped at mapping time
 * for list previews; null when the opening post has no renderable text.
 * @property hasAttachments Whether the opening post carries attachments.
 * @property canReply Whether the user may reply in this discussion.
 * @property isFavourite Whether the user has starred (favourited) this discussion.
 * @property canFavourite Whether the user is allowed to star/unstar this discussion.
 */
data class Discussion(
    val id: DiscussionId,
    val forumId: ForumId,
    val firstPostId: PostId,
    val subject: String,
    val authorUserId: Int?,
    val authorName: String,
    val authorAvatarUrl: String?,
    val createdAt: Instant?,
    val timeModified: Instant?,
    val isPinned: Boolean,
    val isLocked: Boolean,
    val unreadCount: Int,
    val replyCount: Int,
    val lastPostAuthorName: String?,
    val messagePreview: String?,
    val hasAttachments: Boolean,
    val canReply: Boolean,
    val isFavourite: Boolean,
    val canFavourite: Boolean,
)
