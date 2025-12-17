package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class SetLockStateResponse(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("locked") val locked: Boolean? = null,
    @SerializedName("times") val times: LockTimes? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
