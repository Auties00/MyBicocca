package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class MarkMessageReadRequest(
    @SerializedName("messageid") val messageId: Int,
    @SerializedName("timeread") val timeRead: Int? = null
)