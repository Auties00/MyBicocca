package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAssignmentsResponse(
    @SerializedName("courses") val courses: List<AssignmentCourse>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)