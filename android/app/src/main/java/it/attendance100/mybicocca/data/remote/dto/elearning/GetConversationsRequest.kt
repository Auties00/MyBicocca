package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetConversationsRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("limitfrom") val limitFrom: Int? = null,
    @SerializedName("limitnum") val limitNum: Int? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("favourites") val favourites: Boolean? = null,
    @SerializedName("mergeself") val mergeSelf: Boolean? = null
)