package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class SearchMessagesAreaRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("search") val search: String,
    @SerializedName("limitfrom") val limitFrom: Int? = null,
    @SerializedName("limitnum") val limitNum: Int? = null
)