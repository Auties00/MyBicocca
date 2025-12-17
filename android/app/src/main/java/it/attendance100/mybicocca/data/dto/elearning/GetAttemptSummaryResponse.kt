package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAttemptSummaryResponse(
    @SerializedName("questions") val questions: List<AttemptQuestion>? = null,
    @SerializedName("totalunanswered") val totalUnanswered: Int? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
