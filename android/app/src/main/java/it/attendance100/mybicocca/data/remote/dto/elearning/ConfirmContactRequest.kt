package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ConfirmContactRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("requesteduserid") val requestedUserId: Int
)