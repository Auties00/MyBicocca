package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ModuleCompletionData(
    @SerializedName("state") val state: Int? = null,
    @SerializedName("timecompleted") val timeCompleted: Int? = null,
    @SerializedName("overrideby") val overrideBy: Int? = null,
    @SerializedName("valueused") val valueUsed: Boolean? = null,
    @SerializedName("hascompletion") val hasCompletion: Boolean? = null,
    @SerializedName("isautomatic") val isAutomatic: Boolean? = null,
    @SerializedName("istrackeduser") val isTrackedUser: Boolean? = null,
    @SerializedName("uservisible") val userVisible: Boolean? = null
)