package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetConversationBetweenUsersRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("otheruserid") val otherUserId: Int,
    @SerializedName("includecontactrequests") val includeContactRequests: Boolean? = null,
    @SerializedName("includeprivacyinfo") val includePrivacyInfo: Boolean? = null,
    @SerializedName("memberlimit") val memberLimit: Int? = null,
    @SerializedName("memberoffset") val memberOffset: Int? = null,
    @SerializedName("messagelimit") val messageLimit: Int? = null,
    @SerializedName("messageoffset") val messageOffset: Int? = null,
    @SerializedName("newestmessagesfirst") val newestMessagesFirst: Boolean? = null
)