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
    val replyCount: Int,
    // Who wrote the most recent post; equals authorName when the thread has no replies.
    val lastPostAuthorName: String?,
    // Plain-text excerpt of the opening post, pre-stripped at mapping time for list previews.
    val messagePreview: String?,
    val hasAttachments: Boolean,
    val canReply: Boolean,
)
