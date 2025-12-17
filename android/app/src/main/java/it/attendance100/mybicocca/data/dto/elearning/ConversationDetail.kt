package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI

data class ConversationDetail(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("subname") val subName: String? = null,
    @SerializedName("imageurl") val imageUrl: URI? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("membercount") val memberCount: Int? = null,
    @SerializedName("ismuted") val isMuted: Boolean? = null,
    @SerializedName("isfavourite") val isFavourite: Boolean? = null,
    @SerializedName("isread") val isRead: Boolean? = null,
    @SerializedName("unreadcount") val unreadCount: Int? = null,
    @SerializedName("members") val members: List<ConversationMember>? = null,
    @SerializedName("messages") val messages: List<ConversationMessage>? = null,
    @SerializedName("candeletemessagesforallusers") val canDeleteMessagesForAllUsers: Boolean? = null
)