package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class MarkMessageReadResponse(
    @SerializedName("messageid") val messageId: Int? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)