package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ViewAttemptSummaryRequest(
    @SerializedName("attemptid") val attemptId: Int,
    @SerializedName("preflightdata") val preflightData: List<QuizPreflightData>? = null
)