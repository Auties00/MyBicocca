package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class DeleteConversationsRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("conversationids") val conversationIds: List<Int>
)