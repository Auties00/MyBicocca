package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class PostUrls(
    @SerializedName("view") val view: String? = null,
    @SerializedName("viewisolated") val viewIsolated: String? = null,
    @SerializedName("viewparent") val viewParent: String? = null,
    @SerializedName("edit") val edit: String? = null,
    @SerializedName("delete") val delete: String? = null,
    @SerializedName("split") val split: String? = null,
    @SerializedName("reply") val reply: String? = null,
    @SerializedName("export") val export: String? = null,
    @SerializedName("markasread") val markAsRead: String? = null,
    @SerializedName("markasunread") val markAsUnread: String? = null,
    @SerializedName("discuss") val discuss: String? = null
)
