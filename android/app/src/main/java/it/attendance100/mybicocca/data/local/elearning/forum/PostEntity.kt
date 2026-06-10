package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cache row for a forum post, keyed by (account, post). Fed by the thread sync from
 * mod_forum_get_discussion_posts; the two (account, discussion, …) indices serve chronological
 * thread streaming and parent lookups for reply-tree building. The capability columns
 * (can_reply, can_edit, can_delete) flatten the payload's per-post capabilities block.
 *
 * @property parentId Post this one replies to; null for the discussion's opening post.
 * @property createdAtMs Epoch milliseconds, null when unknown.
 * @property modifiedAtMs Epoch milliseconds of the last edit, null when unknown.
 * @property attachmentsJson JSON-encoded list of the post's attachments; null when there are
 * none.
 * @property isDeleted Deleted post kept as a tombstone because it still has visible replies.
 */
@Entity(
    tableName = "elearning_forum_posts",
    primaryKeys = ["account_id", "post_id"],
    indices = [
        Index("account_id", "discussion_id", "created_at_ms"),
        Index("account_id", "discussion_id", "parent_id"),
    ],
)
data class PostEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "post_id") val postId: Int,
    @ColumnInfo(name = "discussion_id") val discussionId: Int,
    @ColumnInfo(name = "parent_id") val parentId: Int?,
    @ColumnInfo(name = "author_user_id") val authorUserId: Int?,
    @ColumnInfo(name = "author_name") val authorName: String,
    @ColumnInfo(name = "author_avatar_url") val authorAvatarUrl: String?,
    val subject: String,
    val message: String,
    @ColumnInfo(name = "created_at_ms") val createdAtMs: Long?,
    @ColumnInfo(name = "modified_at_ms") val modifiedAtMs: Long?,
    @ColumnInfo(name = "attachments_json") val attachmentsJson: String?,
    @ColumnInfo(name = "can_reply") val canReply: Boolean,
    @ColumnInfo(name = "can_edit") val canEdit: Boolean,
    @ColumnInfo(name = "can_delete") val canDelete: Boolean,
    @ColumnInfo(name = "is_deleted", defaultValue = "0") val isDeleted: Boolean,
)
