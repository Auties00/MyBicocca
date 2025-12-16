package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class StartAttemptRequest(
    @SerializedName("quizid") val quizId: Int,
    @SerializedName("preflightdata") val preflightData: List<QuizPreflightData>? = null,
    @SerializedName("forcenew") val forceNew: Boolean? = null
)