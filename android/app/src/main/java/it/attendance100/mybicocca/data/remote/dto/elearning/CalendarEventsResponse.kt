package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class CalendarEventsResponse(
    @SerializedName("events") val events: List<CalendarEventListItem>? = null,
    @SerializedName("warnings") val warnings: List<Warning>? = null
)