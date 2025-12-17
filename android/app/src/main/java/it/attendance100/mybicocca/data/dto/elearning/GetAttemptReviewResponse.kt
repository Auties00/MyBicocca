package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAttemptReviewResponse(
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("attempt") val attempt: QuizAttempt? = null,
    @SerializedName("additionaldata") val additionalData: List<AttemptReviewAdditionalData>? = null,
    @SerializedName("questions") val questions: List<AttemptQuestion>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
