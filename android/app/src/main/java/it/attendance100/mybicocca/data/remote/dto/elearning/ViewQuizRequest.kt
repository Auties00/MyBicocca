package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ViewQuizRequest(
    @SerializedName("quizid") val quizId: Int
)
