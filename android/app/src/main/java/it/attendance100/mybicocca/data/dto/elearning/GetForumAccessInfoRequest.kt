package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetForumAccessInfoRequest(
    @SerializedName("forumid") val forumId: Int
)
