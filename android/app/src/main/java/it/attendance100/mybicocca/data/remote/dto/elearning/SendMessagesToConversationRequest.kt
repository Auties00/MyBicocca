package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SendMessagesToConversationRequest(
    @SerializedName("conversationid") val conversationId: Int,
    @SerializedName("messages") val messages: List<SendMessageEntry>
)

data class SendMessageEntry(
    @SerializedName("text") val text: String,
    @SerializedName("textformat") val textFormat: Int? = null
)