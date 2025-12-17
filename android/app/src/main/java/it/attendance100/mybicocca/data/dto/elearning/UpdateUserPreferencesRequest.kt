package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class UpdateUserPreferencesRequest(
    @SerializedName("userid") val userId: Int? = 0,
    @SerializedName("emailstop") val emailStop: Int? = null,
    @SerializedName("preferences") val preferences: List<UpdateUserPreferenceEntry>? = null
)

data class UpdateUserPreferenceEntry(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: String
)