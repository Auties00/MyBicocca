package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetUserAttemptsRequest(
    @SerializedName("quizid") val quizId: Int,
    @SerializedName("userid") val userId: Int? = 0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("includepreviews") val includePreviews: Boolean? = null
)
