package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetForumAccessInfoRequest(
    @SerializedName("forumid") val forumId: Int
)
