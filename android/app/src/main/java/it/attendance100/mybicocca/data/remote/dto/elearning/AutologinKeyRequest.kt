package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class AutologinKeyRequest(
    @SerializedName("privatetoken") val privateToken: String
)