package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.dto.elearning.DiscussionOption
import it.attendance100.mybicocca.data.dto.elearning.ElearningAddForumDiscussionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningAddForumDiscussionResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningAddForumPostRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningAddForumPostResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetForumDiscussionPostsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetForumDiscussionPostsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetForumDiscussionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetForumDiscussionsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetForumsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetForumsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningSortOrder
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
        sortOrder: ElearningSortOrder = ElearningSortOrder.LAST_POST_DESCENDING,
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
        options: List<DiscussionOption>? = null
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
        options: List<DiscussionOption>? = null
    ): ElearningAddForumPostResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningAddForumPostRequest(postId, subject, message, options)
        )
    }
}
