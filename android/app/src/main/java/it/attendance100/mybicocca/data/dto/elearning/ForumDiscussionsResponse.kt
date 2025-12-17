package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ForumDiscussionsResponse(
    @SerializedName("discussions") val discussions: List<ForumDiscussion>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
