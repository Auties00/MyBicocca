package it.attendance100.mybicocca.data.remote.elearning.dto

import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ElearningConversation type filter for message queries.
 */
@Serializable
sealed class ElearningConversationType(
    /**
     * The numeric value representing the conversation type.
     */
    val value: Int
) {
    /**
     * One-to-one private conversation.
     */
    data object Individual : ElearningConversationType(1)

    /**
     * Group conversation with multiple participants.
     */
    data object Group : ElearningConversationType(2)

    /**
     * Self-conversation (notes to self).
     */
    data object Self : ElearningConversationType(3)
}

/**
 * Request to retrieve a user's conversations.
 *
 * @property userId The user identifier to get conversations for.
 * @property limitFrom The offset for pagination.
 * @property limitNumber The maximum number of conversations to return.
 * @property type Optional conversation type filter.
 * @property favourites Optional filter for favourite conversations.
 * @property mergeSelf Whether to merge self-conversations into the list.
 */
@Serializable
class ElearningGetConversationsRequest(
    private val userId: Int,
    private val limitFrom: Int = 0,
    private val limitNumber: Int = 20,
    private val type: ElearningConversationType? = null,
    private val favourites: Boolean? = null,
    private val mergeSelf: Boolean = false
) : ElearningRequest<ElearningGetConversationsResponse> {
    override val functionName = "core_message_get_conversations"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("limitfrom", limitFrom.toString())
        formData.append("limitnum", limitNumber.toString())
        type?.let { formData.append("type", it.value.toString()) }
        favourites?.let { formData.append("favourites", if (it) "1" else "0") }
        if (mergeSelf) formData.append("mergeself", "1")
    }
}

/**
 * Response containing a list of conversations.
 *
 * @property conversations The list of conversations.
 */
@Serializable
data class ElearningGetConversationsResponse(
    @SerialName("conversations")
    val conversations: List<ElearningConversation> = emptyList()
) : ElearningResponse

/**
 * Request to retrieve messages from a specific conversation.
 *
 * @property currentUserId The current user's identifier.
 * @property conversationId The conversation identifier to get messages from.
 * @property limitFrom The offset for pagination.
 * @property limitNumber The maximum number of messages to return.
 * @property newestFirst Whether to return messages in reverse chronological order.
 * @property timeFrom Only return messages after this Unix timestamp.
 */
@Serializable
class ElearningGetConversationMessagesRequest(
    private val currentUserId: Int,
    private val conversationId: Int,
    private val limitFrom: Int = 0,
    private val limitNumber: Int = 20,
    private val newestFirst: Boolean = true,
    private val timeFrom: Long = 0
) : ElearningRequest<ElearningGetConversationMessagesResponse> {
    override val functionName = "core_message_get_conversation_messages"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("currentuserid", currentUserId.toString())
        formData.append("convid", conversationId.toString())
        formData.append("limitfrom", limitFrom.toString())
        formData.append("limitnum", limitNumber.toString())
        if (newestFirst) formData.append("newest", "1")
        if (timeFrom > 0) formData.append("timefrom", timeFrom.toString())
    }
}

/**
 * Response containing messages from a conversation.
 *
 * @property id The conversation identifier.
 * @property members The list of conversation members.
 * @property messages The list of messages in the conversation.
 */
@Serializable
data class ElearningGetConversationMessagesResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("members")
    val members: List<ElearningConversationMember> = emptyList(),
    @SerialName("messages")
    val messages: List<ElearningMessageItem> = emptyList()
) : ElearningResponse

/**
 * Request to delete conversations.
 *
 * @property userId The user identifier performing the deletion.
 * @property conversationIds The list of conversation identifiers to delete.
 */
@Serializable
class ElearningDeleteConversationsRequest(
    private val userId: Int,
    private val conversationIds: List<Int>
) : ElearningRequest<ElearningDeleteConversationsResponse> {
    override val functionName = "core_message_delete_conversations_by_id"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        conversationIds.forEachIndexed { index, id ->
            formData.append("conversationids[$index]", id.toString())
        }
    }
}

/**
 * Response from deleting conversations.
 *
 * @property items Any warnings that occurred during the deletion.
 */
@Serializable
data class ElearningDeleteConversationsResponse(
    override val items: List<ElearningResponseWarning>
) : ElearningListResponse<ElearningResponseWarning> {
    /**
     * Alias for items providing a more descriptive name.
     */
    val warnings: List<ElearningResponseWarning> get() = items
}

