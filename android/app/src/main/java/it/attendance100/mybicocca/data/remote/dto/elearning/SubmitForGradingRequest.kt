package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SubmitForGradingRequest(
    @SerializedName("assignmentid") val assignmentId: Int,
    @SerializedName("acceptsubmissionstatement") val acceptSubmissionStatement: Boolean
)
