package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ViewUserProfileRequest(
    @SerializedName("userid") val userId: Int,
    @SerializedName("courseid") val courseId: Int? = 0
)