package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("settings") val settings: List<UserPreference>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)