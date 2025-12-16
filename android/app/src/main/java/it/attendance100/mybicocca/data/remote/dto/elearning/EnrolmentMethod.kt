package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class EnrolmentMethod(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("wsfunction") val wsFunction: String? = null
)