package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ViewAttemptRequest(
    @SerializedName("attemptid") val attemptId: Int,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("preflightdata") val preflightData: List<QuizPreflightData>? = null
)