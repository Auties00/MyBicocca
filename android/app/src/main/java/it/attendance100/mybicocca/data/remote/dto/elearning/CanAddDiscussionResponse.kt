package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CanAddDiscussionResponse(
    @SerializedName("status") val status: Boolean? = null,
    @SerializedName("canpindiscussions") val canPinDiscussions: Boolean? = null,
    @SerializedName("cancreateattachment") val canCreateAttachment: Boolean? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)
