package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class AssignmentSubmissions(
    @SerializedName("assignmentid") val assignmentId: Int? = null,
    @SerializedName("submissions") val submissions: List<Submission>? = null
)
