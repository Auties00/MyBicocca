package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UserPreferencesResponse(
    @SerializedName("preferences") val preferences: List<UserPreference>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)