package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class PasswordResetResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("notice") val notice: String? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)