package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetQuizAccessInformationRequest(
    @SerializedName("quizid") val quizId: Int
)
