package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SendInstantMessagesRequest(
    @SerializedName("messages") val messages: List<InstantMessageEntry>
)

data class InstantMessageEntry(
    @SerializedName("touserid") val toUserId: Int,
    @SerializedName("text") val text: String,
    @SerializedName("textformat") val textFormat: Int? = null
)