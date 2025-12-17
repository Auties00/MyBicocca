package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PluginsResponse(
    @SerializedName("plugins") val plugins: List<Plugin>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)