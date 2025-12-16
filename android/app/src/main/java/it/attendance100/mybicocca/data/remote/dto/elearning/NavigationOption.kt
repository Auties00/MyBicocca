package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class NavigationOption(
    @SerializedName("name") val name: String? = null,
    @SerializedName("available") val available: Boolean? = null
)