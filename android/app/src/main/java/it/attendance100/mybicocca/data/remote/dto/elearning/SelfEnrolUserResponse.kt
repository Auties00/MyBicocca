package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class SelfEnrolUserResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)