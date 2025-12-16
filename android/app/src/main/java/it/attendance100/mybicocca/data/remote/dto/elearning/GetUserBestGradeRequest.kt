package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetUserBestGradeRequest(
    @SerializedName("quizid") val quizId: Int,
    @SerializedName("userid") val userId: Int? = 0
)
