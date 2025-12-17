package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ForumDiscussionPostsResponse(
    @SerializedName("posts") val posts: List<DiscussionPost>? = null,
    @SerializedName("ratinginfo") val ratingInfo: Any? = null, // Type is complex, leaving as Any
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
