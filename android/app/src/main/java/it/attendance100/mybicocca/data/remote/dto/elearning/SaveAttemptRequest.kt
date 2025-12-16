package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SaveAttemptRequest(
    @SerializedName("attemptid") val attemptId: Int,
    @SerializedName("data") val data: List<QuizProcessAttemptData>? = null,
    @SerializedName("preflightdata") val preflightData: List<QuizPreflightData>? = null
)