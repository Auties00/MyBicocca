package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ConversationMessagesResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("members") val members: List<ConversationMember>? = null,
    @SerializedName("messages") val messages: List<ConversationMessage>? = null
)