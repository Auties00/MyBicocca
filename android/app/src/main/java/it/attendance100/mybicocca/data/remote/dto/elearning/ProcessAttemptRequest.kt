package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ProcessAttemptRequest(
    @SerializedName("attemptid") val attemptId: Int,
    @SerializedName("data") val data: List<QuizProcessAttemptData>? = null,
    @SerializedName("finishattempt") val finishAttempt: Boolean? = null,
    @SerializedName("timeup") val timeUp: Boolean? = null,
    @SerializedName("preflightdata") val preflightData: List<QuizPreflightData>? = null
)