package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetRecentCoursesRequest(
    @SerializedName("userid") val userId: Int? = 0,
    @SerializedName("limit") val limit: Int? = null,
    @SerializedName("offset") val offset: Int? = null,
    @SerializedName("sort") val sort: String? = null
)
