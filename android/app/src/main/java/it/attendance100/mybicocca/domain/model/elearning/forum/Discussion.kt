package it.attendance100.mybicocca.domain.model.elearning.forum

import java.time.Instant

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
    val canReply: Boolean,
)
