package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ResendConfirmationEmailResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)