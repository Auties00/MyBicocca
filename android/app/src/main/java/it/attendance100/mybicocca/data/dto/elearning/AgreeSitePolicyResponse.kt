package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class AgreeSitePolicyResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)