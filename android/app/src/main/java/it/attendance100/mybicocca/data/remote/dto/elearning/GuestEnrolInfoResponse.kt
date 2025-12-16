package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GuestEnrolInfoResponse(
    @SerializedName("instanceinfo") val instanceInfo: GuestInstanceInfo? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)