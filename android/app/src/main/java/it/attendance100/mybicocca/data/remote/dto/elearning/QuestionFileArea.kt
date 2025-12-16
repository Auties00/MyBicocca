package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class QuestionFileArea(
    @SerializedName("area") val area: String? = null,
    @SerializedName("files") val files: List<MoodleFile>? = null
)
