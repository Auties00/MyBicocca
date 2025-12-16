package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarEventResponse(
    @SerializedName("event") val event: CalendarEvent? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)