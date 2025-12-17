package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PasswordResetRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null
)