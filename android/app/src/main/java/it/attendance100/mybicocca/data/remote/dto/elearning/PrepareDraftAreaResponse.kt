package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class PrepareDraftAreaResponse(
    @SerializedName("draftitemid") val draftItemId: Int? = null,
    @SerializedName("files") val files: List<DraftFile>? = null,
    @SerializedName("areaoptions") val areaOptions: List<UserPreference>? = null,
    @SerializedName("messagetext") val messageText: String? = null,
    @SerializedName("messageformat") val messageFormat: Int? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
