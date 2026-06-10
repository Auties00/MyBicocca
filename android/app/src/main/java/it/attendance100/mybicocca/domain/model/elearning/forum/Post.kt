package it.attendance100.mybicocca.domain.model.elearning.forum

import java.time.Instant

/**
 * A single post of a forum discussion, sourced from the mod_forum_get_discussion_posts web
 * service and cached in Room. Rendered in the forum sheet's thread page, nested under its parent.
 *
 * @property id Moodle post id.
 * @property discussionId Discussion the post belongs to.
 * @property parentId Post this one replies to; null for the discussion's opening post.
 * @property authorUserId Moodle user id of the author, null when the payload omits it.
 * @property authorName Author display name.
 * @property authorAvatarUrl Author avatar URL, null when absent.
 * @property subject Post subject line.
 * @property message Post body as Moodle HTML.
 * @property createdAt When the post was written, null when unknown.
 * @property modifiedAt When the post was last edited, null when unknown.
 * @property attachments Files attached to the post.
 * @property canReply Whether the user may reply to this post.
 * @property canEdit Whether the user may edit this post — own posts within Moodle's edit window.
 * @property canDelete Whether the user may delete this post.
 * @property isDeleted Whether this is a deleted post kept in the tree as a tombstone because it
 * still has visible replies.
 */
data class Post(
    val id: PostId,
    val discussionId: DiscussionId,
    val parentId: PostId?,
    val authorUserId: Int?,
    val authorName: String,
    val authorAvatarUrl: String?,
    val subject: String,
    val message: String,
    val createdAt: Instant?,
    val modifiedAt: Instant?,
    val attachments: List<PostAttachment>,
    val canReply: Boolean,
    val canEdit: Boolean,
    val canDelete: Boolean,
    val isDeleted: Boolean,
)
