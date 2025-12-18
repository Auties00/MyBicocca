package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatLatestMessages200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatLatestMessagesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatUsers200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatUsersRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetChatsByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetSessionMessages200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetSessionMessagesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetSessions200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatGetSessionsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatLoginUser200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatLoginUserRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatSendChatMessage200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatSendChatMessageRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatViewChatRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModChatViewSessionsRequest

interface ModChatApi {
    /**
     * POST mod_chat_get_chat_latest_messages
     * Get the latest messages from the given chat session.
     * Get the latest messages from the given chat session.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatLatestMessagesRequest 
     * @return [Call]<[ElearningModChatGetChatLatestMessages200Response]>
     */
    @POST("mod_chat_get_chat_latest_messages")
    fun modChatGetChatLatestMessages(@Body elearningModChatGetChatLatestMessagesRequest: ElearningModChatGetChatLatestMessagesRequest): Call<ElearningModChatGetChatLatestMessages200Response>

    /**
     * POST mod_chat_get_chat_users
     * Get the list of users in the given chat session.
     * Get the list of users in the given chat session.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatUsersRequest 
     * @return [Call]<[ElearningModChatGetChatUsers200Response]>
     */
    @POST("mod_chat_get_chat_users")
    fun modChatGetChatUsers(@Body elearningModChatGetChatUsersRequest: ElearningModChatGetChatUsersRequest): Call<ElearningModChatGetChatUsers200Response>

    /**
     * POST mod_chat_get_chats_by_courses
     * Returns a list of chat instances in a provided set of courses,                             if no courses are provided then all the chat instances the user has access to will be returned.
     * Returns a list of chat instances in a provided set of courses,                             if no courses are provided then all the chat instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetChatsByCoursesRequest 
     * @return [Call]<[ElearningModChatGetChatsByCourses200Response]>
     */
    @POST("mod_chat_get_chats_by_courses")
    fun modChatGetChatsByCourses(@Body elearningModChatGetChatsByCoursesRequest: ElearningModChatGetChatsByCoursesRequest): Call<ElearningModChatGetChatsByCourses200Response>

    /**
     * POST mod_chat_get_session_messages
     * Retrieves messages of the given chat session.
     * Retrieves messages of the given chat session.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetSessionMessagesRequest 
     * @return [Call]<[ElearningModChatGetSessionMessages200Response]>
     */
    @POST("mod_chat_get_session_messages")
    fun modChatGetSessionMessages(@Body elearningModChatGetSessionMessagesRequest: ElearningModChatGetSessionMessagesRequest): Call<ElearningModChatGetSessionMessages200Response>

    /**
     * POST mod_chat_get_sessions
     * Retrieves chat sessions for a given chat.
     * Retrieves chat sessions for a given chat.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatGetSessionsRequest 
     * @return [Call]<[ElearningModChatGetSessions200Response]>
     */
    @POST("mod_chat_get_sessions")
    fun modChatGetSessions(@Body elearningModChatGetSessionsRequest: ElearningModChatGetSessionsRequest): Call<ElearningModChatGetSessions200Response>

    /**
     * POST mod_chat_login_user
     * Log a user into a chat room in the given chat.
     * Log a user into a chat room in the given chat.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatLoginUserRequest 
     * @return [Call]<[ElearningModChatLoginUser200Response]>
     */
    @POST("mod_chat_login_user")
    fun modChatLoginUser(@Body elearningModChatLoginUserRequest: ElearningModChatLoginUserRequest): Call<ElearningModChatLoginUser200Response>

    /**
     * POST mod_chat_send_chat_message
     * Send a message on the given chat session.
     * Send a message on the given chat session.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatSendChatMessageRequest 
     * @return [Call]<[ElearningModChatSendChatMessage200Response]>
     */
    @POST("mod_chat_send_chat_message")
    fun modChatSendChatMessage(@Body elearningModChatSendChatMessageRequest: ElearningModChatSendChatMessageRequest): Call<ElearningModChatSendChatMessage200Response>

    /**
     * POST mod_chat_view_chat
     * Trigger the course module viewed event and update the module completion status.
     * Trigger the course module viewed event and update the module completion status.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatViewChatRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_chat_view_chat")
    fun modChatViewChat(@Body elearningModChatViewChatRequest: ElearningModChatViewChatRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

    /**
     * POST mod_chat_view_sessions
     * Trigger the chat session viewed event.
     * Trigger the chat session viewed event.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModChatViewSessionsRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_chat_view_sessions")
    fun modChatViewSessions(@Body elearningModChatViewSessionsRequest: ElearningModChatViewSessionsRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
