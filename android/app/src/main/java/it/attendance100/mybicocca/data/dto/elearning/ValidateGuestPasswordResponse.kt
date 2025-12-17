package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ValidateGuestPasswordResponse(
    @SerializedName("validated") val validated: Boolean? = null,
    @SerializedName("hint") val hint: String? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)