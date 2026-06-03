package it.attendance100.mybicocca.data.mapper.elearning

import it.attendance100.mybicocca.data.local.elearning.forum.DiscussionEntity
import it.attendance100.mybicocca.data.local.elearning.forum.ForumEntity
import it.attendance100.mybicocca.data.local.elearning.forum.PostEntity
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningJson
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningForum
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningForumDiscussion
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningForumPost
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostAttachment
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import java.time.Instant

internal fun ElearningForum.toEntity(accountId: AccountId): ForumEntity =
    ForumEntity(
        accountId = accountId.value,
        forumId = id,
        courseId = courseId,
        cmId = courseModuleId,
        name = name,
        intro = introduction,
        typeRaw = type ?: ForumType.Other.raw,
        discussionCount = numberOfDiscussions ?: 0,
        postCount = 0,
        canCreateDiscussions = canCreateDiscussions == true,
        canSubscribe = forceSubscribe != 3,
        canAttachFiles = (maxAttachments ?: 0) > 0,
    )

internal fun ForumEntity.toDomain(): Forum =
    Forum(
        id = ForumId(forumId),
        courseId = CourseId(courseId),
        cmId = cmId,
        name = name,
        intro = intro,
        type = ForumType.fromRaw(typeRaw),
        discussionCount = discussionCount,
        postCount = postCount,
        canCreateDiscussions = canCreateDiscussions,
        canSubscribe = canSubscribe,
        canAttachFiles = canAttachFiles,
    )

internal fun ElearningForumDiscussion.toEntity(accountId: AccountId, forumId: Int): DiscussionEntity =
    DiscussionEntity(
        accountId = accountId.value,
        discussionId = discussionId ?: id,
        forumId = forumId,
        firstPostId = parentPostId ?: id,
        subject = subject ?: name,
        authorUserId = authorUserId,
        authorName = authorFullName ?: "—",
        authorAvatarUrl = authorPictureUrl,
        createdAtMs = createdTimestamp.toMillisOrNullSec(),
        timeModifiedMs = (modifiedAtTimestamp ?: modifiedTimestamp).toMillisOrNullSec(),
        isPinned = pinned == true,
        isLocked = locked == true,
        unreadCount = numberOfUnread ?: 0,
        replyCount = numberOfReplies ?: 0,
        lastPostAuthorName = modifierFullName ?: authorFullName,
        messagePreview = message?.toPlainPreview(),
        hasAttachments = hasAttachment == true,
        canReply = canReply == true,
    )

internal fun DiscussionEntity.toDomain(): Discussion =
    Discussion(
        id = DiscussionId(discussionId),
        forumId = ForumId(forumId),
        firstPostId = PostId(firstPostId),
        subject = subject,
        authorUserId = authorUserId,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        createdAt = createdAtMs?.let(Instant::ofEpochMilli),
        timeModified = timeModifiedMs?.let(Instant::ofEpochMilli),
        isPinned = isPinned,
        isLocked = isLocked,
        unreadCount = unreadCount,
        replyCount = replyCount,
        lastPostAuthorName = lastPostAuthorName,
        messagePreview = messagePreview,
        hasAttachments = hasAttachments,
        canReply = canReply,
    )

internal fun ElearningForumPost.toEntity(accountId: AccountId, discussionIdFallback: Int): PostEntity {
    val attachmentRefs = attachments.orEmpty().map { file ->
        AttachmentRefJson(
            fileName = file.fileName ?: "file",
            fileUrl = file.fileUrl,
            mimeType = file.mimeType,
            sizeBytes = file.fileSize,
        )
    }
    return PostEntity(
        accountId = accountId.value,
        postId = id,
        discussionId = discussionId ?: discussionIdFallback,
        parentId = parentId,
        authorUserId = author?.id,
        authorName = author?.fullName ?: "—",
        authorAvatarUrl = author?.urls?.profileImage,
        subject = subject ?: "",
        message = message ?: "",
        createdAtMs = createdTimestamp.toMillisOrNullSec(),
        modifiedAtMs = modifiedTimestamp.toMillisOrNullSec(),
        attachmentsJson = if (attachmentRefs.isEmpty()) null
        else ElearningJson.encodeToString(attachmentRefs),
        canReply = capabilities?.reply == true,
        canEdit = capabilities?.edit == true,
        canDelete = capabilities?.delete == true,
    )
}

internal fun PostEntity.toDomain(): Post {
    val attachments = attachmentsJson?.let { json ->
        runCatching {
            ElearningJson.decodeFromString(ListSerializer(AttachmentRefJson.serializer()), json)
        }.getOrNull()
    }.orEmpty().map {
        PostAttachment(
            fileName = it.fileName,
            fileUrl = it.fileUrl,
            mimeType = it.mimeType,
            sizeBytes = it.sizeBytes,
        )
    }
    return Post(
        id = PostId(postId),
        discussionId = DiscussionId(discussionId),
        parentId = parentId?.let(::PostId),
        authorUserId = authorUserId,
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        subject = subject,
        message = message,
        createdAt = createdAtMs?.let(Instant::ofEpochMilli),
        modifiedAt = modifiedAtMs?.let(Instant::ofEpochMilli),
        attachments = attachments,
        canReply = canReply,
        canEdit = canEdit,
        canDelete = canDelete,
    )
}

private fun Long?.toMillisOrNullSec(): Long? = this?.takeIf { it > 0 }?.let { it * 1000L }

// Strips the opening post's HTML down to a single-line excerpt at write time, so list rows
// never pay an Html.fromHtml pass per frame.
private val HtmlTagRegex = Regex("<[^>]*>")
private val HtmlEntities = mapOf(
    "&nbsp;" to " ", "&amp;" to "&", "&lt;" to "<", "&gt;" to ">",
    "&quot;" to "\"", "&#39;" to "'", "&apos;" to "'", "&egrave;" to "è",
    "&eacute;" to "é", "&agrave;" to "à", "&ograve;" to "ò", "&ugrave;" to "ù", "&igrave;" to "ì",
)

private fun String.toPlainPreview(maxLength: Int = 220): String? {
    var text = replace(HtmlTagRegex, " ")
    for ((entity, char) in HtmlEntities) text = text.replace(entity, char)
    val collapsed = text.split(Regex("[\\s\u00A0]+"))
        .filter { it.isNotEmpty() }
        .joinToString(" ")
    if (collapsed.isEmpty()) return null
    return if (collapsed.length <= maxLength) collapsed else collapsed.take(maxLength - 1) + "…"
}
