package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetAllowedEventTypesRequest(
    @SerializedName("courseid") val courseId: Int? = null
)