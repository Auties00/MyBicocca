package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class AddUserDeviceRequest(
    @SerializedName("appid") val appId: String,
    @SerializedName("name") val name: String,
    @SerializedName("model") val model: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("version") val version: String,
    @SerializedName("pushid") val pushId: String,
    @SerializedName("uuid") val uuid: String
)