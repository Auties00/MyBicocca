package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class DeclineContactRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("requesteduserid") val requestedUserId: Int
)