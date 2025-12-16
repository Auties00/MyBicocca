package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CheckUpdatesResponse(
    @SerializedName("instances") val instances: List<UpdateInstance>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)