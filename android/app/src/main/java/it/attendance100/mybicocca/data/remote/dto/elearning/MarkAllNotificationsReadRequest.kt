package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class MarkAllNotificationsReadRequest(
    @SerializedName("useridto") val userIdTo: Int,
    @SerializedName("useridfrom") val userIdFrom: Int? = null,
    @SerializedName("timecreatedto") val timeCreatedTo: Int? = null
)