package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ConversationMessage(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("useridfrom") val userIdFrom: Int? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("timecreated") val timeCreated: Int? = null
)