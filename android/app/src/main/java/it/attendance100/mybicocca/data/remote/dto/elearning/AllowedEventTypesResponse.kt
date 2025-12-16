package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class AllowedEventTypesResponse(
    @SerializedName("allowedeventtypes") val allowedEventTypes: List<String>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)