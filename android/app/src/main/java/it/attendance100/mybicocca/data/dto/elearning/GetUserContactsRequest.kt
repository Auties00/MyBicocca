package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetUserContactsRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("limitfrom") val limitFrom: Int? = null,
    @SerializedName("limitnum") val limitNum: Int? = null
)