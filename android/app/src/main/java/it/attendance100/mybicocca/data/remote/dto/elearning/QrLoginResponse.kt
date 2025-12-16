package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class QrLoginResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("privatetoken") val privateToken: String? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)