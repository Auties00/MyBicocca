package it.attendance100.mybicocca.data.api.elearning

import io.ktor.client.HttpClient
import it.attendance100.mybicocca.data.dto.elearning.ConversationType
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockUserRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningBlockUserResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningDeleteConversationsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningDeleteConversationsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetConversationMessagesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetConversationMessagesResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetConversationsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetConversationsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetUnreadConversationCountsRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningGetUnreadConversationCountsResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningMarkAllConversationMessagesAsReadRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningMarkAsReadResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningSendInstantMessagesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningSendInstantMessagesResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningUnblockUserRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningUnblockUserResponse
import it.attendance100.mybicocca.data.dto.elearning.InstantMessage
import kotlinx.serialization.json.Json

/**
 * API for messaging operations.
 *
 * @param client The shared [HttpClient] instance
 * @param json The shared [Json] instance
 */
class ElearningMessageApi(
    client: HttpClient,
    json: Json
) : ElearningAbstractApi(client, json) {

    /**
     * Gets a list of conversations for a user.
     *
     * @param wsToken The web service token
     * @param userId The user ID
     * @param limitFrom Starting position for pagination
     * @param limitNum Maximum number of conversations to return
     * @param type Optional filter by conversation type
     * @param favourites Optional filter for favourite conversations
     * @return List of conversations
     */
    suspend fun getConversations(
        wsToken: String,
        userId: Int,
        limitFrom: Int = 0,
        limitNum: Int = 20,
        type: ConversationType? = null,
        favourites: Boolean? = null
    ): ElearningGetConversationsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetConversationsRequest(userId, limitFrom, limitNum, type, favourites)
        )
    }

    /**
     * Gets messages from a conversation.
     *
     * @param wsToken The web service token
     * @param currentUserId The current user ID
     * @param conversationId The conversation ID
     * @param limitFrom Starting position for pagination
     * @param limitNum Maximum number of messages to return
     * @param newestFirst Whether to return newest messages first
     * @param timeFrom Only return messages after this timestamp
     * @return Messages from the conversation
     */
    suspend fun getConversationMessages(
        wsToken: String,
        currentUserId: Int,
        conversationId: Int,
        limitFrom: Int = 0,
        limitNum: Int = 20,
        newestFirst: Boolean = true,
        timeFrom: Long = 0
    ): ElearningGetConversationMessagesResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningGetConversationMessagesRequest(
                currentUserId, conversationId, limitFrom, limitNum, newestFirst, timeFrom
            )
        )
    }

    /**
     * Sends instant messages to users.
     *
     * @param wsToken The web service token
     * @param messages List of messages to send
     * @return Send results with message IDs
     */
    suspend fun sendInstantMessages(
        wsToken: String,
        messages: List<InstantMessage>
    ): ElearningSendInstantMessagesResponse {
        return executeAuthenticatedRequest(wsToken, ElearningSendInstantMessagesRequest(messages))
    }

    /**
     * Sends an instant message to a user.
     *
     * @param wsToken The web service token
     * @param toUserId The recipient user ID
     * @param text The message text
     * @return Send result
     */
    suspend fun sendInstantMessage(
        wsToken: String,
        toUserId: Int,
        text: String
    ): ElearningSendInstantMessagesResponse {
        return sendInstantMessages(wsToken, listOf(InstantMessage(toUserId, text)))
    }

    /**
     * Gets unread conversation counts by type.
     *
     * @param wsToken The web service token
     * @param userId The user ID
     * @return Unread counts by conversation type
     */
    suspend fun getUnreadConversationCounts(
        wsToken: String,
        userId: Int
    ): ElearningGetUnreadConversationCountsResponse {
        return executeAuthenticatedRequest(wsToken, ElearningGetUnreadConversationCountsRequest(userId))
    }

    /**
     * Marks all messages in a conversation as read.
     *
     * @param wsToken The web service token
     * @param userId The user ID
     * @param conversationId The conversation ID
     * @return Mark as read result
     */
    suspend fun markAllConversationMessagesAsRead(
        wsToken: String,
        userId: Int,
        conversationId: Int
    ): ElearningMarkAsReadResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningMarkAllConversationMessagesAsReadRequest(userId, conversationId)
        )
    }

    /**
     * Deletes conversations.
     *
     * @param wsToken The web service token
     * @param userId The user ID
     * @param conversationIds List of conversation IDs to delete
     * @return Delete result with any warnings
     */
    suspend fun deleteConversations(
        wsToken: String,
        userId: Int,
        conversationIds: List<Int>
    ): ElearningDeleteConversationsResponse {
        return executeAuthenticatedRequest(
            wsToken,
            ElearningDeleteConversationsRequest(userId, conversationIds)
        )
    }

    /**
     * Blocks a user.
     *
     * @param wsToken The web service token
     * @param userId The user ID performing the block
     * @param blockedUserId The user ID to block
     * @return Block result
     */
    suspend fun blockUser(
        wsToken: String,
        userId: Int,
        blockedUserId: Int
    ): ElearningBlockUserResponse {
        return executeAuthenticatedRequest(wsToken, ElearningBlockUserRequest(userId, blockedUserId))
    }

    /**
     * Unblocks a user.
     *
     * @param wsToken The web service token
     * @param userId The user ID performing the unblock
     * @param unblockedUserId The user ID to unblock
     * @return Unblock result
     */
    suspend fun unblockUser(
        wsToken: String,
        userId: Int,
        unblockedUserId: Int
    ): ElearningUnblockUserResponse {
        return executeAuthenticatedRequest(wsToken, ElearningUnblockUserRequest(userId, unblockedUserId))
    }
}
