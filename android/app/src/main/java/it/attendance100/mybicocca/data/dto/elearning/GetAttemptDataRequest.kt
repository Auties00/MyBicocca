package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAttemptDataRequest(
    @SerializedName("attemptid") val attemptId: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("preflightdata") val preflightData: List<QuizPreflightData>? = null
)