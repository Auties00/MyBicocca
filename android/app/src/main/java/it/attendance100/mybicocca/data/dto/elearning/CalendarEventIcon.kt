package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarEventIcon(
    @SerializedName("key") val key: String? = null,
    @SerializedName("component") val component: String? = null,
    @SerializedName("alttext") val altText: String? = null,
    @SerializedName("iconurl") val iconUrl: String? = null,
    @SerializedName("iconclass") val iconClass: String? = null
)