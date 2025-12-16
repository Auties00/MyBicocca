package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ViewAttemptReviewRequest(
    @SerializedName("attemptid") val attemptId: Int
)
