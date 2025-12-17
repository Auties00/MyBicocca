package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetQuizFeedbackForGradeRequest(
    @SerializedName("quizid") val quizId: Int,
    @SerializedName("grade") val grade: Double
)
