package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCoursesRequest(
    @SerializedName("options") val options: GetCoursesOptions? = null
)

data class GetCoursesOptions(
    @SerializedName("ids") val ids: List<Int>? = null
)