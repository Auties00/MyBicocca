package it.attendance100.mybicocca.demo.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.demo.DemoClock
import it.attendance100.mybicocca.demo.DemoCourse
import it.attendance100.mybicocca.demo.DemoCourses
import it.attendance100.mybicocca.demo.DemoElearning
import it.attendance100.mybicocca.demo.DemoStudent
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumAttachmentUpload
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumGroup
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumHtml
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** One forum per course seeded with three threads; posting, editing and starring work in memory. */
@Singleton
class DemoElearningForumRepository @Inject constructor() : ElearningForumRepository {

    private val posts = MutableStateFlow(DemoCourses.all.flatMap(::seedPosts))
    private val favourites = MutableStateFlow(emptySet<DiscussionId>())
    private val read = MutableStateFlow(emptySet<DiscussionId>())
    private val pinned = DemoCourses.all.map { discussionId(it, 0) }.toSet()
    private var nextId = 990_000
    private var nextThread = FIRST_NEW_THREAD

    override fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Forum>>> =
        posts.map { all -> Loadable.Loaded(listOfNotNull(DemoElearning.course(courseId)?.let { forum(it, all) })) }

    override fun observe(accountId: AccountId, forumId: ForumId): Flow<Loadable<Forum>> {
        val course = courseOf(forumId) ?: return flowOf(Loadable.NotYetLoaded)
        return posts.map { Loadable.Loaded(forum(course, it)) }
    }

    override fun observeDiscussions(accountId: AccountId, forumId: ForumId): Flow<Loadable<List<Discussion>>> =
        combine(posts, favourites, read) { all, starred, seen ->
            val discussions = all.filter { forumOf(it.discussionId) == forumId }
                .groupBy { it.discussionId }
                .map { (id, thread) -> discussion(id, forumId, thread, id in starred, id in seen) }
                .sortedWith(compareByDescending<Discussion> { it.isPinned }.thenByDescending { it.timeModified })
            Loadable.Loaded(discussions)
        }

    override fun observePosts(accountId: AccountId, discussionId: DiscussionId): Flow<Loadable<List<Post>>> =
        posts.map { all -> Loadable.Loaded(all.filter { it.discussionId == discussionId }) }

