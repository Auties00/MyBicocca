package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCalendarEventByIdRequest(
    @SerializedName("eventid") val eventId: Int
)