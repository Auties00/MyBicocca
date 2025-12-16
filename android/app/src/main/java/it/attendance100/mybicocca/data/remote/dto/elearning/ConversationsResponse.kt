package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ConversationsResponse(
    @SerializedName("conversations") val conversations: List<Conversation>? = null
)