/**
 * Request to get unread conversation counts.
 *
 * @property userId The user identifier to get unread counts for.
 */
@Serializable
class ElearningGetUnreadConversationCountsRequest(
    private val userId: Int
) : ElearningRequest<ElearningGetUnreadConversationCountsResponse> {
    override val functionName = "core_message_get_unread_conversation_counts"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
    }
}

/**
 * Response containing unread conversation counts.
 *
 * @property favourites The count of unread messages in favourite conversations.
 * @property types The counts of unread messages by conversation type.
 */
@Serializable
data class ElearningGetUnreadConversationCountsResponse(
    @SerialName("favourites")
    val favourites: Int = 0,
    @SerialName("types")
    val types: ElearningConversationTypeCounts? = null
) : ElearningResponse

/**
 * Request to mark all messages in a conversation as read.
 *
 * @property userId The user identifier marking messages as read.
 * @property conversationId The conversation identifier to mark as read.
 */
@Serializable
class ElearningMarkAllConversationMessagesAsReadRequest(
    private val userId: Int,
    private val conversationId: Int
) : ElearningRequest<ElearningMarkAsReadResponse> {
    override val functionName = "core_message_mark_all_conversation_messages_as_read"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        formData.append("userid", userId.toString())
        formData.append("conversationid", conversationId.toString())
    }
}

/**
 * Response from marking messages as read.
 *
 * @property status Whether the operation was successful.
 * @property warnings Any warnings that occurred during the request.
 */
@Serializable
data class ElearningMarkAsReadResponse(
    @SerialName("status")
    val status: Boolean? = null,
    @SerialName("warnings")
    val warnings: List<ElearningResponseWarning> = emptyList()
) : ElearningResponse

/**
 * Represents a message to send.
 *
 * @property toUserId The recipient user identifier.
 * @property text The message text content.
 */
@Serializable
data class ElearningInstantMessage(
    /**
     * The identifier of the user to send the message to.
     */
    val toUserId: Int,
    /**
     * The text content of the message.
     */
    val text: String
)

/**
 * Request to send instant messages to users.
 *
 * @property messages The list of messages to send.
 */
@Serializable
class ElearningSendInstantMessagesRequest(
    private val messages: List<ElearningInstantMessage>
) : ElearningRequest<ElearningSendInstantMessagesResponse> {
    override val functionName = "core_message_send_instant_messages"

    override fun writeAdditionalData(formData: ParametersBuilder) {
        messages.forEachIndexed { index, message ->
            formData.append("messages[$index][touserid]", message.toUserId.toString())
            formData.append("messages[$index][text]", message.text)
            formData.append("messages[$index][textformat]", "1")
        }
    }
}

/**
 * Response from sending instant messages.
 *
 * @property items The list of sent message results.
 */
@Serializable
data class ElearningSendInstantMessagesResponse(
    override val items: List<ElearningSentMessage>
) : ElearningListResponse<ElearningSentMessage>

/**
 * Represents a conversation in the messaging system.
 *
 * @property id The unique identifier of the conversation.
 * @property name The name of the conversation.
 * @property subname The subtitle or description of the conversation.
 * @property imageUrl The URL to the conversation's image.
 * @property type The type of conversation (1=individual, 2=group, 3=self).
 * @property memberCount The number of members in the conversation.
 * @property isMuted Whether the conversation is muted.
 * @property isFavourite Whether the conversation is marked as favourite.
 * @property isRead Whether all messages in the conversation have been read.
 * @property unreadCount The number of unread messages.
 * @property members The list of conversation members.
 * @property messages The list of recent messages in the conversation.
 * @property canDeleteMessagesForAllUsers Whether the user can delete messages for all participants.
 */
@Serializable
data class ElearningConversation(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String? = null,
    @SerialName("subname")
    val subname: String? = null,
    @SerialName("imageurl")
    val imageUrl: String? = null,
    @SerialName("type")
    val type: Int? = null,
    @SerialName("membercount")
    val memberCount: Int? = null,
    @SerialName("ismuted")
    val isMuted: Boolean? = null,
    @SerialName("isfavourite")
    val isFavourite: Boolean? = null,
    @SerialName("isread")
    val isRead: Boolean? = null,
    @SerialName("unreadcount")
    val unreadCount: Int? = null,
    @SerialName("members")
    val members: List<ElearningConversationMember>? = null,
    @SerialName("messages")
    val messages: List<ElearningMessageItem>? = null,
    @SerialName("candeletemessagesforallusers")
    val canDeleteMessagesForAllUsers: Boolean? = null
) {
    /**
     * Whether this is a one-to-one private conversation.
     */
    val isIndividual: Boolean get() = type == 1

    /**
     * Whether this is a group conversation.
     */
    val isGroup: Boolean get() = type == 2

    /**
     * Whether this is a self-conversation (notes to self).
     */
    val isSelf: Boolean get() = type == 3
}

