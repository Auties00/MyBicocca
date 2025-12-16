package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetGradesResponse(
    @SerializedName("assignments") val assignments: List<AssignmentGrades>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
