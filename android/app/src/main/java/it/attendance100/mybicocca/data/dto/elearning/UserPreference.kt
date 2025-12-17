package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UserPreference (
    @SerializedName("name") val name: String? = null,
    @SerializedName("value") val value: String? = null
)