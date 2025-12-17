package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName
import java.net.URI

data class AutologinKeyResponse(
    @SerializedName("key") val key: String? = null,
    @SerializedName("autologinurl") val autologinUrl: URI? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)