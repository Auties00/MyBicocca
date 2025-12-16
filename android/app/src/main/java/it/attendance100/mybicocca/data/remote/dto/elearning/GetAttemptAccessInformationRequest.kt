package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAttemptAccessInformationRequest(
    @SerializedName("quizid") val quizId: Int,
    @SerializedName("attemptid") val attemptId: Int? = 0
)
