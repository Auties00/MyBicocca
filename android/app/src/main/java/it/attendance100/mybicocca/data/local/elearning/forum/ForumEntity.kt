package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "elearning_forums",
    primaryKeys = ["account_id", "forum_id"],
    indices = [Index("account_id", "course_id")],
)
data class ForumEntity(
    @ColumnInfo(name = "account_id") val accountId: String,
    @ColumnInfo(name = "forum_id") val forumId: Int,
    @ColumnInfo(name = "course_id") val courseId: Int,
    @ColumnInfo(name = "cm_id") val cmId: Int?,
    val name: String,
    val intro: String?,
    @ColumnInfo(name = "type_raw") val typeRaw: String,
    @ColumnInfo(name = "discussion_count") val discussionCount: Int,
    @ColumnInfo(name = "post_count") val postCount: Int,
    @ColumnInfo(name = "can_create_discussions") val canCreateDiscussions: Boolean,
    @ColumnInfo(name = "can_subscribe") val canSubscribe: Boolean,
    @ColumnInfo(name = "can_attach_files") val canAttachFiles: Boolean,
)
