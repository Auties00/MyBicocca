package it.attendance100.mybicocca.data.remote.elearning.api

import io.ktor.client.*
import it.attendance100.mybicocca.data.remote.elearning.dto.*
import kotlinx.serialization.json.Json

/**
 * API for forum-related operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningForumApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets forums for courses.
     */
    suspend fun getForums(wsToken: String, courseIds: List<Int>): ElearningGetForumsResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetForumsRequest(courseIds))
    }

    /**
     * Gets forums for a single course.
     */
    suspend fun getForumsForCourse(wsToken: String, courseId: Int): ElearningGetForumsResponse {
        return getForums(wsToken, listOf(courseId))
    }

    /**
     * Gets discussions for a forum.
     */
    suspend fun getForumDiscussions(
        wsToken: String,
        forumId: Int,
        sortOrder: ElearningDiscussionSortOrder = ElearningDiscussionSortOrder.LAST_POST_DESCENDING,
        page: Int = 0,
        perPage: Int = 10,
        groupId: Int = 0
    ): ElearningGetForumDiscussionsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetForumDiscussionsRequest(forumId, sortOrder, page, perPage, groupId)
        )
    }

    /**
     * Gets posts for a discussion.
     */
    suspend fun getDiscussionPosts(
        wsToken: String,
        discussionId: Int,
        includeInlineAttachments: Boolean = false
    ): ElearningGetForumDiscussionPostsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetForumDiscussionPostsRequest(discussionId, includeInlineAttachments)
        )
    }

    /**
     * Creates a new discussion in a forum.
     */
    suspend fun addDiscussion(
        wsToken: String,
        forumId: Int,
        subject: String,
        message: String,
        groupId: Int? = null,
        options: List<ElearningDiscussionOption>? = null
    ): ElearningAddForumDiscussionResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningAddForumDiscussionRequest(forumId, subject, message, groupId, options)
        )
    }

    /**
     * Replies to a post in a discussion.
     */
    suspend fun addPost(
        wsToken: String,
        postId: Int,
        subject: String,
        message: String,
        options: List<ElearningDiscussionOption>? = null
    ): ElearningAddForumPostResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningAddForumPostRequest(postId, subject, message, options)
        )
    }

    /**
     * Edits an existing post. Omitted fields are left unchanged; omit attachment options to
     * keep the post's current attachments.
     */
    suspend fun updatePost(
        wsToken: String,
        postId: Int,
        subject: String? = null,
        message: String? = null,
        options: List<ElearningDiscussionOption>? = null
    ): ElearningForumStatusResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningUpdateForumPostRequest(postId, subject, message, options)
        )
    }

    /**
     * Deletes a post the user owns (or otherwise has permission to delete).
     */
    suspend fun deletePost(wsToken: String, postId: Int): ElearningForumStatusResponse {
        return executeAuthenticatedRequest(wsToken, ElearningDeleteForumPostRequest(postId))
    }

    /**
     * Fetches a single post by id, e.g. to refresh it after an edit.
     */
    suspend fun getDiscussionPost(wsToken: String, postId: Int): ElearningGetForumDiscussionPostResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetForumDiscussionPostRequest(postId))
    }

    /**
     * Checks whether the user can start a new discussion in the forum (optionally for a group).
     */
    suspend fun canAddDiscussion(
        wsToken: String,
        forumId: Int,
        groupId: Int? = null
    ): ElearningCanAddDiscussionResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningCanAddDiscussionRequest(forumId, groupId)
        )
    }

    /**
     * Logs that the user viewed a forum (server-side read tracking).
     */
    suspend fun markForumViewed(wsToken: String, forumId: Int): ElearningForumStatusResponse {
        return executeAuthenticatedRequest(wsToken, ElearningViewForumRequest(forumId))
    }

    /**
     * Logs that the user viewed a discussion, marking its posts read.
     */
    suspend fun markDiscussionViewed(wsToken: String, discussionId: Int): ElearningForumStatusResponse {
        return executeAuthenticatedRequest(wsToken, ElearningViewForumDiscussionRequest(discussionId))
    }

    /**
     * Subscribes to or unsubscribes from a single discussion.
     */
    suspend fun setDiscussionSubscription(
        wsToken: String,
        forumId: Int,
        discussionId: Int,
        subscribed: Boolean
    ): ElearningForumDiscussionStateResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningSetDiscussionSubscriptionRequest(forumId, discussionId, subscribed)
        )
    }

    /**
     * Stars or unstars (favourites) a discussion.
     */
    suspend fun toggleFavourite(
        wsToken: String,
        discussionId: Int,
        starred: Boolean
    ): ElearningForumDiscussionStateResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningToggleForumFavouriteRequest(discussionId, starred)
        )
    }

    /**
     * Prepares a draft file area seeded with an existing post's attachments, so attachments can
     * be added/removed before [updatePost]. The returned draft id is also accepted by
     * [it.attendance100.mybicocca.data.remote.elearning.api.ElearningFileApi.uploadToDraftArea].
     */
    suspend fun prepareDraftAreaForPost(
        wsToken: String,
        postId: Int,
        draftItemId: Int? = null
    ): ElearningPrepareDraftAreaResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningPrepareDraftAreaForPostRequest(postId = postId, draftItemId = draftItemId)
        )
    }

    /**
     * Fetches the groups the user belongs to in a course, to choose a target group when posting
     * to a group-mode forum. [userId] defaults to `0`, which resolves to the authenticated user.
     */
    suspend fun getCourseUserGroups(
        wsToken: String,
        courseId: Int,
        userId: Int = 0
    ): ElearningGetCourseUserGroupsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetCourseUserGroupsRequest(courseId, userId)
        )
    }
}
