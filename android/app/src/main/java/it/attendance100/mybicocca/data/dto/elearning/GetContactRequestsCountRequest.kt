package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetContactRequestsCountRequest(
    @SerializedName("userid") val userId: Int
)