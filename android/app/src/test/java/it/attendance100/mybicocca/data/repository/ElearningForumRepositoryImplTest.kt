package it.attendance100.mybicocca.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import it.attendance100.mybicocca.core.time.StalePolicy
import it.attendance100.mybicocca.data.auth.ElearningSession
import it.attendance100.mybicocca.data.auth.SessionManager
import it.attendance100.mybicocca.data.local.elearning.forum.DiscussionEntity
import it.attendance100.mybicocca.data.local.elearning.forum.ForumDao
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncScope
import it.attendance100.mybicocca.data.local.elearning.sync.ElearningSyncStateDao
import it.attendance100.mybicocca.data.remote.elearning.api.ElearningApi
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAddForumDiscussionResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningAddForumPostResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningDiscussionOption
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningForumDiscussion
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetCourseUserGroupsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetForumDiscussionPostsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningGetForumDiscussionsResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningPrepareDraftAreaResponse
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUploadedFile
import it.attendance100.mybicocca.data.remote.elearning.dto.ElearningUserGroup
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Behaviour coverage for the forum repository write path: page-0-replace vs later-page-merge of
 * discussions, the optimistic favourite toggle that reverts on server failure, the mark-read that
 * clears unread before the best-effort view log, the plain-to-HTML promotion on posts, the
 * attachment draft id riding as the `attachmentsid` option, and the group-name fallback.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ElearningForumRepositoryImplTest {

    private val accountId = AccountId("acc-1")
    private val courseId = CourseId(42)
    private val forumId = ForumId(11)
    private val discussionId = DiscussionId(22)
    private val postId = PostId(33)
    private val lmsUserId = 7
    private val account = elearningRepoTestAccount(accountId, lmsUserId)
    private val token = "x".repeat(32)

    private val sessionManager = mockk<SessionManager>(relaxed = true)
    private val forumDao = mockk<ForumDao>(relaxed = true)
    private val syncStateDao = mockk<ElearningSyncStateDao>(relaxed = true)
    private val elearningApi = mockk<ElearningApi>(relaxed = true)
    private val stalePolicy = StalePolicy(defaultTtlMs = 60_000L)

    private fun newRepository(scope: CoroutineScope): ElearningForumRepositoryImpl {
        every { sessionManager.activeAccount } returns MutableStateFlow(account)
        coEvery { sessionManager.elearning() } returns ElearningSession(elearningApi, token)
        coEvery { syncStateDao.getState(any(), any(), any()) } returns null
        return ElearningForumRepositoryImpl(sessionManager, forumDao, syncStateDao, stalePolicy, scope)
    }

    private fun discussionDto(id: Int) = ElearningForumDiscussion(id = id, name = "Thread $id")

    @Test
    fun `refreshDiscussions page zero replaces the forum list`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            elearningApi.forums.getForumDiscussions(token, forumId.value, any(), 0, any(), 0)
        } returns ElearningGetForumDiscussionsResponse(discussions = listOf(discussionDto(1), discussionDto(2)))

        val count = repository.refreshDiscussions(accountId, forumId, page = 0, perPage = 10)

        assertThat(count).isEqualTo(2)
        val rows = slot<List<DiscussionEntity>>()
        coVerify { forumDao.replaceDiscussionsForForum(accountId.value, forumId.value, capture(rows)) }
        assertThat(rows.captured).hasSize(2)
        coVerify(exactly = 0) { forumDao.upsertDiscussions(any()) }
        coVerify { syncStateDao.upsertState(match { it.scope == ElearningSyncScope.FORUM_DISCUSSIONS }) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshDiscussions later page merges instead of replacing`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            elearningApi.forums.getForumDiscussions(token, forumId.value, any(), 1, any(), 0)
        } returns ElearningGetForumDiscussionsResponse(discussions = listOf(discussionDto(3)))

        repository.refreshDiscussions(accountId, forumId, page = 1, perPage = 10)

        coVerify { forumDao.upsertDiscussions(any()) }
        coVerify(exactly = 0) { forumDao.replaceDiscussionsForForum(any(), any(), any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `refreshDiscussions later page with no rows does not write`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            elearningApi.forums.getForumDiscussions(token, forumId.value, any(), 2, any(), 0)
        } returns ElearningGetForumDiscussionsResponse(discussions = emptyList())

        val count = repository.refreshDiscussions(accountId, forumId, page = 2, perPage = 10)

        assertThat(count).isEqualTo(0)
        coVerify(exactly = 0) { forumDao.upsertDiscussions(any()) }
        coVerify(exactly = 0) { forumDao.replaceDiscussionsForForum(any(), any(), any()) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `setFavourite reverts the optimistic flag when the server call fails`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.forums.toggleFavourite(token, discussionId.value, true) } throws RuntimeException("boom")

        val thrown = repository.runCatching {
            setFavourite(accountId, forumId, discussionId, favourite = true)
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(RuntimeException::class.java)
        coVerify(exactly = 1) { forumDao.setDiscussionFavourite(accountId.value, discussionId.value, true) }
        coVerify(exactly = 1) { forumDao.setDiscussionFavourite(accountId.value, discussionId.value, false) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `setFavourite keeps the optimistic flag when the server call succeeds`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.setFavourite(accountId, forumId, discussionId, favourite = true)

        coVerify(exactly = 1) { forumDao.setDiscussionFavourite(accountId.value, discussionId.value, true) }
        coVerify(exactly = 0) { forumDao.setDiscussionFavourite(accountId.value, discussionId.value, false) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `markDiscussionRead clears unread then logs the view best-effort`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.forums.markDiscussionViewed(token, discussionId.value) } throws RuntimeException("offline")

        repository.markDiscussionRead(accountId, discussionId)

        coVerify { forumDao.clearDiscussionUnread(accountId.value, discussionId.value) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `createDiscussion promotes plain text to HTML and refreshes best-effort`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            elearningApi.forums.addDiscussion(token, forumId.value, "Subj", any(), null, any())
        } returns ElearningAddForumDiscussionResponse(discussionId = 99)
        coEvery {
            elearningApi.forums.getForumDiscussions(token, forumId.value, any(), 0, any(), 0)
        } returns ElearningGetForumDiscussionsResponse(discussions = emptyList())

        val newId = repository.createDiscussion(accountId, forumId, "Subj", "line1\nline2", groupId = null, attachmentDraftItemId = null)

        assertThat(newId).isEqualTo(DiscussionId(99))
        val message = slot<String>()
        coVerify { elearningApi.forums.addDiscussion(token, forumId.value, "Subj", capture(message), null, any()) }
        assertThat(message.captured).isEqualTo("line1<br>line2")
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `reply carries the attachment draft id as the attachmentsid option`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            elearningApi.forums.addPost(token, postId.value, "Re", any(), any())
        } returns ElearningAddForumPostResponse(postId = 77)
        coEvery {
            elearningApi.forums.getDiscussionPosts(token, discussionId.value, true)
        } returns ElearningGetForumDiscussionPostsResponse(posts = emptyList())

        val newPost = repository.reply(accountId, discussionId, postId, "Re", "body", attachmentDraftItemId = 1234)

        assertThat(newPost).isEqualTo(PostId(77))
        val options = slot<List<ElearningDiscussionOption>>()
        coVerify { elearningApi.forums.addPost(token, postId.value, "Re", any(), capture(options)) }
        assertThat(options.captured.first().name).isEqualTo("attachmentsid")
        assertThat(options.captured.first().value).isEqualTo("1234")
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `reply with no attachment sends null options`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            elearningApi.forums.addPost(token, postId.value, "Re", any(), any())
        } returns ElearningAddForumPostResponse(postId = 78)
        coEvery {
            elearningApi.forums.getDiscussionPosts(token, discussionId.value, true)
        } returns ElearningGetForumDiscussionPostsResponse(posts = emptyList())

        repository.reply(accountId, discussionId, postId, "Re", "body", attachmentDraftItemId = null)

        coVerify { elearningApi.forums.addPost(token, postId.value, "Re", any(), null) }
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `getCourseGroups falls back to a synthetic name when the group has none`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery {
            elearningApi.forums.getCourseUserGroups(token, courseId.value, 0)
        } returns ElearningGetCourseUserGroupsResponse(
            groups = listOf(
                ElearningUserGroup(id = 5, name = "Gruppo A"),
                ElearningUserGroup(id = 6, name = null),
            ),
        )

        val groups = repository.getCourseGroups(accountId, courseId)

        assertThat(groups).hasSize(2)
        assertThat(groups[0].name).isEqualTo("Gruppo A")
        assertThat(groups[1].name).isEqualTo("Gruppo 6")
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `uploadAttachments returns the stored draft area id`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.files.uploadToDraftArea(token, any(), 0) } returns listOf(
            ElearningUploadedFile(itemId = 4321),
        )

        val id = repository.uploadAttachments(accountId, files = emptyList(), baseDraftItemId = null)

        assertThat(id).isEqualTo(4321)
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `prepareEditAttachments returns the seeded draft id`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)
        coEvery { elearningApi.forums.prepareDraftAreaForPost(token, postId.value) } returns
            ElearningPrepareDraftAreaResponse(draftItemId = 9999)

        val id = repository.prepareEditAttachments(accountId, postId)

        assertThat(id).isEqualTo(9999)
        scope.coroutineContext[Job]?.cancel()
    }

    @Test
    fun `clearForAccount delegates to the DAO`() = runTest {
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        val repository = newRepository(scope)

        repository.clearForAccount(accountId)

        coVerify { forumDao.clearAllForAccount(accountId.value) }
        scope.coroutineContext[Job]?.cancel()
    }
}
