package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cache row for a forum discussion, keyed by (account, discussion). Fed by the paged
 * discussions sync from mod_forum_get_forum_discussions; the
 * (account, forum, pinned, modified) index serves the pinned-first, latest-activity list
 * ordering.
 *
 * @property firstPostId Post id of the opening post, the reply target for top-level replies.
 * @property createdAtMs Epoch milliseconds, null when unknown.
 * @property timeModifiedMs Epoch milliseconds of the latest change, null when unknown; the
 * recency half of the list ordering.
 * @property unreadCount Unread posts as last reported by the server; zeroed locally when the
 * thread is opened.
 * @property lastPostAuthorName Who wrote the most recent post; equals the discussion author
 * when the thread has no replies.
 * @property messagePreview Plain-text excerpt of the opening post, pre-stripped at mapping time
 * so list rows render without an HTML pass.
 * @property isFavourite Star flag; flipped optimistically by the favourite toggle and reverted
 * when the server call fails.
 * @property canFavourite Whether the user may star/unstar this discussion.
 */
@Entity(
    tableName = "elearning_forum_discussions",
    primaryKeys = ["account_id", "discussion_id"],
    indices = [Index("account_id", "forum_id", "is_pinned", "time_modified_ms")],
)
data class DiscussionEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "discussion_id") val discussionId: Int,
    @ColumnInfo(name = "forum_id") val forumId: Int,
    @ColumnInfo(name = "first_post_id") val firstPostId: Int,
    val subject: String,
    @ColumnInfo(name = "author_user_id") val authorUserId: Int?,
    @ColumnInfo(name = "author_name") val authorName: String,
    @ColumnInfo(name = "author_avatar_url") val authorAvatarUrl: String?,
    @ColumnInfo(name = "created_at_ms") val createdAtMs: Long?,
    @ColumnInfo(name = "time_modified_ms") val timeModifiedMs: Long?,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean,
    @ColumnInfo(name = "is_locked") val isLocked: Boolean,
    @ColumnInfo(name = "unread_count") val unreadCount: Int,
    @ColumnInfo(name = "reply_count") val replyCount: Int,
    @ColumnInfo(name = "last_post_author_name") val lastPostAuthorName: String?,
    @ColumnInfo(name = "message_preview") val messagePreview: String?,
    @ColumnInfo(name = "has_attachments") val hasAttachments: Boolean,
    @ColumnInfo(name = "can_reply") val canReply: Boolean,
    @ColumnInfo(name = "is_favourite", defaultValue = "0") val isFavourite: Boolean,
    @ColumnInfo(name = "can_favourite", defaultValue = "0") val canFavourite: Boolean,
)
