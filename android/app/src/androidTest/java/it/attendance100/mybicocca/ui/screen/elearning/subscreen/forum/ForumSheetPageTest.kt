package it.attendance100.mybicocca.ui.screen.elearning.subscreen.forum

import android.content.Context
import com.google.common.truth.Truth.assertThat
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.Account
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumType
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveDiscussionsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObserveForumUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.ObservePostsUseCase
import it.attendance100.mybicocca.domain.usecase.elearning.forum.RefreshDiscussionsUseCase
import it.attendance100.mybicocca.ui.navigation.route.SheetRoute
import it.attendance100.mybicocca.ui.theme.BicoccaTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * State and behaviour coverage for the forum sheet's discussions-list entry. A real
 * [ForumSheetViewModel] over faked use cases renders the list page from observed Room state: an
 * empty forum shows the empty marker plus the new-discussion button, a populated one shows the keyed
 * discussion rows, and tapping "Nuova discussione" opens the composer page. Anchored on
 * [ForumSheetTestTags] and wrapped in the production [BicoccaTheme].
 */
@RunWith(AndroidJUnit4::class)
class ForumSheetPageTest {

    @get:Rule
    val compose = createComposeRule()

    private val accountId = AccountId("acc-1")
    private val forumId = ForumId(10)
    private val courseId = CourseId(3)

    private val account: Account = mockk(relaxed = true) {
        every { id } returns accountId
    }

    private fun forum(): Forum = Forum(
        id = forumId,
        courseId = courseId,
        cmId = null,
        name = "Forum del corso",
        intro = null,
        type = ForumType.General,
        discussionCount = 0,
        postCount = 0,
        canCreateDiscussions = true,
        canSubscribe = true,
        canAttachFiles = false,
    )

    private fun discussion(id: Int): Discussion = Discussion(
        id = DiscussionId(id),
        forumId = forumId,
        firstPostId = PostId(id * 10),
        subject = "Thread $id",
        authorUserId = null,
        authorName = "Mario Rossi",
        authorAvatarUrl = null,
        createdAt = null,
        timeModified = null,
        isPinned = false,
        isLocked = false,
        unreadCount = 0,
        replyCount = 0,
        lastPostAuthorName = null,
        messagePreview = "preview",
        hasAttachments = false,
        canReply = true,
        isFavourite = false,
        canFavourite = true,
    )

    private fun build(
        discussions: Loadable<List<Discussion>>,
    ): ForumSheetViewModel = ForumSheetViewModel(
        key = SheetRoute.Forum(forumId = forumId.value, courseId = courseId.value),
        appContext = mockk<Context>(relaxed = true),
        observeActiveAccount = mockk {
            every { this@mockk.invoke() } returns MutableStateFlow<Account?>(account)
        },
        observeForum = mockk<ObserveForumUseCase> {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.Loaded(forum()))
        },
        observeDiscussions = mockk<ObserveDiscussionsUseCase> {
            every { this@mockk.invoke(any(), any()) } returns flowOf(discussions)
        },
        refreshDiscussions = mockk<RefreshDiscussionsUseCase> {
            coEvery { this@mockk.invoke(any(), any(), any(), any()) } returns 0
        },
        observePosts = mockk<ObservePostsUseCase> {
            every { this@mockk.invoke(any(), any()) } returns flowOf(Loadable.NotYetLoaded)
        },
        refreshPosts = mockk(relaxed = true),
        createDiscussion = mockk(relaxed = true),
        replyToPost = mockk(relaxed = true),
        editPost = mockk(relaxed = true),
        deletePostUseCase = mockk(relaxed = true),
        setFavourite = mockk(relaxed = true),
        setSubscription = mockk(relaxed = true),
        markRead = mockk(relaxed = true),
        uploadAttachments = mockk(relaxed = true),
        getCourseGroups = mockk {
            coEvery { this@mockk.invoke(any(), any()) } returns emptyList()
        },
        prepareEditAttachments = mockk(relaxed = true),
    )

    private fun setPage(viewModel: ForumSheetViewModel) {
        compose.setContent {
            BicoccaTheme(dark = false) {
                ForumSheetPage(
                    forumId = forumId.value,
                    courseId = courseId.value,
                    initialDiscussionId = null,
                    onOpenFile = { _, _, _, _ -> },
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun an_empty_forum_shows_the_empty_marker_and_the_new_discussion_action() {
        val vm = build(discussions = Loadable.Loaded(emptyList()))
        setPage(vm)

        compose.onNodeWithTag(ForumSheetTestTags.ROOT).assertIsDisplayed()
        compose.onNodeWithTag(ForumSheetTestTags.STATE_EMPTY).assertIsDisplayed()
        compose.onNodeWithTag(ForumSheetTestTags.NEW_DISCUSSION_ACTION).assertIsDisplayed()
    }

    @Test
    fun a_populated_forum_renders_the_keyed_discussion_rows() {
        val vm = build(discussions = Loadable.Loaded(listOf(discussion(1), discussion(2))))
        setPage(vm)

        compose.onNodeWithTag(ForumSheetTestTags.DISCUSSIONS_LIST).assertIsDisplayed()
        compose.onNodeWithTag(ForumSheetTestTags.discussion(1)).assertIsDisplayed()
        compose.onNodeWithTag(ForumSheetTestTags.discussion(2)).assertIsDisplayed()
    }

    @Test
    fun tapping_a_discussion_row_opens_its_thread() {
        val vm = build(discussions = Loadable.Loaded(listOf(discussion(1))))
        setPage(vm)

        compose.onNodeWithTag(ForumSheetTestTags.discussion(1)).performClick()
        compose.waitForIdle()

        assertThat(vm.openDiscussionId.value).isEqualTo(DiscussionId(1))
    }

    @Test
    fun tapping_the_new_discussion_action_shows_the_composer() {
        val vm = build(discussions = Loadable.Loaded(emptyList()))
        setPage(vm)

        compose.onNodeWithTag(ForumSheetTestTags.NEW_DISCUSSION_ACTION).performClick()
        compose.waitForIdle()

        compose.onNodeWithTag(ForumSheetTestTags.COMPOSER).assertIsDisplayed()
    }
}
