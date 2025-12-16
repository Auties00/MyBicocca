package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class RemoveUserDeviceResponse(
    @SerializedName("removed") val removed: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)