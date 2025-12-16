package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SetLockStateRequest(
    @SerializedName("forumid") val forumId: Int,
    @SerializedName("discussionid") val discussionId: Int,
    @SerializedName("targetstate") val targetState: Int
)
