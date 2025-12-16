package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetSubmissionsRequest(
    @SerializedName("assignmentids") val assignmentIds: List<Int>,
    @SerializedName("status") val status: String? = null,
    @SerializedName("since") val since: Int? = null,
    @SerializedName("before") val before: Int? = null
)
