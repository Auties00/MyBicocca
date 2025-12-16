package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCoursesByFieldResponse(
    @SerializedName("courses") val courses: List<CourseByField>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)