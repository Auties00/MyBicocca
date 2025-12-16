package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class AddDiscussionPostResponse(
    @SerializedName("postid") val postId: Int? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null,
    @SerializedName("post") val post: DiscussionPost? = null,
    @SerializedName("messages") val messages: List<PostMessage>? = null
)
