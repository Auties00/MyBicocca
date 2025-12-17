package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetUserAttemptsResponse(
    @SerializedName("attempts") val attempts: List<QuizAttempt>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
