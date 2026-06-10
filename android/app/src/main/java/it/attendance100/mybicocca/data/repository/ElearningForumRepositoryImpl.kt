package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.forum.ForumDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.mapper.elearning.toDomain
import it.attendance100.mybicocca.data.mapper.elearning.toEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningDiscussionOption
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningDiscussionSortOrder
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUpload
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumAttachmentUpload
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumGroup
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumHtml
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.repository.ElearningForumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed forum repository over the Moodle mod_forum web services.
 *
 * Reads stream straight from the forum tables. Syncs write through:
 * mod_forum_get_forums_by_courses replaces a course's forums (TTL-gated and deduplicated so
 * concurrent callers share one in-flight refresh), mod_forum_get_forum_discussions fills the
 * discussion list page by page (page 0 replaces the forum's list, later pages merge), and
 * mod_forum_get_discussion_posts replaces a thread wholesale.
 *
 * Writes go server-first and re-sync the affected cache afterwards: add_discussion and
 * add_discussion_post re-fetch best-effort, so a posting failure surfaces but a follow-up
 * refresh failure does not, while update_discussion_post and delete_post propagate refresh
 * failures too. Two writes deviate: the favourite toggle flips the cached flag optimistically
 * and reverts it when the server call fails, and mark-read clears the cached unread count while
 * only logging the view (mod_forum_view_forum_discussion) best-effort.
 *
 * Attachments ride Moodle's draft-area flow: files upload through webservice/upload.php into a
 * draft area — fresh, or seeded with a post's current files by
 * mod_forum_prepare_draft_area_for_post — and the resulting draft id travels on the write call
 * as its `attachmentsid` option.
 */
@Singleton
class ElearningForumRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val forumDao: ForumDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningForumRepository {

    /** Collapses concurrent course-forum refreshes into one shared job per (account, course). */
    private val courseRefreshInFlight = ConcurrentHashMap<RefreshKey, Deferred<Unit>>()

    override fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Forum>>> =
        forumDao.observeForCourse(accountId.value, courseId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Forum>> }
            .flowOn(Dispatchers.Default)

    override fun observe(accountId: AccountId, forumId: ForumId): Flow<Loadable<Forum>> =
        forumDao.observe(accountId.value, forumId.value)
            .map { row -> if (row == null) Loadable.NotYetLoaded as Loadable<Forum> else Loadable.Loaded(row.toDomain()) }
            .flowOn(Dispatchers.Default)

    override fun observeDiscussions(accountId: AccountId, forumId: ForumId): Flow<Loadable<List<Discussion>>> =
        forumDao.observeDiscussions(accountId.value, forumId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Discussion>> }
            .flowOn(Dispatchers.Default)

    override fun observePosts(accountId: AccountId, discussionId: DiscussionId): Flow<Loadable<List<Post>>> =
        forumDao.observePosts(accountId.value, discussionId.value)
            .map { rows -> Loadable.Loaded(rows.map { it.toDomain() }) as Loadable<List<Post>> }
            .flowOn(Dispatchers.Default)

    override suspend fun refreshForumsForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) {
        val key = RefreshKey(accountId, courseId)
        val deferred = courseRefreshInFlight.computeIfAbsent(key) {
            scope.async(start = CoroutineStart.LAZY) {
                doRefreshForumsForCourse(accountId, courseId, force)
            }.also { d -> d.invokeOnCompletion { courseRefreshInFlight.remove(key, d) } }
        }
        deferred.await()
    }

    private suspend fun doRefreshForumsForCourse(accountId: AccountId, courseId: CourseId, force: Boolean) {
        if (!force && !isStale(accountId, ElearningSyncScope.COURSE_FORUMS, courseId.value.toLong())) return
        val (api, token) = sessionManager.elearning()
        val response = api.forums.getForumsForCourse(token, courseId.value)
        val rows = response.forums.map { it.toEntity(accountId) }
        forumDao.replaceForumsForCourse(accountId.value, courseId.value, rows)
        stamp(accountId, ElearningSyncScope.COURSE_FORUMS, courseId.value.toLong())
    }

    override suspend fun refreshDiscussions(
        accountId: AccountId,
        forumId: ForumId,
        page: Int,
        perPage: Int,
    ): Int {
        val (api, token) = sessionManager.elearning()
        val response = api.forums.getForumDiscussions(
            wsToken = token,
            forumId = forumId.value,
            sortOrder = ElearningDiscussionSortOrder.LAST_POST_DESCENDING,
            page = page,
            perPage = perPage,
            groupId = 0,
        )
        val rows = response.discussions.map { it.toEntity(accountId, forumId.value) }
        if (page == 0) {
            forumDao.replaceDiscussionsForForum(accountId.value, forumId.value, rows)
        } else if (rows.isNotEmpty()) {
            forumDao.upsertDiscussions(rows)
        }
        stamp(accountId, ElearningSyncScope.FORUM_DISCUSSIONS, forumId.value.toLong())
        return rows.size
    }

    override suspend fun refreshPosts(accountId: AccountId, discussionId: DiscussionId) {
        val (api, token) = sessionManager.elearning()
        val response = api.forums.getDiscussionPosts(
            wsToken = token,
            discussionId = discussionId.value,
            includeInlineAttachments = true,
        )
        val rows = response.posts.map { it.toEntity(accountId, discussionId.value) }
        forumDao.replacePostsForDiscussion(accountId.value, discussionId.value, rows)
        stamp(accountId, ElearningSyncScope.DISCUSSION_POSTS, discussionId.value.toLong())
    }

    override suspend fun createDiscussion(
        accountId: AccountId,
        forumId: ForumId,
        subject: String,
        message: String,
        groupId: Int?,
        attachmentDraftItemId: Int?,
    ): DiscussionId {
        val (api, token) = sessionManager.elearning()
        val response = api.forums.addDiscussion(
            wsToken = token,
            forumId = forumId.value,
            subject = subject,
            message = ForumHtml.plainToHtml(message),
            groupId = groupId,
            options = attachmentOptions(attachmentDraftItemId),
        )
        runCatching { refreshDiscussions(accountId, forumId, page = 0) }
        return DiscussionId(response.discussionId)
    }

    override suspend fun reply(
        accountId: AccountId,
        discussionId: DiscussionId,
        parentPostId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int?,
    ): PostId {
        val (api, token) = sessionManager.elearning()
        val response = api.forums.addPost(
            wsToken = token,
            postId = parentPostId.value,
            subject = subject,
            message = ForumHtml.plainToHtml(message),
            options = attachmentOptions(attachmentDraftItemId),
        )
        runCatching { refreshPosts(accountId, discussionId) }
        return PostId(response.postId)
    }

    override suspend fun editPost(
        accountId: AccountId,
        discussionId: DiscussionId,
        postId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int?,
    ) {
        val (api, token) = sessionManager.elearning()
        api.forums.updatePost(
            wsToken = token,
            postId = postId.value,
            subject = subject,
            message = ForumHtml.plainToHtml(message),
            options = attachmentOptions(attachmentDraftItemId),
        )
        refreshPosts(accountId, discussionId)
    }

    override suspend fun deletePost(accountId: AccountId, discussionId: DiscussionId, postId: PostId) {
        val (api, token) = sessionManager.elearning()
        api.forums.deletePost(token, postId.value)
        refreshPosts(accountId, discussionId)
    }

    override suspend fun setFavourite(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        favourite: Boolean,
    ) {
        forumDao.setDiscussionFavourite(accountId.value, discussionId.value, favourite)
        try {
            val (api, token) = sessionManager.elearning()
            api.forums.toggleFavourite(token, discussionId.value, favourite)
        } catch (t: Throwable) {
            forumDao.setDiscussionFavourite(accountId.value, discussionId.value, !favourite)
            throw t
        }
    }

    override suspend fun setDiscussionSubscription(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        subscribed: Boolean,
    ) {
        val (api, token) = sessionManager.elearning()
        api.forums.setDiscussionSubscription(token, forumId.value, discussionId.value, subscribed)
    }

    override suspend fun markDiscussionRead(accountId: AccountId, discussionId: DiscussionId) {
        forumDao.clearDiscussionUnread(accountId.value, discussionId.value)
        runCatching {
            val (api, token) = sessionManager.elearning()
            api.forums.markDiscussionViewed(token, discussionId.value)
        }
    }

    override suspend fun getCourseGroups(accountId: AccountId, courseId: CourseId): List<ForumGroup> {
        val (api, token) = sessionManager.elearning()
        return api.forums.getCourseUserGroups(token, courseId.value).groups
            .map { ForumGroup(id = it.id, name = it.name ?: "Gruppo ${it.id}") }
    }

    override suspend fun uploadAttachments(
        accountId: AccountId,
        files: List<ForumAttachmentUpload>,
        baseDraftItemId: Int?,
    ): Int {
        val (api, token) = sessionManager.elearning()
        val uploads = files.map { ElearningUpload(it.fileName, it.mimeType, it.bytes) }
        val stored = api.files.uploadToDraftArea(token, uploads, itemId = baseDraftItemId ?: 0)
        return stored.firstOrNull()?.itemId
            ?: baseDraftItemId
            ?: error("Upload did not return a draft area id")
    }

    override suspend fun prepareEditAttachments(accountId: AccountId, postId: PostId): Int {
        val (api, token) = sessionManager.elearning()
        return api.forums.prepareDraftAreaForPost(token, postId.value).draftItemId
    }

    /** Wraps a draft-area id as the `attachmentsid` option the mod_forum write services accept. */
    private fun attachmentOptions(draftItemId: Int?): List<ElearningDiscussionOption>? =
        draftItemId?.let { listOf(ElearningDiscussionOption("attachmentsid", it.toString())) }

    override suspend fun clearForAccount(accountId: AccountId) {
        forumDao.clearAllForAccount(accountId.value)
    }

    /** Whether the scope's last successful sync is older than its TTL; never-synced reads as stale. */
    private suspend fun isStale(accountId: AccountId, scope: String, scopeId: Long): Boolean {
        val state = syncStateDao.getState(accountId.value, scope, scopeId) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(scope)
    }

    /** Records a successful sync of the scope for later staleness checks. */
    private suspend fun stamp(accountId: AccountId, scope: String, scopeId: Long) {
        syncStateDao.upsertState(
            ElearningSyncStateEntity(
                accountId = accountId.value,
                scope = scope,
                scopeId = scopeId,
                lastRefreshedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    private data class RefreshKey(val accountId: AccountId, val courseId: CourseId)
}
