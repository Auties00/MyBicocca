package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class QuizPreflightData(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)
