package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * Cache row for a course forum, keyed by (account, forum) so each signed-in account holds its
 * own copy. Fed by the course-forums sync from mod_forum_get_forums_by_courses; the
 * (account, course) index serves the course-scoped listing. The capability columns
 * (can_create_discussions, can_subscribe, can_attach_files) are precomputed at mapping time
 * from the payload's capability and configuration fields.
 *
 * @property cmId Course-module id of the forum activity, null when the payload omits it.
 * @property typeRaw Moodle forum type value, resolved to a domain type at read time.
 * @property postCount Total posts in the forum; zero when the source payload does not report it.
 */
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
