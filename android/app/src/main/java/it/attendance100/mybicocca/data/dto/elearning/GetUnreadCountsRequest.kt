package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetUnreadCountsRequest(
    @SerializedName("userid") val userId: Int
)