package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCoursesByFieldRequest(
    @SerializedName("field") val field: CourseSearchFieldEnum? = null,
    @SerializedName("value") val value: String? = null
)
