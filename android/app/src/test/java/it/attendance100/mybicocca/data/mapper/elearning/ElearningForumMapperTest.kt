package it.attendance100.mybicocca.data.mapper.elearning

import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.elearning.forum.DiscussionEntity
import it.attendance100.mybicocca.data.local.elearning.forum.ForumEntity
import it.attendance100.mybicocca.data.local.elearning.forum.PostEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleForum
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleForumDiscussion
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodleForumPost
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodlePostAttachment
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodlePostAttachmentHtml
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodlePostAttachmentUrls
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodlePostAuthor
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodlePostAuthorUrls
import it.attendance100.mybicocca.data.remote.elearning.dto.MoodlePostCapabilities
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import org.junit.Test
import java.time.Instant

/**
 * Covers the mod_forum mappers: forum capability derivation (forced-disabled subscription,
 * attachment allowance, type-raw fallback), discussion id/field fallbacks and the
 * write-time plain-text preview, and the post mapper's capability flattening, author/subject
 * defaults, and attachment JSON round-trip.
 */
class ElearningForumMapperTest {

    private val account = AccountId("acc-1")

    private fun forum(
        id: Int = 30,
        courseId: Int = 5,
        type: String = "news",
        forceSubscribe: Int = 1,
        maxAttachments: Int = 3,
        numberOfDiscussions: Int? = 4,
        canCreateDiscussions: Boolean? = true,
    ) = MoodleForum(
        id = id,
        course = courseId,
        type = type,
        name = "Annunci",
        intro = "intro",
        forceSubscribe = forceSubscribe,
        maxAttachments = maxAttachments,
        numDiscussions = numberOfDiscussions,
        canCreateDiscussions = canCreateDiscussions,
        cmId = 300,
    )

    @Test
    fun `forum toEntity carries identity and counts`() {
        val entity = forum().toEntity(account)
        assertThat(entity.accountId).isEqualTo("acc-1")
        assertThat(entity.forumId).isEqualTo(30)
        assertThat(entity.courseId).isEqualTo(5)
        assertThat(entity.cmId).isEqualTo(300)
        assertThat(entity.typeRaw).isEqualTo("news")
        assertThat(entity.discussionCount).isEqualTo(4)
        assertThat(entity.postCount).isEqualTo(0)
    }

    @Test
    fun `forum toEntity falls back to the other type when none is reported`() {
        assertThat(forum(type = "").toEntity(account).typeRaw).isEqualTo(ForumType.Other.raw)
    }

    @Test
    fun `forum toEntity allows subscription unless force-disabled`() {
        assertThat(forum(forceSubscribe = 1).toEntity(account).canSubscribe).isTrue()
        assertThat(forum(forceSubscribe = 3).toEntity(account).canSubscribe).isFalse()
    }

    @Test
    fun `forum toEntity allows attaching only when an attachment is permitted`() {
        assertThat(forum(maxAttachments = 3).toEntity(account).canAttachFiles).isTrue()
        assertThat(forum(maxAttachments = 0).toEntity(account).canAttachFiles).isFalse()
    }

    @Test
    fun `forum toEntity defaults a null can-create flag to false`() {
        assertThat(forum(canCreateDiscussions = null).toEntity(account).canCreateDiscussions).isFalse()
    }

    @Test
    fun `forum entity toDomain resolves the type and wraps the ids`() {
        val entity = ForumEntity(
            accountId = "acc-1",
            forumId = 30,
            courseId = 5,
            cmId = 300,
            name = "Annunci",
            intro = "intro",
            typeRaw = "news",
            discussionCount = 4,
            postCount = 0,
            canCreateDiscussions = true,
            canSubscribe = true,
            canAttachFiles = true,
        )
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(ForumId(30))
        assertThat(domain.courseId).isEqualTo(CourseId(5))
        assertThat(domain.type).isEqualTo(ForumType.News)
    }

    private fun discussion(
        id: Int = 500,
        discussionId: Int = 501,
        parentPostId: Int = 600,
        subject: String = "Domanda",
        name: String = "Domanda thread",
        authorFullName: String = "Mario Rossi",
        modifierFullName: String = "Luca Bianchi",
        message: String = "<p>Ciao &amp; benvenuti</p>",
        createdTimestamp: Long = 1_000L,
        modifiedAtTimestamp: Long = 3_000L,
        modifiedTimestamp: Long = 2_000L,
        pinned: Boolean = true,
        starred: Boolean = true,
        canFavourite: Boolean = true,
    ) = MoodleForumDiscussion(
        id = id,
        name = name,
        discussion = discussionId,
        parent = parentPostId,
        subject = subject,
        message = message,
        userFullName = authorFullName,
        userModifiedFullName = modifierFullName,
        created = createdTimestamp,
        modified = modifiedAtTimestamp,
        timeModified = modifiedTimestamp,
        pinned = pinned,
        starred = starred,
        canFavourite = canFavourite,
    )

