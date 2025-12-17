package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UserEnrolledCourse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null,
    @SerializedName("shortname") val shortName: String? = null
)