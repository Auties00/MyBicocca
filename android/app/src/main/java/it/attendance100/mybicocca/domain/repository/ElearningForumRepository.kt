package it.attendance100.mybicocca.domain.repository

import it.attendance100.mybicocca.core.state.Loadable
import it.attendance100.mybicocca.domain.model.account.AccountId
import it.attendance100.mybicocca.domain.model.elearning.course.CourseId
import it.attendance100.mybicocca.domain.model.elearning.forum.Discussion
import it.attendance100.mybicocca.domain.model.elearning.forum.DiscussionId
import it.attendance100.mybicocca.domain.model.elearning.forum.Forum
import it.attendance100.mybicocca.domain.model.elearning.forum.ForumId
import it.attendance100.mybicocca.domain.model.elearning.forum.Post
import it.attendance100.mybicocca.domain.model.elearning.forum.PostId
import kotlinx.coroutines.flow.Flow

interface ElearningForumRepository {
    fun observeForCourse(accountId: AccountId, courseId: CourseId): Flow<Loadable<List<Forum>>>
    fun observe(accountId: AccountId, forumId: ForumId): Flow<Loadable<Forum>>
    fun observeDiscussions(accountId: AccountId, forumId: ForumId): Flow<Loadable<List<Discussion>>>
    fun observePosts(accountId: AccountId, discussionId: DiscussionId): Flow<Loadable<List<Post>>>

    suspend fun refreshForumsForCourse(accountId: AccountId, courseId: CourseId, force: Boolean = false)
    // Returns how many discussions the page contained, so callers can tell whether more pages exist.
    suspend fun refreshDiscussions(accountId: AccountId, forumId: ForumId, page: Int = 0, perPage: Int = 25): Int
    suspend fun refreshPosts(accountId: AccountId, discussionId: DiscussionId)
    suspend fun createDiscussion(
        accountId: AccountId,
        forumId: ForumId,
        subject: String,
        message: String,
        groupId: Int? = null,
    ): DiscussionId
    suspend fun reply(
        accountId: AccountId,
        parentPostId: PostId,
        subject: String,
        message: String,
    ): PostId

    suspend fun clearForAccount(accountId: AccountId)
}
