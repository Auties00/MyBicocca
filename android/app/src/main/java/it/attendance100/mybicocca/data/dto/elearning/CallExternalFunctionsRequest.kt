package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CallExternalFunctionsRequest(
    @SerializedName("requests") val requests: List<CallExternalFunctionEntry>
)

data class CallExternalFunctionEntry(
    @SerializedName("function") val function: String,
    @SerializedName("arguments") val arguments: String? = null,
    @SerializedName("settingraw") val settingRaw: Boolean? = null,
    @SerializedName("settingfilter") val settingFilter: Boolean? = null,
    @SerializedName("settingfileurl") val settingFileUrl: Boolean? = null,
    @SerializedName("settinglang") val settingLang: String? = null
)