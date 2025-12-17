package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetForumDiscussionPostsRequest(
    @SerializedName("discussionid") val discussionId: Int,
    @SerializedName("sortby") val sortBy: String? = null,
    @SerializedName("sortdirection") val sortDirection: String? = null,
    @SerializedName("includeinlineattachments") val includeInlineAttachments: Boolean? = null
)
