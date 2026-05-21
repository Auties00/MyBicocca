package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

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
)
