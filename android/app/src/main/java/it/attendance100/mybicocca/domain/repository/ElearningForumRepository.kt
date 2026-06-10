package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumAttachmentUpload
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumGroup
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the forum slice of the e-learning cache, backing the course detail's forum
 * section and the forum modal sheet. The `observe*` methods are hot Room flows over cached data;
 * `refresh*` and the mutations hit the Moodle mod_forum web services, write back to the cache,
 * and throw on failure for the caller to translate into sync status or an outcome.
 */
interface ElearningForumRepository {
    /** Streams a course's cached forums, sorted by name. */
    fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Forum>>>

    /** Streams one cached forum, not yet loaded while it is absent from the cache. */
    fun observe(accountId: AccountId, forumId: ForumId): Flow<Loadable<Forum>>

    /** Streams a forum's cached discussions, pinned threads first, then by latest activity. */
    fun observeDiscussions(accountId: AccountId, forumId: ForumId): Flow<Loadable<List<Discussion>>>

    /** Streams a discussion's cached posts in chronological order. */
    fun observePosts(accountId: AccountId, discussionId: DiscussionId): Flow<Loadable<List<Post>>>

    /** Replaces a course's cached forums from the server; skipped while still fresh unless [force]. */
    suspend fun refreshForumsForCourse(accountId: AccountId, courseId: CourseId, force: Boolean = false)

    /**
     * Fetches one page of a forum's discussions; page 0 replaces the forum's cached list, later
     * pages merge into it. Returns how many discussions the page contained, so callers can tell
     * whether more pages exist.
     */
    suspend fun refreshDiscussions(accountId: AccountId, forumId: ForumId, page: Int = 0, perPage: Int = 25): Int

    /** Replaces a discussion's cached post tree from the server. */
    suspend fun refreshPosts(accountId: AccountId, discussionId: DiscussionId)

    /**
     * Starts a new discussion and returns its id. [groupId] targets a group in group-mode
     * forums; [attachmentDraftItemId] references an uploaded draft area to attach files. The
     * cached discussion list re-syncs best-effort afterwards.
     */
    suspend fun createDiscussion(
        accountId: AccountId,
        forumId: ForumId,
        subject: String,
        message: String,
        groupId: Int? = null,
        attachmentDraftItemId: Int? = null,
    ): DiscussionId

    /**
     * Replies to a post and returns the new post's id. [attachmentDraftItemId] references an
     * uploaded draft area to attach files. The cached thread re-syncs best-effort afterwards.
     */
    suspend fun reply(
        accountId: AccountId,
        discussionId: DiscussionId,
        parentPostId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int? = null,
    ): PostId

    /**
     * Edits an existing post, then refreshes the discussion so the post tree stays consistent.
     * A non-null [attachmentDraftItemId] — from [prepareEditAttachments] plus [uploadAttachments]
     * — carries the post's existing attachments plus any newly added ones; null leaves
     * attachments untouched.
     */
    suspend fun editPost(
        accountId: AccountId,
        discussionId: DiscussionId,
        postId: PostId,
        subject: String,
        message: String,
        attachmentDraftItemId: Int? = null,
    )

    /** Deletes a post, then refreshes the discussion: Moodle may re-parent or tombstone replies. */
    suspend fun deletePost(accountId: AccountId, discussionId: DiscussionId, postId: PostId)

    /**
     * Stars or unstars a discussion: the cached flag flips optimistically before the server
     * call and reverts when the call fails.
     */
    suspend fun setFavourite(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        favourite: Boolean,
    )

    /**
     * Subscribes to or unsubscribes from a discussion. Throws on the server's business errors —
     * e.g. forced-subscription forums — which the caller surfaces as an outcome. Subscription
     * state is server-only, not part of the cached discussion data.
     */
    suspend fun setDiscussionSubscription(
        accountId: AccountId,
        forumId: ForumId,
        discussionId: DiscussionId,
        subscribed: Boolean,
    )

    /** Optimistically clears the discussion's cached unread count, then logs the view server-side best-effort. */
    suspend fun markDiscussionRead(accountId: AccountId, discussionId: DiscussionId)

    /** The user's groups in a course; empty when the forum is not group-restricted for them. */
    suspend fun getCourseGroups(accountId: AccountId, courseId: CourseId): List<ForumGroup>

    /**
     * Uploads files to a Moodle draft area and returns its id for `attachmentsid`. Pass
     * [baseDraftItemId] — e.g. from [prepareEditAttachments] — to add to an existing draft
     * rather than creating a fresh one.
     */
    suspend fun uploadAttachments(
        accountId: AccountId,
        files: List<ForumAttachmentUpload>,
        baseDraftItemId: Int? = null,
    ): Int

    /**
     * Prepares a draft area pre-populated with a post's current attachments, so an edit can add
     * to them without losing them. Returns the draft id.
     */
    suspend fun prepareEditAttachments(accountId: AccountId, postId: PostId): Int

    /** Drops every cached forum, discussion, and post row belonging to the account. */
    suspend fun clearForAccount(accountId: AccountId)
}
