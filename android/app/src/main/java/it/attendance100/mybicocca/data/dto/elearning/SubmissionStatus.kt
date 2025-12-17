package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class SubmissionStatus(
    @SerializedName("gradingsummary") val gradingSummary: GradingSummary? = null,
    @SerializedName("lastattempt") val lastAttempt: LastAttempt? = null,
    @SerializedName("feedback") val feedback: SubmissionFeedback? = null,
    @SerializedName("previousattempts") val previousAttempts: List<PreviousAttempt>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
