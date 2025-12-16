package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class ContentResponse(
    @SerializedName("templates") val templates: List<ContentTemplate>? = null,
    @SerializedName("javascript") val javascript: String? = null,
    @SerializedName("otherdata") val otherData: List<UserPreference>? = null,
    @SerializedName("files") val files: List<MoodleFile>? = null,
    @SerializedName("restrict") val restrict: ContentRestrict? = null,
    @SerializedName("disabled") val disabled: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)