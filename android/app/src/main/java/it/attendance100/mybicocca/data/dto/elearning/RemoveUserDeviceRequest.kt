package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class RemoveUserDeviceRequest(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("appid") val appId: String? = null
)