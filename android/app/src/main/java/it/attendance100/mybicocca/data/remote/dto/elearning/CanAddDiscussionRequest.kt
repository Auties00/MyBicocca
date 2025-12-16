package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CanAddDiscussionRequest(
    @SerializedName("forumid") val forumId: Int,
    @SerializedName("groupid") val groupId: Int? = null
)
