package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class AttemptAccessInformation(
    @SerializedName("endtime") val endTime: Int? = null,
    @SerializedName("isfinished") val isFinished: Boolean? = null,
    @SerializedName("ispreflightcheckrequired") val isPreflightCheckRequired: Boolean? = null,
    @SerializedName("preventnewattemptreasons") val preventNewAttemptReasons: List<String>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
