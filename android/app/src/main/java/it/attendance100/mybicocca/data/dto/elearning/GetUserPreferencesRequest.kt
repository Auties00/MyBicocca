package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetUserPreferencesRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("userid") val userId: Int? = 0
)