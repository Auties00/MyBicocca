package it.attendance100.mybicocca.data.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.forum.ForumDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateEntity
import it.attendance100.mybicocca.data.mapper.toDomain
import it.attendance100.mybicocca.data.mapper.toEntity
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningDiscussionSortOrder
import it.attendance100.mybicocca.di.ApplicationScope
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
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

@Singleton
class ElearningForumRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager,
    private val forumDao: ForumDao,
    private val syncStateDao: ElearningSyncStateDao,
    private val stalePolicy: StalePolicy,
    @ApplicationScope private val scope: CoroutineScope,
) : ElearningForumRepository {

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
    ) {
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
    ): DiscussionId {
        val (api, token) = sessionManager.elearning()
        val response = api.forums.addDiscussion(
            wsToken = token,
            forumId = forumId.value,
            subject = subject,
            message = message,
            groupId = groupId,
            options = null,
        )
        runCatching { refreshDiscussions(accountId, forumId, page = 0) }
        return DiscussionId(response.discussionId)
    }

    override suspend fun reply(
        accountId: AccountId,
        parentPostId: PostId,
        subject: String,
        message: String,
    ): PostId {
        val (api, token) = sessionManager.elearning()
        val response = api.forums.addPost(
            wsToken = token,
            postId = parentPostId.value,
            subject = subject,
            message = message,
            options = null,
        )
        return PostId(response.postId)
    }

    override suspend fun clearForAccount(accountId: AccountId) {
        forumDao.clearAllForAccount(accountId.value)
    }

    private suspend fun isStale(accountId: AccountId, scope: String, scopeId: Long): Boolean {
        val state = syncStateDao.getState(accountId.value, scope, scopeId) ?: return true
        return kotlin.time.Clock.System.now().toEpochMilliseconds() - state.lastRefreshedAtMs > stalePolicy.ttlFor(scope)
    }

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
