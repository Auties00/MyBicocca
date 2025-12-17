package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAttemptDataResponse(
    @SerializedName("attempt") val attempt: QuizAttempt? = null,
    @SerializedName("messages") val messages: List<String>? = null,
    @SerializedName("nextpage") val nextPage: Int? = null,
    @SerializedName("questions") val questions: List<AttemptQuestion>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
