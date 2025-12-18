package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumAddDiscussion200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumAddDiscussionPost200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumAddDiscussionPostRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumAddDiscussionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumCanAddDiscussion200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumCanAddDiscussionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumDeletePost200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumDeletePostRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetDiscussionPost200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetDiscussionPostRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetDiscussionPosts200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetDiscussionPostsByUserid200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetDiscussionPostsByUseridRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetDiscussionPostsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetForumAccessInformation200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetForumAccessInformationRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetForumDiscussions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetForumDiscussionsPaginated200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetForumDiscussionsPaginatedRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetForumDiscussionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumGetForumsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumPrepareDraftAreaForPost200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumPrepareDraftAreaForPostRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumSetLockState200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumSetLockStateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumSetPinState200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumSetPinStateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumSetSubscriptionState200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumSetSubscriptionStateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumToggleFavouriteStateRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumUpdateDiscussionPost200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumUpdateDiscussionPostRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumViewForumDiscussionRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModForumViewForumRequest

interface ModForumApi {
    /**
     * POST mod_forum_add_discussion
     * Add a new discussion into an existing forum.
     * Add a new discussion into an existing forum.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumAddDiscussionRequest 
     * @return [Call]<[ElearningModForumAddDiscussion200Response]>
     */
    @POST("mod_forum_add_discussion")
    fun modForumAddDiscussion(@Body elearningModForumAddDiscussionRequest: ElearningModForumAddDiscussionRequest): Call<ElearningModForumAddDiscussion200Response>

    /**
     * POST mod_forum_add_discussion_post
     * Create new posts into an existing discussion.
     * Create new posts into an existing discussion.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumAddDiscussionPostRequest 
     * @return [Call]<[ElearningModForumAddDiscussionPost200Response]>
     */
    @POST("mod_forum_add_discussion_post")
    fun modForumAddDiscussionPost(@Body elearningModForumAddDiscussionPostRequest: ElearningModForumAddDiscussionPostRequest): Call<ElearningModForumAddDiscussionPost200Response>

    /**
     * POST mod_forum_can_add_discussion
     * Check if the current user can add discussions in the given forum (and optionally for the given group).
     * Check if the current user can add discussions in the given forum (and optionally for the given group).
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumCanAddDiscussionRequest 
     * @return [Call]<[ElearningModForumCanAddDiscussion200Response]>
     */
    @POST("mod_forum_can_add_discussion")
    fun modForumCanAddDiscussion(@Body elearningModForumCanAddDiscussionRequest: ElearningModForumCanAddDiscussionRequest): Call<ElearningModForumCanAddDiscussion200Response>

    /**
     * POST mod_forum_delete_post
     * Deletes a post or a discussion completely when the post is the discussion topic.
     * Deletes a post or a discussion completely when the post is the discussion topic.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumDeletePostRequest 
     * @return [Call]<[ElearningModForumDeletePost200Response]>
     */
    @POST("mod_forum_delete_post")
    fun modForumDeletePost(@Body elearningModForumDeletePostRequest: ElearningModForumDeletePostRequest): Call<ElearningModForumDeletePost200Response>

    /**
     * POST mod_forum_get_discussion_post
     * Get a particular discussion post.
     * Get a particular discussion post.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumGetDiscussionPostRequest 
     * @return [Call]<[ElearningModForumGetDiscussionPost200Response]>
     */
    @POST("mod_forum_get_discussion_post")
    fun modForumGetDiscussionPost(@Body elearningModForumGetDiscussionPostRequest: ElearningModForumGetDiscussionPostRequest): Call<ElearningModForumGetDiscussionPost200Response>

    /**
     * POST mod_forum_get_discussion_posts
     * Returns a list of forum posts for a discussion.
     * Returns a list of forum posts for a discussion.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumGetDiscussionPostsRequest 
     * @return [Call]<[ElearningModForumGetDiscussionPosts200Response]>
     */
    @POST("mod_forum_get_discussion_posts")
    fun modForumGetDiscussionPosts(@Body elearningModForumGetDiscussionPostsRequest: ElearningModForumGetDiscussionPostsRequest): Call<ElearningModForumGetDiscussionPosts200Response>

    /**
     * POST mod_forum_get_discussion_posts_by_userid
     * Returns a list of forum posts for a discussion for a user.
     * Returns a list of forum posts for a discussion for a user.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumGetDiscussionPostsByUseridRequest 
     * @return [Call]<[ElearningModForumGetDiscussionPostsByUserid200Response]>
     */
    @POST("mod_forum_get_discussion_posts_by_userid")
    fun modForumGetDiscussionPostsByUserid(@Body elearningModForumGetDiscussionPostsByUseridRequest: ElearningModForumGetDiscussionPostsByUseridRequest): Call<ElearningModForumGetDiscussionPostsByUserid200Response>

    /**
     * POST mod_forum_get_forum_access_information
     * Return capabilities information for a given forum.
     * Return capabilities information for a given forum.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumGetForumAccessInformationRequest 
     * @return [Call]<[ElearningModForumGetForumAccessInformation200Response]>
     */
    @POST("mod_forum_get_forum_access_information")
    fun modForumGetForumAccessInformation(@Body elearningModForumGetForumAccessInformationRequest: ElearningModForumGetForumAccessInformationRequest): Call<ElearningModForumGetForumAccessInformation200Response>

