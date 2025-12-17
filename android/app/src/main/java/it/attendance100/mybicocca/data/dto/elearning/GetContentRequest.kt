package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetContentRequest(
    @SerializedName("component") val component: String,
    @SerializedName("method") val method: String,
    @SerializedName("args") val args: List<ContentArg>? = null
)

data class ContentArg(
    @SerializedName("name") val name: String,
    @SerializedName("value") val value: String
)