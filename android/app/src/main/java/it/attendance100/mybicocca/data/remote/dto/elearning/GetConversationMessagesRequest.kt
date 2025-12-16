package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetConversationMessagesRequest(
    @SerializedName("currentuserid") val currentUserId: Int,
    @SerializedName("convid") val convId: Int,
    @SerializedName("limitfrom") val limitFrom: Int? = null,
    @SerializedName("limitnum") val limitNum: Int? = null,
    @SerializedName("newest") val newest: Boolean? = null,
    @SerializedName("timefrom") val timeFrom: Int? = null
)