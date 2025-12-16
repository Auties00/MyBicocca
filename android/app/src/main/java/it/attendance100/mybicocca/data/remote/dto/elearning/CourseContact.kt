package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CourseContact(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("fullname") val fullName: String? = null
)