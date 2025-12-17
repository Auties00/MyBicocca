package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetQuizzesByCoursesRequest(
    @SerializedName("courseids") val courseIds: List<Int>? = null
)
