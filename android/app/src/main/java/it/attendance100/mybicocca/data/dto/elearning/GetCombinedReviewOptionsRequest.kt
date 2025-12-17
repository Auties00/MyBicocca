package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCombinedReviewOptionsRequest(
    @SerializedName("quizid") val quizId: Int,
    @SerializedName("userid") val userId: Int? = 0
)
