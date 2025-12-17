package it.attendance100.mybicocca.data.api.elearning

import it.attendance100.mybicocca.data.remote.dto.elearning.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * # Elearning Message API
 *
 * Handles messaging, conversations, contacts, and notifications.
 *
 * ## Key Features
 *
 * - **Conversations:** List conversations, get messages, delete conversations.
 * - **Messages:** Send messages, mark as read, search messages.
 * - **Contacts:** Manage contacts, block/unblock users, handle requests.
 * - **Notifications:** Get unread counts, mark notifications as read.
 *
 * ## Usage Example
 *
 * ```kotlin
 * // Get conversations list
 * val conversations = messageApi.getConversations(
 *     CoreMessageGetConversationsPostRequest(userid = userId)
 * )
 *
 * // Send a message
 * messageApi.sendInstantMessages(
 *     CoreMessageSendInstantMessagesPostRequest(messages = listOf(...))
 * )
 * ```
 */
interface ElearningMessageApi {

    /**
     * Retrieve a list of conversations for a user.
     *
     * @param request User ID and pagination options.
     * @return List of conversations.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_get_conversations")
    suspend fun getConversations(@Body request: GetConversationsRequest): Response<ConversationsResponse>

    /**
     * Retrieve messages for a specific conversation.
     *
     * @param request User ID, conversation ID, and pagination.
     * @return List of messages.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_get_conversation_messages")
    suspend fun getConversationMessages(@Body request: GetConversationMessagesRequest): Response<ConversationMessagesResponse>

    /**
     * Retrieve the conversation ID between two users.
     *
     * @param request Other user ID and current user ID.
     * @return Conversation details (structure varies).
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_get_conversation_between_users")
    suspend fun getConversationBetweenUsers(@Body request: GetConversationBetweenUsersRequest): Response<ConversationDetail>

    /**
     * Send messages to an existing conversation.
     *
     * @param request Conversation ID and message text.
     * @return List of sent messages.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_send_messages_to_conversation")
    suspend fun sendMessagesToConversation(@Body request: SendMessagesToConversationRequest): Response<List<SentMessage>>

    /**
     * Delete conversations by their IDs.
     *
     * @param request User ID and conversation IDs.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_delete_conversations_by_id")
    suspend fun deleteConversationsById(@Body request: DeleteConversationsRequest): Response<StatusWithWarningsResponse>

    /**
     * Get unread conversation counts.
     *
     * @param request User ID.
     * @return Unread counts by type (favorites, individual, etc.).
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_get_unread_conversation_counts")
    suspend fun getUnreadConversationCounts(@Body request: GetUnreadCountsRequest): Response<UnreadCountsResponse>

    /**
     * Mark all notifications as read for a user.
     *
     * @param request User ID and timestamp.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_mark_all_notifications_as_read")
    suspend fun markAllNotificationsAsRead(@Body request: MarkAllNotificationsReadRequest): Response<Boolean>

    /**
     * Retrieve messages based on filters (read/unread, time).
     *
     * @param request User ID and filters.
     * @return Messages list.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_get_messages")
    suspend fun getMessages(@Body request: GetMessagesRequest): Response<MessagesResponse>

    /**
     * Send instant messages to users.
     *
     * @param request List of messages to send.
     * @return List of sent messages.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_send_instant_messages")
    suspend fun sendInstantMessages(@Body request: SendInstantMessagesRequest): Response<List<SentInstantMessage>>

    /**
     * Block a user.
     *
     * @param request User ID and blocked user ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_block_user")
    suspend fun blockUser(@Body request: BlockUserRequest): Response<StatusWithWarningsResponse>

    /**
     * Unblock a user.
     *
     * @param request User ID and unblocked user ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_unblock_user")
    suspend fun unblockUser(@Body request: UnblockUserRequest): Response<StatusWithWarningsResponse>

    /**
     * Get user's contacts.
     *
     * @param request User ID.
     * @return List of contacts.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_get_user_contacts")
    suspend fun getUserContacts(@Body request: GetUserContactsRequest): Response<List<UserContact>>

    /**
     * Confirm a contact request.
     *
     * @param request User ID and requested user ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_confirm_contact_request")
    suspend fun confirmContactRequest(@Body request: ConfirmContactRequest): Response<StatusWithWarningsResponse>

    /**
     * Decline a contact request.
     *
     * @param request User ID and requested user ID.
     * @return Unit.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_decline_contact_request")
    suspend fun declineContactRequest(@Body request: DeclineContactRequest): Response<StatusWithWarningsResponse>

    /**
     * Get the count of received contact requests.
     *
     * @param request User ID.
     * @return Number of requests.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_get_received_contact_requests_count")
    suspend fun getReceivedContactRequestsCount(@Body request: GetContactRequestsCountRequest): Response<Int>

    /**
     * Search for users to message.
     *
     * @param request User ID and search string.
     * @return Search results.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_message_search_users")
    suspend fun messageSearchUsers(@Body request: MessageSearchUsersRequest): Response<MessageSearchUsersResponse>

    /**
     * Mark a specific message as read.
     *
     * @param request Message ID.
     * @return Read status.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_mark_message_read")
    suspend fun markMessageRead(@Body request: MarkMessageReadRequest): Response<MarkMessageReadResponse>

    /**
     * Search for messages in the message area.
     *
     * @param request User ID and search string.
     * @return Search results.
     */
    @POST("webservice/rest/server.php?moodlewsrestformat=json&wsfunction=core_message_data_for_messagearea_search_messages")
    suspend fun searchMessages(@Body request: SearchMessagesAreaRequest): Response<SearchMessagesAreaResponse>
}