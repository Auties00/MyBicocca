package it.attendance100.mybicocca.domain.model.elearning.forum

import java.time.Instant

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
)