    override suspend fun refreshForumsForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) = Unit

    override suspend fun refreshDiscussions(accountId: AccountId, forumId: ForumId, page: Int, perPage: Int): Int = 0

    override suspend fun refreshPosts(accountId: AccountId, discussionId: DiscussionId) = Unit

    override suspend fun createDiscussion(
        accountId: AccountId,
        forumId: ForumId,
        subject: String,
        message: String,
        groupId: Int?,
        attachmentDraftItemId: Int?,
    ): DiscussionId {
        // Keeps the forum recoverable from the id, as forumOf expects.
        val id = DiscussionId(forumId.value * 1000 + nextThread++)
        posts.update { it + ownPost(id, parent = null, subject = subject, message = message) }
        return id
    }

    override suspend fun reply(
        accountId: AccountId,
        discussionId: DiscussionId,
        parentPostId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int?,
    ): PostId {
        val post = ownPost(discussionId, parentPostId, subject, message)
        posts.update { it + post }
        return post.id
    }

    override suspend fun editPost(
        accountId: AccountId,
        discussionId: DiscussionId,
        postId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int?,
    ) {
        posts.update { all ->
            all.map {
                if (it.id == postId) it.copy(subject = subject, message = message, modifiedAt = DemoClock.fixed.instant())
                else it
            }
        }
    }

    override suspend fun deletePost(accountId: AccountId, discussionId: DiscussionId, postId: PostId) {
        posts.update { all -> all.filterNot { it.id == postId } }
    }

    override suspend fun setFavourite(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        favourite: Boolean,
    ) {
        favourites.update { if (favourite) it + discussionId else it - discussionId }
    }

    override suspend fun setDiscussionSubscription(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        subscribed: Boolean,
    ) = Unit

    override suspend fun markDiscussionRead(accountId: AccountId, discussionId: DiscussionId) {
        read.update { it + discussionId }
    }

    override suspend fun getCourseGroups(accountId: AccountId, courseId: CourseId): List<ForumGroup> = emptyList()

    override suspend fun uploadAttachments(
        accountId: AccountId,
        files: List<ForumAttachmentUpload>,
        baseDraftItemId: Int?,
    ): Int = baseDraftItemId ?: 1

    override suspend fun prepareEditAttachments(accountId: AccountId, postId: PostId): Int = 1

    override suspend fun clearForAccount(accountId: AccountId) = Unit

    private fun courseOf(forumId: ForumId): DemoCourse? =
        DemoCourses.all.firstOrNull { DemoElearning.forumId(it) == forumId.value }

    private fun forumOf(discussionId: DiscussionId) = ForumId(discussionId.value / 1000)

    private fun discussionId(course: DemoCourse, index: Int) =
        DiscussionId(DemoElearning.forumId(course) * 1000 + index)

    private fun forum(course: DemoCourse, all: List<Post>): Forum {
        val forumId = ForumId(DemoElearning.forumId(course))
        val own = all.filter { forumOf(it.discussionId) == forumId }
        return Forum(
            id = forumId,
            courseId = CourseId(course.moodleCourseId),
            cmId = DemoElearning.cmId(course, 1),
            name = "Annunci",
            intro = "Avvisi del docente e domande sul corso.",
            type = ForumType.General,
            discussionCount = own.map { it.discussionId }.distinct().size,
            postCount = own.size,
            canCreateDiscussions = true,
            canSubscribe = true,
            canAttachFiles = true,
        )
    }

    private fun discussion(
        id: DiscussionId,
        forumId: ForumId,
        thread: List<Post>,
        favourite: Boolean,
        seen: Boolean,
    ): Discussion {
        val first = thread.first()
        val last = thread.last()
        return Discussion(
            id = id,
            forumId = forumId,
            firstPostId = first.id,
            subject = first.subject,
            authorUserId = first.authorUserId,
            authorName = first.authorName,
            authorAvatarUrl = null,
            createdAt = first.createdAt,
            timeModified = last.createdAt,
            isPinned = id in pinned,
            isLocked = false,
            unreadCount = if (seen || id.value % 1000 != 1) 0 else thread.size - 1,
            replyCount = thread.size - 1,
            lastPostAuthorName = last.authorName,
            messagePreview = ForumHtml.htmlToPlain(first.message).take(140),
            hasAttachments = false,
            canReply = true,
            isFavourite = favourite,
            canFavourite = true,
        )
    }

    private fun ownPost(discussionId: DiscussionId, parent: PostId?, subject: String, message: String) = Post(
        id = PostId(nextId++),
        discussionId = discussionId,
        parentId = parent,
        authorUserId = DemoStudent.account.learning.lmsUserId,
        authorName = DemoStudent.FULL_NAME,
        authorAvatarUrl = null,
        subject = subject,
        message = message,
        createdAt = DemoClock.fixed.instant(),
        modifiedAt = null,
        attachments = emptyList(),
        canReply = true,
        canEdit = true,
        canDelete = true,
        isDeleted = false,
    )

    private fun seedPosts(course: DemoCourse): List<Post> {
        val teacher = course.teacher
        val exam = DemoClock.now.plusDays(8)
        return thread(
            course, 0, "Calendario degli appelli estivi",
            Line(teacher, DemoClock.now.minusDays(6).withHour(9).withMinute(12), "<p>Gentili studenti, gli appelli della sessione estiva sono stati pubblicati su Esse3. Il primo è fissato per il ${exam.dayOfMonth} giugno.</p><p>Le iscrizioni chiudono tre giorni prima della prova.</p>"),
        ) + thread(
            course, 1, "Dubbio sull'esercizio 3 dell'ultima esercitazione",
            Line("Giulia Moretti", DemoClock.now.minusDays(2).withHour(18).withMinute(40), "<p>Buonasera, nell'esercizio 3 non mi è chiaro se si debba considerare anche il caso con input vuoto. Qualcuno l'ha risolto?</p>"),
            Line("Andrea Ricci", DemoClock.now.minusDays(2).withHour(21).withMinute(5), "<p>Io l'ho trattato come caso base, il risultato torna con la soluzione del libro.</p>"),
            Line(teacher, DemoClock.now.minusDays(1).withHour(8).withMinute(31), "<p>Confermo: l'input vuoto va gestito esplicitamente. Ne riparliamo giovedì a lezione.</p>"),
        ) + thread(
            course, 2, "Gruppo di studio in biblioteca",
            Line("Federica Sala", DemoClock.now.minusDays(4).withHour(14).withMinute(22), "<p>Ciao a tutti, qualcuno vuole ripassare insieme in biblioteca U6 la prossima settimana?</p>"),
            Line(DemoStudent.FULL_NAME, DemoClock.now.minusDays(4).withHour(16).withMinute(3), "<p>Io ci sono, martedì pomeriggio per me va bene.</p>"),
        )
    }

    private companion object {
        // Above the seeded thread indexes, so a new thread never lands on one of them.
        const val FIRST_NEW_THREAD = 100
    }

    private data class Line(val author: String, val at: LocalDateTime, val html: String)

    private fun thread(course: DemoCourse, index: Int, subject: String, vararg lines: Line): List<Post> {
        val id = discussionId(course, index)
        return lines.mapIndexed { position, line ->
            val own = line.author == DemoStudent.FULL_NAME
            Post(
                id = PostId(id.value * 10 + position),
                discussionId = id,
                parentId = if (position == 0) null else PostId(id.value * 10),
                authorUserId = if (own) DemoStudent.account.learning.lmsUserId else 31000 + line.author.hashCode().mod(8000),
                authorName = line.author,
                authorAvatarUrl = null,
                subject = if (position == 0) subject else "Re: $subject",
                message = line.html,
                createdAt = DemoClock.instant(line.at),
                modifiedAt = null,
                attachments = emptyList(),
                canReply = true,
                canEdit = own,
                canDelete = own,
                isDeleted = false,
            )
        }
    }
}
