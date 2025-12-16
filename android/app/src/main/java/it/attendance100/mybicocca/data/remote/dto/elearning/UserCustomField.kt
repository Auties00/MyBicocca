package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class UserCustomField (
    @SerializedName("type") val type: String? = null,
    @SerializedName("value") val value: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("shortname") val shortName: String? = null
)