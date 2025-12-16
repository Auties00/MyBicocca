package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetDiscussionPostsRequest(
    @SerializedName("discussionid") val discussionId: Int,
    @SerializedName("sortby") val sortBy: String? = null,
    @SerializedName("sortdirection") val sortDirection: String? = null
)
