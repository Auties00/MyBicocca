package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

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
    @ColumnInfo(name = "can_reply") val canReply: Boolean,
)
