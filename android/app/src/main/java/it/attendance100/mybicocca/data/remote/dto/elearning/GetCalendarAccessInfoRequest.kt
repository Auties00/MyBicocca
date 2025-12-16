package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCalendarAccessInfoRequest(
    @SerializedName("courseid") val courseId: Int? = null
)