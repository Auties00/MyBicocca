package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class ExternalFunctionResponse(
    @SerializedName("error") val error: Boolean? = null,
    @SerializedName("data") val data: String? = null,
    @SerializedName("exception") val exception: String? = null,
    @SerializedName("errorcode") val errorCode: String? = null
)