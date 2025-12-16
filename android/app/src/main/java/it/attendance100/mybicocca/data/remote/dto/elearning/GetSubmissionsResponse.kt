package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetSubmissionsResponse(
    @SerializedName("assignments") val assignments: List<AssignmentSubmissions>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
