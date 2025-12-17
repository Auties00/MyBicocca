package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UserNavigationOption(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("options") val options: List<NavigationOption>? = null
)