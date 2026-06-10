package it.attendance100.mybicocca.data.local.elearning.forum

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import it.attendance100.mybicocca.data.local.account.MyBicoccaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Behaviour coverage for [ForumDao] against a real in-memory Room database (Robolectric).
 * Exercises the forum/discussion/post tables: forum listing by name, discussions ordered
 * pinned-first then latest-activity, posts chronological, account/course/forum/discussion
 * scoping, the optimistic favourite and mark-read updates, the splice replace transactions
 * (drop only the targeted parent's children then insert fresh), and the account-wide clear.
 * No forum table declares a foreign key, so rows insert without a parent account.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ForumDaoTest {

    private lateinit var db: MyBicoccaDatabase
    private lateinit var dao: ForumDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MyBicoccaDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.elearningForumDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `observeForCourse orders forums by name and is account and course scoped`() = runTest {
        dao.upsertForums(
            listOf(
                forum(forumId = 1, name = "Zeta", courseId = 100),
                forum(forumId = 2, name = "Alpha", courseId = 100),
                forum(forumId = 3, name = "OtherCourse", courseId = 200),
                forum(forumId = 4, name = "OtherAccount", courseId = 100, accountId = "acc-2"),
            ),
        )

        val rows = dao.observeForCourse("acc-1", courseId = 100).first()

        assertThat(rows.map { it.name }).containsExactly("Alpha", "Zeta").inOrder()
    }

    @Test
    fun `observe returns the single forum carrying its capability flags`() = runTest {
        dao.upsertForums(listOf(forum(forumId = 7).copy(
            canCreateDiscussions = true,
            canSubscribe = false,
            canAttachFiles = true,
        )))

        val stored = dao.observe("acc-1", forumId = 7).first()

        assertThat(stored).isNotNull()
        assertThat(stored!!.canCreateDiscussions).isTrue()
        assertThat(stored.canSubscribe).isFalse()
        assertThat(stored.canAttachFiles).isTrue()
    }

    @Test
    fun `observeDiscussions orders pinned first then latest activity`() = runTest {
        dao.upsertDiscussions(
            listOf(
                discussion(discussionId = 1, forumId = 7, isPinned = false, timeModifiedMs = 9_000L),
                discussion(discussionId = 2, forumId = 7, isPinned = true, timeModifiedMs = 1_000L),
                discussion(discussionId = 3, forumId = 7, isPinned = false, timeModifiedMs = 5_000L),
                discussion(discussionId = 4, forumId = 7, isPinned = true, timeModifiedMs = 8_000L),
            ),
        )

        val ordered = dao.observeDiscussions("acc-1", forumId = 7).first()

        assertThat(ordered.map { it.discussionId }).containsExactly(4, 2, 1, 3).inOrder()
    }

    @Test
    fun `observePosts orders chronologically and is discussion scoped`() = runTest {
        dao.upsertPosts(
            listOf(
                post(postId = 1, discussionId = 50, createdAtMs = 3_000L),
                post(postId = 2, discussionId = 50, createdAtMs = 1_000L),
                post(postId = 3, discussionId = 50, createdAtMs = 2_000L),
                post(postId = 4, discussionId = 99, createdAtMs = 500L),
            ),
        )

        val posts = dao.observePosts("acc-1", discussionId = 50).first()

        assertThat(posts.map { it.postId }).containsExactly(2, 3, 1).inOrder()
    }

    @Test
    fun `setDiscussionFavourite flips only the targeted discussion`() = runTest {
        dao.upsertDiscussions(
            listOf(
                discussion(discussionId = 1, forumId = 7, isFavourite = false),
                discussion(discussionId = 2, forumId = 7, isFavourite = false),
            ),
        )

        dao.setDiscussionFavourite("acc-1", discussionId = 1, favourite = true)

        val rows = dao.observeDiscussions("acc-1", forumId = 7).first().associateBy { it.discussionId }
        assertThat(rows.getValue(1).isFavourite).isTrue()
        assertThat(rows.getValue(2).isFavourite).isFalse()
    }

    @Test
    fun `clearDiscussionUnread zeroes the unread count of the targeted discussion`() = runTest {
        dao.upsertDiscussions(
            listOf(
                discussion(discussionId = 1, forumId = 7, unreadCount = 5),
                discussion(discussionId = 2, forumId = 7, unreadCount = 3),
            ),
        )

        dao.clearDiscussionUnread("acc-1", discussionId = 1)

        val rows = dao.observeDiscussions("acc-1", forumId = 7).first().associateBy { it.discussionId }
        assertThat(rows.getValue(1).unreadCount).isEqualTo(0)
        assertThat(rows.getValue(2).unreadCount).isEqualTo(3)
    }

    @Test
    fun `replaceForumsForCourse swaps only the target course forums`() = runTest {
        dao.upsertForums(
            listOf(
                forum(forumId = 1, courseId = 100),
                forum(forumId = 2, courseId = 200),
            ),
        )

        dao.replaceForumsForCourse("acc-1", courseId = 100, rows = listOf(forum(forumId = 3, courseId = 100)))

        assertThat(dao.observeForCourse("acc-1", 100).first().map { it.forumId }).containsExactly(3)
        assertThat(dao.observeForCourse("acc-1", 200).first().map { it.forumId }).containsExactly(2)
    }

    @Test
    fun `replaceDiscussionsForForum swaps only the target forum discussions`() = runTest {
        dao.upsertDiscussions(
            listOf(
                discussion(discussionId = 1, forumId = 7),
                discussion(discussionId = 2, forumId = 8),
            ),
        )

        dao.replaceDiscussionsForForum("acc-1", forumId = 7, rows = listOf(discussion(discussionId = 3, forumId = 7)))

        assertThat(dao.observeDiscussions("acc-1", 7).first().map { it.discussionId }).containsExactly(3)
        assertThat(dao.observeDiscussions("acc-1", 8).first().map { it.discussionId }).containsExactly(2)
    }

    @Test
    fun `replacePostsForDiscussion swaps only the target discussion posts`() = runTest {
        dao.upsertPosts(
            listOf(
                post(postId = 1, discussionId = 50),
                post(postId = 2, discussionId = 60),
            ),
        )

        dao.replacePostsForDiscussion("acc-1", discussionId = 50, rows = listOf(post(postId = 3, discussionId = 50)))

        assertThat(dao.observePosts("acc-1", 50).first().map { it.postId }).containsExactly(3)
        assertThat(dao.observePosts("acc-1", 60).first().map { it.postId }).containsExactly(2)
    }

    @Test
    fun `clearAllForAccount empties every forum table for the account only`() = runTest {
        dao.upsertForums(listOf(forum(forumId = 1), forum(forumId = 2, accountId = "acc-2")))
        dao.upsertDiscussions(listOf(discussion(discussionId = 1, forumId = 1), discussion(discussionId = 2, forumId = 2, accountId = "acc-2")))
        dao.upsertPosts(listOf(post(postId = 1, discussionId = 1), post(postId = 2, discussionId = 2, accountId = "acc-2")))

        dao.clearAllForAccount("acc-1")

        assertThat(dao.observeForCourse("acc-1", 100).first()).isEmpty()
        assertThat(dao.observeDiscussions("acc-1", 1).first()).isEmpty()
        assertThat(dao.observePosts("acc-1", 1).first()).isEmpty()

        assertThat(dao.observeForCourse("acc-2", 100).first()).hasSize(1)
        assertThat(dao.observeDiscussions("acc-2", 2).first()).hasSize(1)
        assertThat(dao.observePosts("acc-2", 2).first()).hasSize(1)
    }

    @Test
    fun `observeDiscussions re-emits after a favourite toggle`() = runTest {
        dao.upsertDiscussions(listOf(discussion(discussionId = 1, forumId = 7, isFavourite = false)))

        dao.observeDiscussions("acc-1", forumId = 7).test {
            assertThat(awaitItem().single().isFavourite).isFalse()

            dao.setDiscussionFavourite("acc-1", discussionId = 1, favourite = true)
            assertThat(awaitItem().single().isFavourite).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun forum(
        forumId: Int,
        accountId: String = "acc-1",
        courseId: Int = 100,
        name: String = "Forum $forumId",
    ) = ForumEntity(
        accountId = accountId,
        forumId = forumId,
        courseId = courseId,
        cmId = forumId + 1_000,
        name = name,
        intro = null,
        typeRaw = "news",
        discussionCount = 0,
        postCount = 0,
        canCreateDiscussions = true,
        canSubscribe = true,
        canAttachFiles = false,
    )

    private fun discussion(
        discussionId: Int,
        forumId: Int,
        accountId: String = "acc-1",
        isPinned: Boolean = false,
        timeModifiedMs: Long? = 1_000L,
        unreadCount: Int = 0,
        isFavourite: Boolean = false,
    ) = DiscussionEntity(
        accountId = accountId,
        discussionId = discussionId,
        forumId = forumId,
        firstPostId = discussionId * 10,
        subject = "Discussion $discussionId",
        authorUserId = 42,
        authorName = "Mario Rossi",
        authorAvatarUrl = null,
        createdAtMs = 1_000L,
        timeModifiedMs = timeModifiedMs,
        isPinned = isPinned,
        isLocked = false,
        unreadCount = unreadCount,
        replyCount = 0,
        lastPostAuthorName = null,
        messagePreview = null,
        hasAttachments = false,
        canReply = true,
        isFavourite = isFavourite,
        canFavourite = true,
    )

    private fun post(
        postId: Int,
        discussionId: Int,
        accountId: String = "acc-1",
        createdAtMs: Long? = 1_000L,
    ) = PostEntity(
        accountId = accountId,
        postId = postId,
        discussionId = discussionId,
        parentId = null,
        authorUserId = 42,
        authorName = "Mario Rossi",
        authorAvatarUrl = null,
        subject = "Post $postId",
        message = "body",
        createdAtMs = createdAtMs,
        modifiedAtMs = null,
        attachmentsJson = null,
        canReply = true,
        canEdit = false,
        canDelete = false,
        isDeleted = false,
    )
}
