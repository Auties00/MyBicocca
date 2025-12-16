package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class PostCapabilities(
    @SerializedName("view") val view: Boolean? = null,
    @SerializedName("edit") val edit: Boolean? = null,
    @SerializedName("delete") val delete: Boolean? = null,
    @SerializedName("split") val split: Boolean? = null,
    @SerializedName("reply") val reply: Boolean? = null,
    @SerializedName("selfenrol") val selfEnrol: Boolean? = null,
    @SerializedName("export") val export: Boolean? = null,
    @SerializedName("controlreadstatus") val controlReadStatus: Boolean? = null,
    @SerializedName("canreplyprivately") val canReplyPrivately: Boolean? = null
)