package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetForumDiscussionsRequest(
    @SerializedName("forumid") val forumId: Int,
    @SerializedName("sortorder") val sortOrder: Int? = null,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("perpage") val perPage: Int? = null,
    @SerializedName("groupid") val groupId: Int? = null
)
