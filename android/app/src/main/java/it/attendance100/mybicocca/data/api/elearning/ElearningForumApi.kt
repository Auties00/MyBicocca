package it.attendance100.mybicocca.data.api.elearning

import it.attendance100.mybicocca.data.remote.dto.elearning.AddDiscussionPostRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.AddDiscussionPostResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.AddDiscussionRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.AddDiscussionResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.CanAddDiscussionRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.CanAddDiscussionResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.DeletePostRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.DiscussionPostsResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.Forum
import it.attendance100.mybicocca.data.remote.dto.elearning.ForumAccessInfoResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.ForumDiscussionPostsResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.ForumDiscussionsResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.GetDiscussionPostsRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetForumAccessInfoRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetForumDiscussionPostsRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetForumDiscussionsRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.GetForumsByCoursesRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.PrepareDraftAreaRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.PrepareDraftAreaResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.SetLockStateRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.SetLockStateResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.StatusWithWarningsResponse
import it.attendance100.mybicocca.data.remote.dto.elearning.UpdateDiscussionPostRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.ViewForumDiscussionRequest
import it.attendance100.mybicocca.data.remote.dto.elearning.ViewForumRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * # Elearning Forum API
 *
 * Handles forum discussions, posts, and access.
 *
 * ## Key Features
 *
 * - **Forums:** Get forums by course.
 * - **Discussions:** List, add, update, and view discussions.
 * - **Posts:** Get, add, update, and delete posts.
 * - **Access:** Check capabilities and access information.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get discussions
 * val discussions = forumApi.getForumDiscussions(
 *     GetForumDiscussionsRequest(forumId = forumId)
 * )
 *
 * // Add a post
 * forumApi.addDiscussionPost(
 *     AddDiscussionPostRequest(postId = parentPostId, message = "Reply")
 * )
 * ```
 */
interface ElearningForumApi {

    /**
     * Returns a list of forums for the given courses.
     *
     * @param request Course IDs.
     * @return List of forums.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_get_forums_by_courses")
    suspend fun getForumsByCourses(@Body request: GetForumsByCoursesRequest): Response<List<Forum>>

    /**
     * Returns a list of forum discussions.
     *
     * @param request Forum ID and pagination.
     * @return List of discussions.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_get_forum_discussions")
    suspend fun getForumDiscussions(@Body request: GetForumDiscussionsRequest): Response<ForumDiscussionsResponse>

    /**
     * Returns a list of forum discussion posts.
     *
     * @param discussionId Discussion ID.
     * @return List of posts.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_get_forum_discussion_posts")
    suspend fun getForumDiscussionPosts(@Body request: GetForumDiscussionPostsRequest): Response<ForumDiscussionPostsResponse>

    /**
     * Returns a list of discussion posts.
     *
     * @param discussionId Discussion ID.
     * @return List of posts.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_get_discussion_posts")
    suspend fun getDiscussionPosts(@Body request: GetDiscussionPostsRequest): Response<DiscussionPostsResponse>

    /**
     * Create a new post.
     *
     * @param request Post ID, subject, and message.
     * @return The created post.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_add_discussion_post")
    suspend fun addDiscussionPost(@Body request: AddDiscussionPostRequest): Response<AddDiscussionPostResponse>

    /**
     * Add a new discussion.
     *
     * @param request Forum ID, subject, and message.
     * @return The created discussion.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_add_discussion")
    suspend fun addDiscussion(@Body request: AddDiscussionRequest): Response<AddDiscussionResponse>

    /**
     * Check if the user can add a discussion.
     *
     * @param request Forum ID.
     * @return Boolean status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_can_add_discussion")
    suspend fun canAddDiscussion(@Body request: CanAddDiscussionRequest): Response<CanAddDiscussionResponse>

    /**
     * Update a post.
     *
     * @param request Post ID, subject, and message.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_update_discussion_post")
    suspend fun updateDiscussionPost(@Body request: UpdateDiscussionPostRequest): Response<StatusWithWarningsResponse>

    /**
     * Delete a post.
     *
     * @param request Post ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_delete_post")
    suspend fun deletePost(@Body request: DeletePostRequest): Response<StatusWithWarningsResponse>

    /**
     * Set the lock state for the discussion.
     *
     * @param request Discussion ID and state.
     * @return Locked state details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_set_lock_state")
    suspend fun setLockState(@Body request: SetLockStateRequest): Response<SetLockStateResponse>

    /**
     * Log that the forum was viewed.
     *
     * @param request Forum ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_view_forum")
    suspend fun viewForum(@Body request: ViewForumRequest): Response<StatusWithWarningsResponse>

    /**
     * Log that the discussion was viewed.
     *
     * @param request Discussion ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_view_forum_discussion")
    suspend fun viewForumDiscussion(@Body request: ViewForumDiscussionRequest): Response<StatusWithWarningsResponse>

    /**
     * Return capabilities for a given forum.
     *
     * @param request Forum ID.
     * @return Capabilities.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_get_forum_access_information")
    suspend fun getForumAccessInformation(@Body request: GetForumAccessInfoRequest): Response<ForumAccessInfoResponse>

    /**
     * Prepare draft area for a post.
     *
     * @param request Post ID or forum/discussion details.
     * @return Draft area details.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=mod_forum_prepare_draft_area_for_post")
    suspend fun prepareDraftAreaForPost(@Body request: PrepareDraftAreaRequest): Response<PrepareDraftAreaResponse>
}
