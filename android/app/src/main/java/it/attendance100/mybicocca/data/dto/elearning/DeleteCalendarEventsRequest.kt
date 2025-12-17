package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class DeleteCalendarEventsRequest(
    @SerializedName("events") val events: List<DeleteCalendarEventEntry>? = null
)

data class DeleteCalendarEventEntry(
    @SerializedName("eventid") val eventId: Int,
    @SerializedName("repeat") val repeat: Boolean
)