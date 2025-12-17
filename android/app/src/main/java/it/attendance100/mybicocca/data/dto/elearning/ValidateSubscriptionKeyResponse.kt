package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ValidateSubscriptionKeyResponse(
    @SerializedName("validated") val validated: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)