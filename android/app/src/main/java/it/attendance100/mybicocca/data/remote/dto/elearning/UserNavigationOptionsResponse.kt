package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class UserNavigationOptionsResponse(
    @SerializedName("courses") val courses: List<UserNavigationOption>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)