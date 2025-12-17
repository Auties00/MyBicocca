package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class StartAttemptResponse(
    @SerializedName("attempt") val attempt: QuizAttempt? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
