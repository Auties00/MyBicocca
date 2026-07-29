package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.core.state.SyncStatus
import it.attendance100.mybicocca.core.text.StringResolver
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.usecase.elearning.forum.CreateDiscussionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.DeletePostUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.EditPostUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.GetCourseGroupsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.MarkDiscussionReadUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveForumUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObservePostsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.PrepareEditAttachmentsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshPostsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ReplyToPostUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.SetDiscussionFavouriteUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.SetDiscussionSubscriptionUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.UploadForumAttachmentsUseCase
import it.attendance100.mybicocca.testing.MainDispatcherRule
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ComposerTarget
import it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum.state.ForumSheetEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Covers the forum sheet ViewModel: first-page sync and pagination state, the open/close-thread
 * lifecycle, the favourite/subscription/delete actions, and the composer target machine including
 * the new-discussion submit that opens the freshly created thread.
 */
class ForumSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accountId = AccountId("acc-1")
    private val forumId = ForumId(10)
    private val courseId = CourseId(3)

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun forum(canAttach: Boolean = false): Forum = Forum(
        id = forumId,
        courseId = courseId,
        cmId = null,
        name = "Forum",
        intro = null,
        type = ForumType.General,
        discussionCount = 0,
        postCount = 0,
        canCreateDiscussions = true,
        canSubscribe = true,
        canAttachFiles = canAttach,
    )

    private fun discussion(id: Int, favourite: Boolean = false): Discussion = Discussion(
        id = DiscussionId(id),
        forumId = forumId,
        firstPostId = PostId(id * 10),
        subject = "Thread $id",
        authorUserId = null,
        authorName = "Author",
        authorAvatarUrl = null,
        createdAt = null,
        timeModified = null,
        isPinned = false,
        isLocked = false,
        unreadCount = 0,
        replyCount = 0,
        lastPostAuthorName = null,
        messagePreview = null,
        hasAttachments = false,
        canReply = true,
        isFavourite = favourite,
        canFavourite = true,
    )

    private fun post(id: Int, subject: String = "Subject"): Post = Post(
        id = PostId(id),
        discussionId = DiscussionId(1),
        parentId = null,
        authorUserId = null,
        authorName = "Author",
        authorAvatarUrl = null,
        subject = subject,
        message = "<p>body</p>",
        createdAt = null,
        modifiedAt = null,
        attachments = emptyList(),
        canReply = true,
        canEdit = true,
        canDelete = true,
        isDeleted = false,
    )

    private fun build(
        key: SheetRoute.Forum = SheetRoute.Forum(forumId = forumId.value, courseId = courseId.value),
        activeAccount: Account? = account,
        observeForum: ObserveForumUseCase = mockk {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(forum()))
        },
        observeDiscussions: ObserveDiscussionsUseCase = mockk {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(emptyList()))
        },
        refreshDiscussions: RefreshDiscussionsUseCase = mockk {
            coEvery { this@mockk.invoke(any(), any(), any(), any()) } returns 0
        },
        observePosts: ObservePostsUseCase = mockk {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.NotYetLoaded)
        },
        refreshPosts: RefreshPostsUseCase = mockk(relaxed = true),
        createDiscussion: CreateDiscussionUseCase = mockk(relaxed = true),
        reply: ReplyToPostUseCase = mockk(relaxed = true),
        edit: EditPostUseCase = mockk(relaxed = true),
        delete: DeletePostUseCase = mockk(relaxed = true),
        setFavourite: SetDiscussionFavouriteUseCase = mockk(relaxed = true),
        setSubscription: SetDiscussionSubscriptionUseCase = mockk(relaxed = true),
        markRead: MarkDiscussionReadUseCase = mockk(relaxed = true),
        upload: UploadForumAttachmentsUseCase = mockk(relaxed = true),
        getGroups: GetCourseGroupsUseCase = mockk {
            coEvery { this@mockk.invoke(any(), any()) } returns emptyList()
        },
        prepareEdit: PrepareEditAttachmentsUseCase = mockk(relaxed = true),
        attachmentReader: ForumAttachmentReader = mockk(relaxed = true),
        stringResolver: StringResolver = mockk(relaxed = true),
    ): ForumSheetViewModel = ForumSheetViewModel(
        key = key,
        attachmentReader = attachmentReader,
        stringResolver = stringResolver,
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(activeAccount)
        },
        observeForum = observeForum,
        observeDiscussions = observeDiscussions,
        refreshDiscussions = refreshDiscussions,
        observePosts = observePosts,
        refreshPosts = refreshPosts,
        createDiscussion = createDiscussion,
        replyToPost = reply,
        editPost = edit,
        deletePostUseCase = delete,
        setFavourite = setFavourite,
        setSubscription = setSubscription,
        markRead = markRead,
        uploadAttachments = upload,
        getCourseGroups = getGroups,
        prepareEditAttachments = prepareEdit,
    )

    @Test
    fun `first page success with full page sets hasMore`() = runTest {
        val refresh = mockk<RefreshDiscussionsUseCase> {
            coEvery { this@mockk.invoke(accountId, forumId, 0, 25) } returns 25
        }
        val vm = build(refreshDiscussions = refresh)
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Idle)
        assertThat(vm.pageState.value.loadedPages).isEqualTo(1)
        assertThat(vm.pageState.value.hasMore).isTrue()
    }

    @Test
    fun `first page success with short page clears hasMore`() = runTest {
        val refresh = mockk<RefreshDiscussionsUseCase> {
            coEvery { this@mockk.invoke(accountId, forumId, 0, 25) } returns 5
        }
        val vm = build(refreshDiscussions = refresh)
        assertThat(vm.pageState.value.hasMore).isFalse()
    }

    @Test
    fun `first page failure sets Failed and emits a Failed event`() = runTest {
        val cause = IOException("offline")
        val refresh = mockk<RefreshDiscussionsUseCase> {
            coEvery { this@mockk.invoke(any(), any(), any(), any()) } throws cause
        }
        val vm = build(refreshDiscussions = refresh)
        vm.eventFlow.test {
            val event = awaitItem()
            assertThat(event).isInstanceOf(ForumSheetEvent.Failed::class.java)
            assertThat((event as ForumSheetEvent.Failed).cause).isEqualTo(cause)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(vm.syncStatus.value).isEqualTo(SyncStatus.Failed(cause))
    }

    @Test
    fun `loadMore is a no-op before the first page loads`() = runTest {
        val refresh = mockk<RefreshDiscussionsUseCase> {
            coEvery { this@mockk.invoke(any(), any(), any(), any()) } throws IOException("x")
        }
        val vm = build(refreshDiscussions = refresh)
        vm.loadMore()
        coVerify(exactly = 1) { refresh.invoke(any(), any(), any(), any()) }
    }

    @Test
    fun `openThread opens the discussion, marks it read and refreshes its posts`() = runTest {
        val markRead = mockk<MarkDiscussionReadUseCase>(relaxed = true)
        val refreshPosts = mockk<RefreshPostsUseCase>(relaxed = true)
        val vm = build(markRead = markRead, refreshPosts = refreshPosts)
        vm.openThread(DiscussionId(5))
        assertThat(vm.openDiscussionId.value).isEqualTo(DiscussionId(5))
        coVerify { markRead.invoke(accountId, DiscussionId(5)) }
        coVerify { refreshPosts.invoke(accountId, DiscussionId(5)) }
    }

    @Test
    fun `closeThread clears the open discussion`() = runTest {
        val vm = build()
        vm.openThread(DiscussionId(5))
        vm.closeThread()
        assertThat(vm.openDiscussionId.value).isNull()
        assertThat(vm.threadSyncStatus.value).isEqualTo(SyncStatus.Idle)
    }

    @Test
    fun `toggleFavourite flips the discussion flag`() = runTest {
        val setFavourite = mockk<SetDiscussionFavouriteUseCase>(relaxed = true)
        val vm = build(setFavourite = setFavourite)
        vm.toggleFavourite(discussion(7, favourite = false))
        coVerify { setFavourite.invoke(accountId, forumId, DiscussionId(7), true) }
    }

    @Test
    fun `setSubscribed emits an Info confirmation on success`() = runTest {
        val vm = build()
        vm.eventFlow.test {
            vm.setSubscribed(DiscussionId(7), subscribed = true)
            val event = awaitItem()
            assertThat(event).isInstanceOf(ForumSheetEvent.Info::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deletePost is a no-op when no thread is open`() = runTest {
        val delete = mockk<DeletePostUseCase>(relaxed = true)
        val vm = build(delete = delete)
        vm.deletePost(post(1))
        coVerify(exactly = 0) { delete.invoke(any(), any(), any()) }
    }

    @Test
    fun `startNewDiscussion sets the new-discussion composer target`() = runTest {
        val vm = build(observeForum = mockk {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(forum(canAttach = true)))
        })
        vm.forum.test {
            while (awaitItem() !is Loadable.Loaded) { /* drain loading emissions */
            }
            vm.startNewDiscussion()
            val target = vm.composerTarget.value
            assertThat(target).isInstanceOf(ComposerTarget.NewDiscussion::class.java)
            assertThat((target as ComposerTarget.NewDiscussion).canAttach).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `startReply prefixes Re to the parent subject`() = runTest {
        val vm = build()
        vm.openThread(DiscussionId(5))
        vm.startReply(post(2, subject = "Domanda"))
        val target = vm.composerTarget.value
        assertThat(target).isInstanceOf(ComposerTarget.Reply::class.java)
        assertThat((target as ComposerTarget.Reply).replySubject).isEqualTo("Re: Domanda")
    }

    @Test
    fun `startReply keeps an existing Re prefix`() = runTest {
        val vm = build()
        vm.openThread(DiscussionId(5))
        vm.startReply(post(2, subject = "Re: Domanda"))
        assertThat((vm.composerTarget.value as ComposerTarget.Reply).replySubject)
            .isEqualTo("Re: Domanda")
    }

    @Test
    fun `cancelComposer clears the composer state`() = runTest {
        val vm = build()
        vm.startNewDiscussion()
        vm.cancelComposer()
        assertThat(vm.composerTarget.value).isNull()
        assertThat(vm.pendingAttachments.value).isEmpty()
        assertThat(vm.selectedGroupId.value).isNull()
    }

    @Test
    fun `submitComposer ignores a blank message`() = runTest {
        val create = mockk<CreateDiscussionUseCase>(relaxed = true)
        val vm = build(createDiscussion = create)
        vm.startNewDiscussion()
        vm.submitComposer(subject = "Title", message = "   ")
        coVerify(exactly = 0) { create.invoke(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `submitComposer ignores a new discussion with a blank subject`() = runTest {
        val create = mockk<CreateDiscussionUseCase>(relaxed = true)
        val vm = build(createDiscussion = create)
        vm.startNewDiscussion()
        vm.submitComposer(subject = "  ", message = "Body")
        coVerify(exactly = 0) { create.invoke(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `submitComposer creates a discussion and opens its new thread`() = runTest {
        val create = mockk<CreateDiscussionUseCase> {
            coEvery { this@mockk.invoke(accountId, forumId, "Title", "Body", any(), any()) } returns
                DiscussionId(99)
        }
        val markRead = mockk<MarkDiscussionReadUseCase>(relaxed = true)
        val vm = build(createDiscussion = create, markRead = markRead)
        vm.startNewDiscussion()
        vm.submitComposer(subject = "Title", message = "Body")
        assertThat(vm.composerTarget.value).isNull()
        assertThat(vm.openDiscussionId.value).isEqualTo(DiscussionId(99))
        coVerify { markRead.invoke(accountId, DiscussionId(99)) }
    }

    @Test
    fun `initialDiscussionId opens the thread at construction`() = runTest {
        val markRead = mockk<MarkDiscussionReadUseCase>(relaxed = true)
        val vm = build(
            key = SheetRoute.Forum(
                forumId = forumId.value,
                courseId = courseId.value,
                initialDiscussionId = 42,
            ),
            markRead = markRead,
        )
        assertThat(vm.openDiscussionId.value).isEqualTo(DiscussionId(42))
    }
}
