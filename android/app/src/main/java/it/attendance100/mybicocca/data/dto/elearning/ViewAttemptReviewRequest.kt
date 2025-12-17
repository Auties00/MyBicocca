package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ViewAttemptReviewRequest(
    @SerializedName("attemptid") val attemptId: Int
)