    @Test
    fun `discussion toEntity uses the discussion id and parent post when present`() {
        val entity = discussion(id = 500, discussionId = 501, parentPostId = 600).toEntity(account, forumId = 30)
        assertThat(entity.discussionId).isEqualTo(501)
        assertThat(entity.firstPostId).isEqualTo(600)
        assertThat(entity.forumId).isEqualTo(30)
    }

    @Test
    fun `discussion toEntity falls back to the overloaded id`() {
        val entity = discussion(id = 500, discussionId = 0, parentPostId = 0).toEntity(account, forumId = 30)
        assertThat(entity.discussionId).isEqualTo(500)
        assertThat(entity.firstPostId).isEqualTo(500)
    }

    @Test
    fun `discussion toEntity falls back from subject to thread name`() {
        val entity = discussion(subject = "", name = "Thread name").toEntity(account, forumId = 30)
        assertThat(entity.subject).isEqualTo("Thread name")
    }

    @Test
    fun `discussion toEntity uses a dash placeholder for a missing author`() {
        assertThat(discussion(authorFullName = "").toEntity(account, forumId = 30).authorName).isEqualTo("—")
    }

    @Test
    fun `discussion toEntity prefers the modified-at over the modified timestamp`() {
        val entity = discussion(modifiedAtTimestamp = 3_000L, modifiedTimestamp = 2_000L)
            .toEntity(account, forumId = 30)
        assertThat(entity.timeModifiedMs).isEqualTo(3_000_000L)
    }

    @Test
    fun `discussion toEntity falls back to the modified timestamp`() {
        val entity = discussion(modifiedAtTimestamp = 0L, modifiedTimestamp = 2_000L)
            .toEntity(account, forumId = 30)
        assertThat(entity.timeModifiedMs).isEqualTo(2_000_000L)
    }

    @Test
    fun `discussion toEntity uses the last modifier as the last post author`() {
        assertThat(discussion(modifierFullName = "Luca Bianchi").toEntity(account, forumId = 30).lastPostAuthorName)
            .isEqualTo("Luca Bianchi")
    }

    @Test
    fun `discussion toEntity falls back to the author for the last post author`() {
        val entity = discussion(modifierFullName = "", authorFullName = "Mario Rossi")
            .toEntity(account, forumId = 30)
        assertThat(entity.lastPostAuthorName).isEqualTo("Mario Rossi")
    }

    @Test
    fun `discussion toEntity strips HTML and decodes entities into the preview`() {
        val entity = discussion(message = "<p>Ciao &amp; benvenuti</p>").toEntity(account, forumId = 30)
        assertThat(entity.messagePreview).isEqualTo("Ciao & benvenuti")
    }

    @Test
    fun `discussion toEntity yields a null preview when the message is absent`() {
        assertThat(discussion(message = "").toEntity(account, forumId = 30).messagePreview).isNull()
    }

    @Test
    fun `discussion toEntity yields a null preview when the message has no renderable text`() {
        assertThat(discussion(message = "<p>   </p>").toEntity(account, forumId = 30).messagePreview).isNull()
    }

    @Test
    fun `discussion entity toDomain wraps ids and copies flags`() {
        val entity = DiscussionEntity(
            accountId = "acc-1",
            discussionId = 501,
            forumId = 30,
            firstPostId = 600,
            subject = "Domanda",
            authorUserId = 7,
            authorName = "Mario Rossi",
            authorAvatarUrl = null,
            createdAtMs = 1_000_000L,
            timeModifiedMs = 3_000_000L,
            isPinned = true,
            isLocked = false,
            unreadCount = 2,
            replyCount = 4,
            lastPostAuthorName = "Luca Bianchi",
            messagePreview = "Ciao",
            hasAttachments = true,
            canReply = true,
            isFavourite = true,
            canFavourite = true,
        )
        val domain = entity.toDomain()
        assertThat(domain.id).isEqualTo(DiscussionId(501))
        assertThat(domain.forumId).isEqualTo(ForumId(30))
        assertThat(domain.firstPostId).isEqualTo(PostId(600))
        assertThat(domain.createdAt).isEqualTo(Instant.ofEpochMilli(1_000_000L))
        assertThat(domain.isPinned).isTrue()
        assertThat(domain.isFavourite).isTrue()
    }

