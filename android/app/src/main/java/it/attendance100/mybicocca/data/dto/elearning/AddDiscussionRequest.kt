package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class AddDiscussionRequest(
    @SerializedName("forumid") val forumId: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("message") val message: String,
    @SerializedName("groupid") val groupId: Int? = null,
    @SerializedName("options") val options: List<AddDiscussionOption>? = null
)

data class AddDiscussionOption(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
