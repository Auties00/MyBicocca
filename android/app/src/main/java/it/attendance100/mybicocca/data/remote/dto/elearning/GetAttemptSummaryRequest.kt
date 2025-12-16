package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAttemptSummaryRequest(
    @SerializedName("attemptid") val attemptId: Int,
    @SerializedName("preflightdata") val preflightData: List<QuizPreflightData>? = null
)