/**
 * Represents a member of a conversation.
 *
 * @property id The unique identifier of the user.
 * @property fullName The full name of the user.
 * @property profileUrl The URL to the user's profile.
 * @property profileImageUrl The URL to the user's profile image.
 * @property profileImageUrlSmall The URL to a smaller version of the profile image.
 * @property isOnline Whether the user is currently online.
 * @property showOnlineStatus Whether to show the user's online status.
 * @property isBlocked Whether the user has blocked the current user.
 * @property isContact Whether the user is a contact.
 * @property isDeleted Whether the user account has been deleted.
 * @property canMessageEvenIfBlocked Whether messages can be sent even if blocked.
 * @property canMessage Whether the current user can send messages to this user.
 * @property requiresContact Whether the user requires contact approval before messaging.
 * @property contactRequests The list of pending contact requests from this user.
 */
@Serializable
data class ElearningConversationMember(
    @SerialName("id")
    val id: Int,
    @SerialName("fullname")
    val fullName: String? = null,
    @SerialName("profileurl")
    val profileUrl: String? = null,
    @SerialName("profileimageurl")
    val profileImageUrl: String? = null,
    @SerialName("profileimageurlsmall")
    val profileImageUrlSmall: String? = null,
    @SerialName("isonline")
    val isOnline: Boolean? = null,
    @SerialName("showonlinestatus")
    val showOnlineStatus: Boolean? = null,
    @SerialName("isblocked")
    val isBlocked: Boolean? = null,
    @SerialName("iscontact")
    val isContact: Boolean? = null,
    @SerialName("isdeleted")
    val isDeleted: Boolean? = null,
    @SerialName("canmessageevenifblocked")
    val canMessageEvenIfBlocked: Boolean? = null,
    @SerialName("canmessage")
    val canMessage: Boolean? = null,
    @SerialName("requirescontact")
    val requiresContact: Boolean? = null,
    @SerialName("contactrequests")
    val contactRequests: List<ElearningContactRequest>? = null
)

/**
 * Represents a contact request.
 *
 * @property id The unique identifier of the contact request.
 * @property userId The user identifier who sent the request.
 * @property requestedUserId The user identifier who received the request.
 * @property timeCreated The Unix timestamp when the request was created.
 */
@Serializable
data class ElearningContactRequest(
    @SerialName("id")
    val id: Int,
    @SerialName("userid")
    val userId: Int,
    @SerialName("requesteduserid")
    val requestedUserId: Int,
    @SerialName("timecreated")
    val timeCreated: Long? = null
)

/**
 * Represents a message in a conversation.
 *
 * @property id The unique identifier of the message.
 * @property userIdFrom The identifier of the user who sent the message.
 * @property text The text content of the message.
 * @property timeCreated The Unix timestamp when the message was created.
 */
@Serializable
data class ElearningMessageItem(
    @SerialName("id")
    val id: Int,
    @SerialName("useridfrom")
    val userIdFrom: Int? = null,
    @SerialName("text")
    val text: String? = null,
    @SerialName("timecreated")
    val timeCreated: Long? = null
)

/**
 * Represents counts of unread messages by conversation type.
 *
 * @property individual The count of unread messages in individual conversations.
 * @property group The count of unread messages in group conversations.
 * @property self The count of unread messages in self-conversations.
 */
@Serializable
data class ElearningConversationTypeCounts(
    @SerialName("1")
    val individual: Int = 0,
    @SerialName("2")
    val group: Int = 0,
    @SerialName("3")
    val self: Int = 0
)

/**
 * Represents a result from sending a message.
 *
 * @property messageId The identifier of the sent message.
 * @property clientMessageId The client-provided message identifier for correlation.
 * @property errorMessage The error message if the send failed.
 */
@Serializable
data class ElearningSentMessage(
    @SerialName("msgid")
    val messageId: Int? = null,
    @SerialName("clientmsgid")
    val clientMessageId: String? = null,
    @SerialName("errormessage")
    val errorMessage: String? = null
)
