package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GuestInstanceInfo(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("courseid") val courseId: Int? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("passwordrequired") val passwordRequired: Boolean? = null
)