    private fun post(
        id: Int = 600,
        discussionId: Int = 501,
        parentId: Int? = null,
        author: MoodlePostAuthor = author(id = 7, fullName = "Mario Rossi"),
        subject: String = "Re: Domanda",
        message: String = "<p>risposta</p>",
        capabilities: MoodlePostCapabilities =
            MoodlePostCapabilities(reply = true, edit = true, delete = false),
        attachments: List<MoodlePostAttachment> = emptyList(),
        isDeleted: Boolean = false,
    ) = MoodleForumPost(
        id = id,
        discussionId = discussionId,
        parentId = parentId,
        author = author,
        subject = subject,
        message = message,
        capabilities = capabilities,
        attachments = attachments,
        isDeleted = isDeleted,
        timeCreated = 1_000L,
        timeModified = 2_000L,
    )

    private fun author(id: Int? = null, fullName: String? = null) =
        MoodlePostAuthor(id = id, fullName = fullName, urls = MoodlePostAuthorUrls())

    private fun attachment(fileName: String, url: String = "https://m/x", fileSize: Long = 12L) =
        MoodlePostAttachment(
            fileName = fileName,
            url = url,
            fileSize = fileSize,
            urls = MoodlePostAttachmentUrls(),
            html = MoodlePostAttachmentHtml(),
        )

    @Test
    fun `post toEntity flattens the capability flags`() {
        val entity = post().toEntity(account, discussionIdFallback = 999)
        assertThat(entity.canReply).isTrue()
        assertThat(entity.canEdit).isTrue()
        assertThat(entity.canDelete).isFalse()
    }

    @Test
    fun `post toEntity uses the discussion id and falls back when null`() {
        assertThat(post(discussionId = 501).toEntity(account, discussionIdFallback = 999).discussionId)
            .isEqualTo(501)
        assertThat(post(discussionId = 0).toEntity(account, discussionIdFallback = 999).discussionId)
            .isEqualTo(999)
    }

    @Test
    fun `post toEntity defaults author, subject and message`() {
        val entity = post(author = author(), subject = "", message = "").toEntity(account, discussionIdFallback = 999)
        assertThat(entity.authorName).isEqualTo("—")
        assertThat(entity.authorUserId).isNull()
        assertThat(entity.subject).isEqualTo("")
        assertThat(entity.message).isEqualTo("")
    }

    @Test
    fun `post toEntity leaves the attachments column null when there are none`() {
        assertThat(post(attachments = emptyList()).toEntity(account, discussionIdFallback = 999).attachmentsJson)
            .isNull()
    }

    @Test
    fun `post toEntity packs attachments into the JSON column`() {
        val file = attachment(fileName = "slides.pdf")
        val entity = post(attachments = listOf(file)).toEntity(account, discussionIdFallback = 999)
        assertThat(entity.attachmentsJson).isNotNull()
        assertThat(entity.attachmentsJson).contains("slides.pdf")
    }

    @Test
    fun `post toEntity names an attachment with a missing file name file`() {
        val file = attachment(fileName = "")
        val entity = post(attachments = listOf(file)).toEntity(account, discussionIdFallback = 999)
        assertThat(entity.attachmentsJson).contains("file")
    }

    @Test
    fun `post entity toDomain decodes attachments and wraps ids`() {
        val source = post(
            attachments = listOf(attachment(fileName = "a.pdf", url = "u", fileSize = 5L)),
        ).toEntity(account, discussionIdFallback = 999)
        val domain = source.toDomain()
        assertThat(domain.id).isEqualTo(PostId(600))
        assertThat(domain.discussionId).isEqualTo(DiscussionId(501))
        assertThat(domain.attachments).hasSize(1)
        assertThat(domain.attachments.single().fileName).isEqualTo("a.pdf")
    }

    @Test
    fun `post entity toDomain wraps a present parent id`() {
        val entity = PostEntity(
            accountId = "acc-1",
            postId = 601,
            discussionId = 501,
            parentId = 600,
            authorUserId = 7,
            authorName = "Mario",
            authorAvatarUrl = null,
            subject = "Re",
            message = "x",
            createdAtMs = null,
            modifiedAtMs = null,
            attachmentsJson = null,
            canReply = false,
            canEdit = false,
            canDelete = false,
            isDeleted = false,
        )
        assertThat(entity.toDomain().parentId).isEqualTo(PostId(600))
    }

    @Test
    fun `post entity toDomain decodes a malformed attachment blob to no attachments`() {
        val entity = PostEntity(
            accountId = "acc-1",
            postId = 601,
            discussionId = 501,
            parentId = null,
            authorUserId = null,
            authorName = "—",
            authorAvatarUrl = null,
            subject = "",
            message = "",
            createdAtMs = null,
            modifiedAtMs = null,
            attachmentsJson = "{bad json",
            canReply = false,
            canEdit = false,
            canDelete = false,
            isDeleted = false,
        )
        assertThat(entity.toDomain().attachments).isEmpty()
        assertThat(entity.toDomain().parentId).isNull()
    }
}
