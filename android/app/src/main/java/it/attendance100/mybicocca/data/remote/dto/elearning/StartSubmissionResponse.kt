package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class StartSubmissionResponse(
    @SerializedName("submissionid") val submissionId: Int? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