    /**
     * POST mod_forum_get_forum_discussions
     * Returns a list of forum discussions optionally sorted and paginated.
     * Returns a list of forum discussions optionally sorted and paginated.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumGetForumDiscussionsRequest 
     * @return [Call]<[ElearningModForumGetForumDiscussions200Response]>
     */
    @POST("mod_forum_get_forum_discussions")
    fun modForumGetForumDiscussions(@Body elearningModForumGetForumDiscussionsRequest: ElearningModForumGetForumDiscussionsRequest): Call<ElearningModForumGetForumDiscussions200Response>

    /**
     * POST mod_forum_get_forum_discussions_paginated
     * ** DEPRECATED ** Please do not call this function any more.                           Returns a list of forum discussions optionally sorted and paginated.
     * ** DEPRECATED ** Please do not call this function any more.                           Returns a list of forum discussions optionally sorted and paginated.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumGetForumDiscussionsPaginatedRequest 
     * @return [Call]<[ElearningModForumGetForumDiscussionsPaginated200Response]>
     */
    @POST("mod_forum_get_forum_discussions_paginated")
    fun modForumGetForumDiscussionsPaginated(@Body elearningModForumGetForumDiscussionsPaginatedRequest: ElearningModForumGetForumDiscussionsPaginatedRequest): Call<ElearningModForumGetForumDiscussionsPaginated200Response>

    /**
     * POST mod_forum_get_forums_by_courses
     * Returns a list of forum instances in a provided set of courses, if             no courses are provided then all the forum instances the user has access to will be             returned.
     * Returns a list of forum instances in a provided set of courses, if             no courses are provided then all the forum instances the user has access to will be             returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumGetForumsByCoursesRequest 
     * @return [Call]<[kotlin.Any]>
     */
    @POST("mod_forum_get_forums_by_courses")
    fun modForumGetForumsByCourses(@Body elearningModForumGetForumsByCoursesRequest: ElearningModForumGetForumsByCoursesRequest): Call<kotlin.Any>

    /**
     * POST mod_forum_prepare_draft_area_for_post
     * Prepares a draft area for editing a post.
     * Prepares a draft area for editing a post.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumPrepareDraftAreaForPostRequest 
     * @return [Call]<[ElearningModForumPrepareDraftAreaForPost200Response]>
     */
    @POST("mod_forum_prepare_draft_area_for_post")
    fun modForumPrepareDraftAreaForPost(@Body elearningModForumPrepareDraftAreaForPostRequest: ElearningModForumPrepareDraftAreaForPostRequest): Call<ElearningModForumPrepareDraftAreaForPost200Response>

    /**
     * POST mod_forum_set_lock_state
     * Set the lock state for the discussion
     * Set the lock state for the discussion
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumSetLockStateRequest 
     * @return [Call]<[ElearningModForumSetLockState200Response]>
     */
    @POST("mod_forum_set_lock_state")
    fun modForumSetLockState(@Body elearningModForumSetLockStateRequest: ElearningModForumSetLockStateRequest): Call<ElearningModForumSetLockState200Response>

    /**
     * POST mod_forum_set_pin_state
     * Set the pin state
     * Set the pin state
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumSetPinStateRequest 
     * @return [Call]<[ElearningModForumSetPinState200Response]>
     */
    @POST("mod_forum_set_pin_state")
    fun modForumSetPinState(@Body elearningModForumSetPinStateRequest: ElearningModForumSetPinStateRequest): Call<ElearningModForumSetPinState200Response>

    /**
     * POST mod_forum_set_subscription_state
     * Set the subscription state
     * Set the subscription state
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumSetSubscriptionStateRequest 
     * @return [Call]<[ElearningModForumSetSubscriptionState200Response]>
     */
    @POST("mod_forum_set_subscription_state")
    fun modForumSetSubscriptionState(@Body elearningModForumSetSubscriptionStateRequest: ElearningModForumSetSubscriptionStateRequest): Call<ElearningModForumSetSubscriptionState200Response>

    /**
     * POST mod_forum_toggle_favourite_state
     * Toggle the favourite state
     * Toggle the favourite state
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumToggleFavouriteStateRequest 
     * @return [Call]<[ElearningModForumSetSubscriptionState200Response]>
     */
    @POST("mod_forum_toggle_favourite_state")
    fun modForumToggleFavouriteState(@Body elearningModForumToggleFavouriteStateRequest: ElearningModForumToggleFavouriteStateRequest): Call<ElearningModForumSetSubscriptionState200Response>

    /**
     * POST mod_forum_update_discussion_post
     * Updates a post or a discussion topic post.
     * Updates a post or a discussion topic post.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumUpdateDiscussionPostRequest 
     * @return [Call]<[ElearningModForumUpdateDiscussionPost200Response]>
     */
    @POST("mod_forum_update_discussion_post")
    fun modForumUpdateDiscussionPost(@Body elearningModForumUpdateDiscussionPostRequest: ElearningModForumUpdateDiscussionPostRequest): Call<ElearningModForumUpdateDiscussionPost200Response>

    /**
     * POST mod_forum_view_forum
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumViewForumRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_forum_view_forum")
    fun modForumViewForum(@Body elearningModForumViewForumRequest: ElearningModForumViewForumRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_forum_view_forum_discussion
     * Trigger the forum discussion viewed event.
     * Trigger the forum discussion viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModForumViewForumDiscussionRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_forum_view_forum_discussion")
    fun modForumViewForumDiscussion(@Body elearningModForumViewForumDiscussionRequest: ElearningModForumViewForumDiscussionRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
