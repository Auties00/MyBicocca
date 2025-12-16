package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCourseContentsRequest(
    @SerializedName("courseid") val courseId: Int,
    @SerializedName("options") val options: List<CourseContentOption>? = null
)

data class CourseContentOption(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)