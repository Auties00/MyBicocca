package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetMessagesRequest(
    @SerializedName("useridto") val userIdTo: Int,
    @SerializedName("useridfrom") val userIdFrom: Int? = null,
    @SerializedName("type") val type: String,
    @SerializedName("read") val read: Int? = null,
    @SerializedName("newestfirst") val newestFirst: Boolean? = null,
    @SerializedName("limitfrom") val limitFrom: Int? = null,
    @SerializedName("limitnum") val limitNum: Int? = null
)