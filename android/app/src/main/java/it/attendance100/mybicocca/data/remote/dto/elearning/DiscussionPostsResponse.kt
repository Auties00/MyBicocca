package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class DiscussionPostsResponse(
    @SerializedName("posts") val posts: List<DiscussionPost>? = null,
    @SerializedName("forumid") val forumId: Int? = null,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("ratinginfo") val ratingInfo: Any? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
