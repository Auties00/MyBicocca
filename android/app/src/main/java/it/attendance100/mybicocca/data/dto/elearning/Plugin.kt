package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class Plugin(
    @SerializedName("component") val component: String? = null,
    @SerializedName("version") val version: String? = null,
    @SerializedName("addon") val addon: String? = null,
    @SerializedName("dependencies") val dependencies: List<String>? = null,
    @SerializedName("fileurl") val fileUrl: String? = null,
    @SerializedName("filehash") val fileHash: String? = null,
    @SerializedName("filesize") val fileSize: Int? = null,
    @SerializedName("handlers") val handlers: String? = null,
    @SerializedName("lang") val lang: String? = null
)