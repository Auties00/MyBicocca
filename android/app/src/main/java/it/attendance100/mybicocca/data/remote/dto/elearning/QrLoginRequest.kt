package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class QrLoginRequest(
    @SerializedName("qrloginkey") val qrLoginKey: String,
    @SerializedName("userid") val userId: Int
)