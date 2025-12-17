package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class AddDiscussionPostRequest(
    @SerializedName("postid") val postId: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("message") val message: String,
    @SerializedName("options") val options: List<AddDiscussionPostOption>? = null
)

data class AddDiscussionPostOption(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
