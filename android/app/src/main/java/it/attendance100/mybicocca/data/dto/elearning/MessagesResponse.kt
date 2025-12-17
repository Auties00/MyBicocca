package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class MessagesResponse(
    @SerializedName("messages") val messages: List<MessageDetail>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)