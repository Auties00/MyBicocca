package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SearchCoursesResponse(
    @SerializedName("total") val total: Int? = null,
    @SerializedName("courses") val courses: List<CourseByField>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)