package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UpdateDiscussionPostRequest(
    @SerializedName("postid") val postId: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("message") val message: String,
    @SerializedName("messageformat") val messageFormat: Int? = null,
    @SerializedName("options") val options: List<UpdateDiscussionPostOption>? = null
)

data class UpdateDiscussionPostOption(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
