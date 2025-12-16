package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class UnblockUserRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("unblockeduserid") val unblockedUserId: Int
)