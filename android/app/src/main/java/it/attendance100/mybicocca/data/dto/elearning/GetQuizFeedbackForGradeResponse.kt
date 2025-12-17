package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetQuizFeedbackForGradeResponse(
    @SerializedName("feedbacktext") val feedbackText: String? = null,
    @SerializedName("feedbacktextformat") val feedbackTextFormat: Int? = null,
    @SerializedName("feedbackinlinefiles") val feedbackInlineFiles: List<MoodleFile>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
