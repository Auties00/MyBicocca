package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAssignmentsRequest(
    @SerializedName("courseids") val courseIds: List<Int>? = null,
    @SerializedName("capabilities") val capabilities: List<String>? = null,
    @SerializedName("includenotenrolledcourses") val includeNotEnrolledCourses: Boolean? = null
)