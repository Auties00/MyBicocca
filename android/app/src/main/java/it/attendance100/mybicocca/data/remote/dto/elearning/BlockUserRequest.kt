package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class BlockUserRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("blockeduserid") val blockedUserId: Int
)