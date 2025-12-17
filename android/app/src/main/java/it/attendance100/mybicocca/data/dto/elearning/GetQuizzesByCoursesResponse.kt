package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetQuizzesByCoursesResponse(
    @SerializedName("quizzes") val quizzes: List<Quiz>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
