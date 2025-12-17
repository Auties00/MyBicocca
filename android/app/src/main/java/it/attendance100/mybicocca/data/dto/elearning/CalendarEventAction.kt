package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarEventAction(
    @SerializedName("name") val name: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("itemcount") val itemCount: Int? = null,
    @SerializedName("actionable") val actionable: Boolean? = null,
    @SerializedName("showitemcount") val showItemCount: Boolean? = null
)