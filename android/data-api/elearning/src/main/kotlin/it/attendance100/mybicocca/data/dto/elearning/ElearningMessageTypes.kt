package it.attendance100.mybicocca.data.dto.elearning

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a conversation in the messaging system.
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
    val isIndividual: Boolean get() = type == 1
    val isGroup: Boolean get() = type == 2
    val isSelf: Boolean get() = type == 3
}

/**
 * Represents a member of a conversation.
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
